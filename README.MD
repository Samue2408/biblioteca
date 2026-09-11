# Sistema de préstamos para biblioteca

Aplicación Fullstack para la gestión de libros, préstamos y reservas de una biblioteca.

El proyecto reemplaza un proceso manual basado en hojas de cálculo por una aplicación web con autenticación, control de disponibilidad, préstamos, devoluciones, lista de espera, bloqueo por atrasos, notificaciones por correo e integración con Open Library.

## Tecnologías

### Backend

* Java 21
* Spring Boot 4.1.1
* Spring Web MVC
* Spring Data JPA
* Spring Security
* JWT mediante JJWT 0.13.0
* BCrypt
* Spring Validation
* Spring Mail
* Spring Cache
* Thymeleaf
* Thymeleaf Extras Spring Security 6
* Flyway
* PostgreSQL
* Spring Boot Actuator
* Lombok

### Frontend

* Next.js 16.3.4
* React 19.2.8
* TypeScript 5
* Tailwind CSS 4
* ESLint 9

### Infraestructura

* Docker
* Docker Compose
* PostgreSQL 17
* MailHog


## Arquitectura

El proyecto está dividido en frontend y backend independientes.

```text
┌────────────────────┐
│     Frontend       │
│      Next.js       │
└─────────┬──────────┘
          │ HTTP / JWT
          ▼
┌────────────────────┐
│      Backend       │
│    Spring Boot     │
└──────┬───────┬─────┘
       │       │
       ▼       ▼
 PostgreSQL  MailHog
       │
       ▼
 Open Library
```

## Funcionalidades principales

* Registro e inicio de sesión.
* Autenticación mediante JWT.
* Control de acceso mediante roles.
* Catálogo de libros.
* Búsqueda por título, autor y estado.
* Registro de libros mediante ISBN.
* Autocompletado de información mediante Open Library.
* Préstamos de libros.
* Devolución de libros.
* Reservas y lista de espera.
* Control de atrasos.
* Bloqueo temporal de usuarios.
* Notificaciones por correo.
* Estadísticas administrativas.
* Eliminación lógica de libros.
* Restauración de libros.

# Reglas y decisiones de negocio

## Usuarios y roles

La aplicación maneja dos roles:

* `ADMIN`
* `BIBLIOTECARIO`

El endpoint público de registro no permite seleccionar el rol.

Todo usuario registrado mediante:

```http
POST /api/auth/register
```

es creado automáticamente con el rol:

```text
BIBLIOTECARIO
```

Esta decisión evita que un usuario pueda asignarse a sí mismo privilegios administrativos.

Los usuarios `ADMIN` mediante una operación controlada directamente en la base de datos. Para la prueba se dispone de un usuario `ADMIN` de ejemplo.

## Préstamos y reservas asociados a usuarios

Los préstamos y reservas siempre están asociados a un usuario registrado mediante una relación con `AppUser`.

No se almacenan el nombre o correo del usuario como texto libre dentro de `Loan` o `Reservation`.

Esto permite mantener la integridad referencial y evita inconsistencias entre los datos actuales del usuario y su historial de préstamos o reservas.

## Control de usuarios bloqueados

El bloqueo del usuario se valida antes de realizar la búsqueda del libro.

Si el usuario está bloqueado, la operación se rechaza inmediatamente sin consultar la existencia o disponibilidad del libro.

Esta decisión evita una consulta innecesaria a la base de datos y mantiene como prioridad la regla de que un usuario bloqueado no puede solicitar préstamos.

## Duración del préstamo

Cada préstamo tiene una duración de 14 días.

La fecha límite se calcula como:

```text
dueDate = loanDate + 14 días
```

## Atrasos

Los atrasos no se mantienen mediante un contador independiente.

Se calculan dinámicamente a partir del historial real de devoluciones del usuario.

Una devolución se considera tardía cuando:

```text
returnDate > dueDate
```

Para determinar el número de atrasos relevantes se consideran únicamente las devoluciones tardías cuya fecha se encuentre dentro de los últimos 90 días.

Conceptualmente:

```text
returnDate > dueDate
AND
returnDate >= hoy - 90 días
```

Esta lógica se implementa mediante una consulta JPQL porque la condición compara dos columnas de la misma entidad (`returnDate` y `dueDate`), algo que no puede expresarse mediante un método derivado convencional de Spring Data.

Esta estrategia evita mantener un contador separado que pueda desincronizarse del historial real de préstamos.

### Bloqueo por atrasos

Cuando un usuario alcanza tres atrasos dentro de los últimos 90 días:

* queda bloqueado para solicitar nuevos préstamos;
* el bloqueo dura una semana;
* se envía una notificación de bloqueo por correo.

Un `ADMIN` puede levantar manualmente el bloqueo antes de que termine el período.

El historial de atrasos no se elimina al realizar un desbloqueo manual. Por ello, si los tres atrasos continúan dentro de la ventana de 90 días, un nuevo atraso puede provocar nuevamente el bloqueo.

Este comportamiento es intencional y se deriva directamente del cálculo dinámico sobre el historial real.

## Préstamos de libros reservados

Un libro puede encontrarse en uno de los siguientes estados:

```text
DISPONIBLE
PRESTADO
RESERVADO
ELIMINADO
```

Cuando un libro se encuentra `RESERVADO`, solamente puede obtener el préstamo el usuario que tenga la primera reserva pendiente para ese libro.

Las reservas se ordenan por fecha de solicitud (`requestDate`).

El flujo es:

```text
RESERVADO
    ↓
buscar primera reserva PENDIENTE
    ↓
¿pertenece al usuario actual?
    ├── Sí → puede solicitar el préstamo
    └── No → préstamo rechazado
```

Cuando el usuario correspondiente toma el libro:

```text
Reservation: PENDIENTE → CUMPLIDO
Book:        RESERVADO → PRESTADO
```

La actualización de la reserva y la creación del préstamo forman parte de la misma transacción.

## Lista de espera

Cuando un libro no está disponible, un usuario puede registrarse mediante una reserva.

Las reservas pendientes se mantienen en orden de solicitud.

Cuando el libro es devuelto y existen reservas pendientes:

1. Se identifica al primer usuario de la cola.
2. Se envía una notificación indicando que el libro está disponible.
3. El libro pasa a estado `RESERVADO`.
4. El usuario que encabeza la cola puede solicitar el préstamo.

## Eliminación de libros

Los libros utilizan eliminación lógica (soft delete).

No se elimina físicamente el registro de la base de datos, con el objetivo de conservar la integridad de los datos históricos relacionados con préstamos y reservas.

Cuando un libro es eliminado, su estado pasa a:

```text
ELIMINADO
```


Los libros eliminados pueden ser restaurados mediante:

```http
PUT /api/books/{id}/restore
```

## Integración con Open Library

La aplicación permite autocompletar información de un libro a partir de su ISBN.

Se utiliza Open Library para consultar información bibliográfica como:

* título;
* autor;
* año de publicación;
* portada.

La consulta se realiza mediante el ISBN.

Se utiliza Spring Cache para almacenar en memoria los resultados de consultas a Open Library, evitando solicitudes repetidas para un mismo ISBN.

### Estrategia de caché

El resultado de la consulta se cachea tanto cuando se obtiene información como cuando el resultado es `Optional.empty()`.

Esto evita volver a consultar Open Library cuando el ISBN es válido pero la API no proporciona información para ese libro.

Cuando ocurre una excepción `ExternalBookLookupException`, el resultado no se almacena en caché.

De esta forma, una falla temporal de Open Library no queda almacenada indefinidamente y una consulta posterior puede intentar nuevamente acceder al servicio externo.

### Fallos de Open Library

La disponibilidad de Open Library no debe impedir el registro de un libro.

Cuando la API externa falla, el libro puede registrarse utilizando los datos introducidos manualmente.

Esto evita que una dependencia externa interrumpa una operación interna de la biblioteca.

## Notificaciones por correo

La aplicación utiliza Spring Mail junto con plantillas Thymeleaf para generar los mensajes.

Se implementan los siguientes tipos de notificación:

1. Confirmación de préstamo.
2. Recordatorio antes del vencimiento.
3. Aviso de bloqueo por atrasos.
4. Notificación de disponibilidad de un libro reservado.

La confirmación del préstamo se procesa de forma asíncrona después de completar la transacción, evitando bloquear la petición HTTP original.

Los recordatorios se ejecutan mediante una tarea programada.

MailHog se utiliza para interceptar los mensajes durante desarrollo y pruebas sin necesidad de utilizar credenciales SMTP reales.

## Autenticación y seguridad

La autenticación utiliza JWT.

Después del inicio de sesión, el backend devuelve:

```json
{
  "token": "<JWT>",
  "role": "ADMIN"
}
```

El JWT contiene el correo electrónico del usuario como subject (`sub`).

Las solicitudes protegidas utilizan:

```http
Authorization: Bearer <token>
```

Las contraseñas se almacenan utilizando BCrypt.

El registro público siempre crea usuarios con rol `BIBLIOTECARIO`.

Las operaciones administrativas requieren el rol `ADMIN`.

## API REST

### Autenticación

```http
POST /api/auth/register
POST /api/auth/login
GET /api/auth/me
```

### Libros

```http
GET    /api/books
GET    /api/books/lookup/{isbn}
POST   /api/books
DELETE /api/books/{id}
PUT    /api/books/{id}/restore
```

`GET /api/books` admite filtros opcionales:

```text
/api/books?title=java
/api/books?author=martin
/api/books?status=DISPONIBLE
```

### Préstamos

```http
POST /api/loans
GET  /api/loans/mine
PUT  /api/loans/{id}/return
```

### Reservas

```http
POST   /api/reservations
DELETE /api/reservations/{id}
GET    /api/reservations/mine
```

### Administración

```http
GET /api/admin/stats
GET /api/admin/users/blocked
PUT /api/admin/users/{id}/unblock
```

### Health / conectividad

```http
GET /api/ping
```

Este endpoint requiere autenticación.

## Manejo de errores

El backend utiliza `@RestControllerAdvice` para centralizar el tratamiento de excepciones.

Las respuestas de error siguen una estructura consistente que incluye código, mensaje y ruta.


## Frontend

El frontend está desarrollado con Next.js 16, React 19, TypeScript y Tailwind CSS.

La aplicación proporciona:

* Registro e inicio de sesión.
* Gestión de sesión mediante JWT.
* Catálogo de libros.
* Búsqueda y filtrado por título, autor y estado.
* Consulta de información de libros mediante ISBN.
* Autocompletado de libros utilizando Open Library.
* Solicitud y devolución de préstamos.
* Gestión de reservas.
* Panel de administración.
* Consulta de estadísticas.
* Gestión de libros para usuarios ADMIN.

La comunicación con el backend se realiza mediante HTTP utilizando las capacidades disponibles en el propio proyecto, manteniendo una separación entre la interfaz, los servicios de comunicación y los tipos de datos.

El frontend contempla estados de carga, éxito, error y ausencia de resultados, además de validaciones y control de acceso según el rol del usuario.

### Dependencias principales

```text
Next.js       16.3.4
React         19.2.8
React DOM     19.2.8
TypeScript    5
Tailwind CSS  4
ESLint        9
```


## Base de datos

La aplicación utiliza PostgreSQL y las modificaciones de esquema se gestionan mediante migraciones versionadas con Flyway.

Entidades principales:

```text
AppUser
Book
Loan
Reservation
```

Las relaciones entre ellas permiten mantener la trazabilidad de usuarios, préstamos y reservas.

## Docker

Todo el entorno puede levantarse mediante Docker Compose.

Servicios:

```text
frontend
backend
postgres
mailhog
```

Ejecutar:

```bash
docker compose up --build
```

Servicios disponibles:

```text
Frontend
http://localhost:3000

Backend
http://localhost:8080

MailHog
http://localhost:8025

PostgreSQL
localhost:5432
```

## Variables de entorno

Ejemplo de configuración:

```env
POSTGRES_DB=app
POSTGRES_USER=app
POSTGRES_PASSWORD=app

POSTGRES_PORT=5432

BACKEND_PORT=8080

FRONTEND_PORT=3000

MAILHOG_SMTP_PORT=1025
MAILHOG_UI_PORT=8025
```

No deben almacenarse secretos o credenciales reales en el repositorio.

Para entornos locales, utilizar `.env` o `.env.local` según el componente correspondiente.

## Usuario ADMIN de prueba

Para probar las funcionalidades administrativas se utiliza un usuario `ADMIN` creado de forma controlada directamente en la base de datos.

Ejemplo:

```text
Email: admin@example.com
Password: Admin1234
Role: ADMIN
```

> Reemplazar estas credenciales por seguridad.

El endpoint público de registro no permite crear usuarios con rol `ADMIN`.

## Pruebas

El proyecto cuenta con pruebas automatizadas para las principales reglas de negocio y componentes solicitados en la prueba técnica.

Se cubren:

* ISBN duplicado.
* Creación de préstamos.
* Devolución y cálculo de atrasos.
* Bloqueo por atrasos.
* Endpoints REST.
* Envío de correos.
* Integración con Open Library.
* Manejo de errores de Open Library.


## Colección Postman / Insomnia

El proyecto incluye una colección para facilitar las pruebas manuales de la API.

https://mult-api-team.postman.co/workspace/Team-Workspace~f26ddc31-5093-45d7-8edf-70f4fe4569a3/collection/30132939-5903f1f6-6197-462d-8ebc-19f98dcfcbfc?action=share&creator=30132939

## Decisiones de diseño

Las decisiones no completamente definidas por el enunciado se documentan en este README.

Las principales decisiones son:

* Los usuarios que se registran públicamente siempre tienen rol `BIBLIOTECARIO`.
* Los préstamos y reservas siempre pertenecen a usuarios registrados.
* Los atrasos se calculan a partir del historial real.
* Los bloqueos se basan en tres atrasos dentro de una ventana de 90 días.
* Los libros utilizan eliminación lógica.
* Las reservas se procesan en orden de solicitud.
* Un libro reservado únicamente puede ser tomado por el usuario que encabeza la lista de espera.
* Los resultados de Open Library se almacenan en caché.
* Los fallos temporales de Open Library no se cachean como resultado negativo.
* Las notificaciones se procesan sin bloquear la petición que origina el evento.

Estas decisiones buscan preservar la integridad de los datos, mantener las reglas de negocio consistentes y evitar dependencias innecesarias entre componentes.
