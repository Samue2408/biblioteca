"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { ApiException } from "@/lib/api/errors";
import { findBooks } from "@/services/books.service";
import { createLoan } from "@/services/loans.service";
import {
  cancelReservation,
  createReservation,
  findMyReservations,
} from "@/services/reservations.service";
import type { BookFilters, BookResponse, BookStatus } from "@/types/book";
import type { ReservationResponse } from "@/types/reservation";

const statusOptions: Array<{ value: BookStatus; label: string }> = [
  { value: "DISPONIBLE", label: "Disponible" },
  { value: "PRESTADO", label: "Prestado" },
  { value: "RESERVADO", label: "Reservado" },
];

const statusLabel: Record<string, string> = {
  DISPONIBLE: "Disponible",
  PRESTADO: "Prestado",
  RESERVADO: "Reservado",
};

function statusClass(status: string) {
  switch (status) {
    case "DISPONIBLE":
      return "bg-emerald-100 text-emerald-800";
    case "PRESTADO":
      return "bg-amber-100 text-amber-800";
    case "RESERVADO":
      return "bg-sky-100 text-sky-800";
    default:
      return "bg-zinc-100 text-zinc-700";
  }
}

export default function CatalogPage() {
  const [books, setBooks] = useState<BookResponse[]>([]);
  const [filters, setFilters] = useState<BookFilters>({});
  const [appliedFilters, setAppliedFilters] = useState<BookFilters>({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionBookId, setActionBookId] = useState<number | null>(null);
  const [reservations, setReservations] = useState<Record<string, ReservationResponse>>({});
  const [feedback, setFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);

  const loadBooks = useCallback(async (nextFilters: BookFilters) => {
    setIsLoading(true);
    setError(null);

    try {
      setBooks(await findBooks(nextFilters));
    } catch (caught) {
      setError(
        caught instanceof ApiException
          ? caught.message
          : "No se pudo cargar el catálogo. Inténtalo de nuevo.",
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let isCurrent = true;

    async function loadInitialBooks() {
      try {
        const nextBooks = await findBooks(appliedFilters);
        if (isCurrent) {
          setBooks(nextBooks);
          setError(null);
          setIsLoading(false);
        }
      } catch (caught) {
        if (isCurrent) {
          setError(
            caught instanceof ApiException
              ? caught.message
              : "No se pudo cargar el catálogo. Inténtalo de nuevo.",
          );
          setIsLoading(false);
        }
      }
    }

    void loadInitialBooks();
    return () => {
      isCurrent = false;
    };
  }, [appliedFilters]);

  useEffect(() => {
    let isCurrent = true;

    async function loadReservations() {
      try {
        const responses = await findMyReservations();
        if (isCurrent) {
          setReservations(
            Object.fromEntries(
              responses
                .filter((reservation) =>
                  ["PENDIENTE", "NOTIFICADO"].includes(reservation.status),
                )
                .map((reservation) => [reservation.bookTitle, reservation]),
            ),
          );
        }
      } catch {
        // El catálogo sigue siendo usable aunque no se puedan recuperar reservas previas.
      }
    }

    void loadReservations();
    return () => {
      isCurrent = false;
    };
  }, []);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsLoading(true);
    setAppliedFilters(filters);
  }

  function resetFilters() {
    setIsLoading(true);
    setFilters({});
    setAppliedFilters({});
  }

  async function requestLoan(book: BookResponse) {
    setActionBookId(book.id);
    setFeedback(null);

    try {
      const loan = await createLoan({ bookId: book.id });
      setFeedback({
        type: "success",
        message: `Préstamo confirmado. Debes devolver “${loan.bookTitle}” el ${loan.dueDate}.`,
      });
      await loadBooks(appliedFilters);
    } catch (caught) {
      setFeedback({
        type: "error",
        message: caught instanceof ApiException ? caught.message : "No se pudo solicitar el préstamo.",
      });
    } finally {
      setActionBookId(null);
    }
  }

  async function reserveBook(book: BookResponse) {
    setActionBookId(book.id);
    setFeedback(null);

    try {
      const reservation = await createReservation({ bookId: book.id });
      setReservations((current) => ({ ...current, [book.title]: reservation }));
      setFeedback({
        type: "success",
        message: `Reserva confirmada para “${reservation.bookTitle}”. Tu posición en la fila es ${reservation.position}.`,
      });
    } catch (caught) {
      setFeedback({
        type: "error",
        message: caught instanceof ApiException ? caught.message : "No se pudo crear la reserva.",
      });
    } finally {
      setActionBookId(null);
    }
  }

  async function cancelBookReservation(book: BookResponse, reservation: ReservationResponse) {
    setActionBookId(book.id);
    setFeedback(null);

    try {
      await cancelReservation(reservation.id);
      setReservations((current) => {
        const next = { ...current };
        delete next[book.title];
        return next;
      });
      setFeedback({
        type: "success",
        message: `La reserva de “${reservation.bookTitle}” fue cancelada.`,
      });
    } catch (caught) {
      setFeedback({
        type: "error",
        message: caught instanceof ApiException ? caught.message : "No se pudo cancelar la reserva.",
      });
    } finally {
      setActionBookId(null);
    }
  }

  return (
    <main className="mx-auto w-full max-w-6xl">
      <header className="mb-8">
        <p className="mb-2 text-sm font-medium text-indigo-600">Biblioteca</p>
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-900">
          Catálogo de libros
        </h1>
        <p className="mt-2 text-zinc-600">
          Encuentra títulos por nombre, autor o disponibilidad.
        </p>
      </header>

      <form
        onSubmit={handleSubmit}
        className="mb-8 grid gap-4 rounded-xl border border-zinc-200 bg-white p-4 shadow-sm md:grid-cols-[1fr_1fr_180px_auto]"
      >
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Título
          <input
            type="search"
            value={filters.title ?? ""}
            onChange={(event) =>
              setFilters((current) => ({ ...current, title: event.target.value }))
            }
            placeholder="Ej. Cien años de soledad"
            className="rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Autor
          <input
            type="search"
            value={filters.author ?? ""}
            onChange={(event) =>
              setFilters((current) => ({ ...current, author: event.target.value }))
            }
            placeholder="Ej. Gabriel García Márquez"
            className="rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Estado
          <select
            value={filters.status ?? ""}
            onChange={(event) =>
              setFilters((current) => ({
                ...current,
                status: event.target.value === "" ? undefined : (event.target.value as BookStatus),
              }))
            }
            className="rounded-lg border border-zinc-300 bg-white px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
          >
            <option value="">Todos</option>
            {statusOptions.map((status) => (
              <option key={status.value} value={status.value}>
                {status.label}
              </option>
            ))}
          </select>
        </label>
        <div className="flex items-end gap-2">
          <button
            type="submit"
            className="rounded-lg bg-indigo-600 px-4 py-2 font-medium text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={isLoading}
          >
            Buscar
          </button>
          <button
            type="button"
            onClick={resetFilters}
            className="rounded-lg px-3 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-100"
          >
            Limpiar
          </button>
        </div>
      </form>

      {feedback ? (
        <section
          className={`mb-6 rounded-xl border p-4 text-sm ${
            feedback.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-900"
              : "border-red-200 bg-red-50 text-red-900"
          }`}
          role={feedback.type === "error" ? "alert" : "status"}
        >
          {feedback.message}
        </section>
      ) : null}

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" aria-live="polite">
          {[0, 1, 2, 3, 4, 5].map((item) => (
            <div key={item} className="h-44 animate-pulse rounded-xl bg-zinc-200" />
          ))}
          <span className="sr-only">Cargando libros…</span>
        </div>
      ) : null}

      {!isLoading && error ? (
        <section className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-900" role="alert">
          <h2 className="font-semibold">No fue posible cargar el catálogo</h2>
          <p className="mt-1 text-sm">{error}</p>
          <button
            type="button"
            onClick={() => void loadBooks(appliedFilters)}
            className="mt-4 rounded-lg bg-red-700 px-3 py-2 text-sm font-medium text-white hover:bg-red-800"
          >
            Reintentar
          </button>
        </section>
      ) : null}

      {!isLoading && !error && books.length === 0 ? (
        <section className="rounded-xl border border-dashed border-zinc-300 bg-zinc-50 px-6 py-14 text-center">
          <h2 className="text-lg font-semibold text-zinc-900">No hay libros para mostrar</h2>
          <p className="mt-2 text-sm text-zinc-600">
            Prueba cambiando los filtros o vuelve a consultar el catálogo completo.
          </p>
          {Object.values(appliedFilters).some(Boolean) ? (
            <button
              type="button"
              onClick={resetFilters}
              className="mt-4 rounded-lg border border-zinc-300 bg-white px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
            >
              Ver todos los libros
            </button>
          ) : null}
        </section>
      ) : null}

      {!isLoading && !error && books.length > 0 ? (
        <section aria-live="polite">
          <p className="mb-4 text-sm text-zinc-600">
            {books.length} {books.length === 1 ? "libro encontrado" : "libros encontrados"}
          </p>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {books.map((book) => (
              <article key={book.id} className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <h2 className="text-lg font-semibold text-zinc-900">{book.title}</h2>
                    <p className="mt-1 text-sm text-zinc-600">{book.author}</p>
                  </div>
                  <span className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-medium ${statusClass(book.status)}`}>
                    {statusLabel[book.status] ?? book.status}
                  </span>
                </div>
                <dl className="mt-5 grid grid-cols-2 gap-x-4 gap-y-3 border-t border-zinc-100 pt-4 text-sm">
                  <div>
                    <dt className="text-zinc-500">ISBN</dt>
                    <dd className="mt-0.5 font-medium text-zinc-800">{book.isbn}</dd>
                  </div>
                  <div>
                    <dt className="text-zinc-500">Publicación</dt>
                    <dd className="mt-0.5 font-medium text-zinc-800">
                      {book.publicationYear ?? "Sin dato"}
                    </dd>
                  </div>
                </dl>
                <div className="mt-5 border-t border-zinc-100 pt-4">
                  {reservations[book.title] ? (
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <p className="text-sm text-zinc-600">
                        Reserva confirmada · posición {reservations[book.title].position}
                      </p>
                      <button
                        type="button"
                        onClick={() => void cancelBookReservation(book, reservations[book.title])}
                        disabled={actionBookId === book.id}
                        className="rounded-lg border border-red-300 px-3 py-2 text-sm font-medium text-red-700 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        {actionBookId === book.id ? "Cancelando…" : "Cancelar reserva"}
                      </button>
                    </div>
                  ) : book.status === "DISPONIBLE" ? (
                    <button
                      type="button"
                      onClick={() => void requestLoan(book)}
                      disabled={actionBookId === book.id}
                      className="w-full rounded-lg bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {actionBookId === book.id ? "Solicitando…" : "Solicitar préstamo"}
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => void reserveBook(book)}
                      disabled={actionBookId === book.id}
                      className="w-full rounded-lg border border-indigo-300 px-3 py-2 text-sm font-medium text-indigo-700 hover:bg-indigo-50 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {actionBookId === book.id ? "Reservando…" : "Reservar"}
                    </button>
                  )}
                </div>
              </article>
            ))}
          </div>
        </section>
      ) : null}
    </main>
  );
}
