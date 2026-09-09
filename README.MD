decidí que un préstamo/reserva siempre está asociado a un usuario registrado, en vez de guardar nombre/correo como texto libre

RegisterRequest no trae role. Cualquiera que se registre por /api/auth/register entra como BIBLIOTECARIO a la fuerza — si dejaras que el propio request eligiera ADMIN, cualquiera podría autoasignarse el rol más alto. Un ADMIN lo crea otro ADMIN (o tú a mano en la BD para el usuario de prueba).

valido el bloqueo antes de buscar el libro. Es a propósito — si el usuario está bloqueado, no importa si el libro existe o está disponible, la respuesta debe ser la misma. Evita hacerle una consulta a la BD para nada.


LoanRepository.java

´
Query("""
            SELECT COUNT(l) FROM Loan l
            WHERE l.borrower = :borrower
              AND l.returnDate IS NOT NULL
              AND l.returnDate > l.dueDate
              AND l.returnDate >= :since
            """)
    long countLateReturnsSince(@Param("borrower") AppUser borrower, @Param("since") LocalDate since);
´

esto no se puede expresar como derived query method (findByX...) porque compara dos columnas de la misma entidad entre sí (returnDate > dueDate); Spring Data no soporta eso por nombre, por eso el @Query con JPQL.

los atrasos se calculan en el momento contando devoluciones tardías del usuario en los últimos 90 días (returnDate > dueDate y returnDate >= hoy - 90 días). Esto respeta literalmente la regla del enunciado y evita que el contador se desincronice del historial real. Como consecuencia, si un ADMIN desbloquea la cuenta antes de tiempo pero los 3 atrasos siguen dentro de la ventana de 90 días, el siguiente atraso vuelve a bloquear inmediatamente — es el comportamiento esperado, no un bug.

el @Cacheable: cachea tanto el resultado positivo como el Optional.empty() (ISBN válido pero sin datos en Open Library) — evita pegarle a la API de nuevo por el mismo ISBN. Si el método lanza ExternalBookLookupException, Spring no cachea eso — es lo correcto, para que la próxima consulta reintente en vez de quedar "cacheado el fallo" para siempre.