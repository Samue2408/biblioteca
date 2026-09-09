"use client";

import { useEffect, useState } from "react";
import { ApiException } from "@/lib/api/errors";
import { findMyLoans, returnLoan } from "@/services/loans.service";
import type { LoanResponse } from "@/types/loan";

function toLocalDate(value: string) {
  return new Date(`${value}T00:00:00`);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("es-CO", {
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(toLocalDate(value));
}

function isOverdue(loan: LoanResponse) {
  if (loan.returnDate) {
    return false;
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return toLocalDate(loan.dueDate) < today;
}

function loanStatus(loan: LoanResponse) {
  if (loan.returnDate) {
    return { label: "Devuelto", className: "bg-emerald-100 text-emerald-800" };
  }
  if (isOverdue(loan)) {
    return { label: "Vencido", className: "bg-red-100 text-red-800" };
  }
  return { label: "Activo", className: "bg-sky-100 text-sky-800" };
}

export default function LoansPage() {
  const [loans, setLoans] = useState<LoanResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [returningId, setReturningId] = useState<number | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);

  async function loadLoans() {
    setIsLoading(true);
    setError(null);

    try {
      setLoans(await findMyLoans());
    } catch (caught) {
      setError(
        caught instanceof ApiException
          ? caught.message
          : "No se pudieron cargar tus préstamos. Inténtalo de nuevo.",
      );
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    let isCurrent = true;

    async function loadInitialLoans() {
      try {
        const nextLoans = await findMyLoans();
        if (isCurrent) {
          setLoans(nextLoans);
          setIsLoading(false);
        }
      } catch (caught) {
        if (isCurrent) {
          setError(
            caught instanceof ApiException
              ? caught.message
              : "No se pudieron cargar tus préstamos. Inténtalo de nuevo.",
          );
          setIsLoading(false);
        }
      }
    }

    void loadInitialLoans();
    return () => {
      isCurrent = false;
    };
  }, []);

  async function handleReturn(loan: LoanResponse) {
    setReturningId(loan.id);
    setFeedback(null);

    try {
      const returnedLoan = await returnLoan(loan.id);
      setLoans((current) =>
        current.map((item) => (item.id === returnedLoan.id ? returnedLoan : item)),
      );
      setFeedback(`Confirmamos la devolución de “${returnedLoan.bookTitle}”.`);
    } catch (caught) {
      setError(
        caught instanceof ApiException ? caught.message : "No se pudo registrar la devolución.",
      );
    } finally {
      setReturningId(null);
    }
  }

  return (
    <main className="mx-auto w-full max-w-4xl">
      <header className="mb-8">
        <p className="mb-2 text-sm font-medium text-indigo-600">Biblioteca</p>
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-900">Mis préstamos</h1>
        <p className="mt-2 text-zinc-600">Consulta las fechas y devuelve los libros que ya terminaste.</p>
      </header>

      {feedback ? (
        <p className="mb-6 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-900" role="status">
          {feedback}
        </p>
      ) : null}

      {isLoading ? (
        <div className="space-y-4" aria-live="polite">
          {[0, 1, 2].map((item) => (
            <div key={item} className="h-40 animate-pulse rounded-xl bg-zinc-200" />
          ))}
          <span className="sr-only">Cargando préstamos…</span>
        </div>
      ) : null}

      {!isLoading && error ? (
        <section className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-900" role="alert">
          <h2 className="font-semibold">No fue posible cargar tus préstamos</h2>
          <p className="mt-1 text-sm">{error}</p>
          <button
            type="button"
            onClick={() => void loadLoans()}
            className="mt-4 rounded-lg bg-red-700 px-3 py-2 text-sm font-medium text-white hover:bg-red-800"
          >
            Reintentar
          </button>
        </section>
      ) : null}

      {!isLoading && !error && loans.length === 0 ? (
        <section className="rounded-xl border border-dashed border-zinc-300 bg-zinc-50 px-6 py-14 text-center">
          <h2 className="text-lg font-semibold text-zinc-900">Aún no tienes préstamos</h2>
          <p className="mt-2 text-sm text-zinc-600">Cuando solicites un libro, aparecerá aquí.</p>
        </section>
      ) : null}

      {!isLoading && !error && loans.length > 0 ? (
        <div className="space-y-4">
          {loans.map((loan) => {
            const status = loanStatus(loan);
            const overdue = isOverdue(loan);

            return (
              <article
                key={loan.id}
                className={`rounded-xl border bg-white p-5 shadow-sm ${
                  overdue ? "border-red-300" : "border-zinc-200"
                }`}
              >
                <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="text-lg font-semibold text-zinc-900">{loan.bookTitle}</h2>
                      <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${status.className}`}>
                        {status.label}
                      </span>
                    </div>
                    <dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2">
                      <div>
                        <dt className="text-zinc-500">Solicitado el</dt>
                        <dd className="mt-0.5 font-medium text-zinc-800">{formatDate(loan.loanDate)}</dd>
                      </div>
                      <div>
                        <dt className={overdue ? "font-medium text-red-700" : "text-zinc-500"}>
                          {loan.returnDate ? "Devuelto el" : "Fecha de devolución"}
                        </dt>
                        <dd className={overdue ? "mt-0.5 font-semibold text-red-700" : "mt-0.5 font-medium text-zinc-800"}>
                          {loan.returnDate ? formatDate(loan.returnDate) : formatDate(loan.dueDate)}
                        </dd>
                      </div>
                    </dl>
                    {overdue ? (
                      <p className="mt-4 text-sm font-medium text-red-700">
                        Este préstamo está vencido. Devuelve el libro lo antes posible.
                      </p>
                    ) : null}
                  </div>
                  {!loan.returnDate ? (
                    <button
                      type="button"
                      onClick={() => void handleReturn(loan)}
                      disabled={returningId === loan.id}
                      className="shrink-0 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {returningId === loan.id ? "Registrando…" : "Devolver libro"}
                    </button>
                  ) : null}
                </div>
              </article>
            );
          })}
        </div>
      ) : null}
    </main>
  );
}
