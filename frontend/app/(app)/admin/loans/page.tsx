"use client";

import { useEffect, useState } from "react";
import { findAllLoans } from "@/services/loans.service";
import type { LoanResponse } from "@/types/loan";

 type LoanFilter = "" | "ACTIVE" | "RETURNED" | "OVERDUE";

function formatDate(value: string | null): string {
  if (!value) return "-";
  return new Date(`${value}T00:00:00`).toLocaleDateString("es-ES", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function getLoanStatus(loan: LoanResponse): Exclude<LoanFilter, ""> {
  if (loan.returnDate) return "RETURNED";
  if (new Date(`${loan.dueDate}T00:00:00`) < new Date()) return "OVERDUE";
  return "ACTIVE";
}

const statusLabels: Record<Exclude<LoanFilter, "">, string> = {
  ACTIVE: "Activo",
  RETURNED: "Devuelto",
  OVERDUE: "Vencido",
};

export default function AdminLoansPage() {
  const [loans, setLoans] = useState<LoanResponse[]>([]);
  const [bookFilter, setBookFilter] = useState("");
  const [borrowerFilter, setBorrowerFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState<LoanFilter>("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    findAllLoans()
      .then((result) => {
        if (!cancelled) setLoans(result);
      })
      .catch(() => {
        if (!cancelled) setError("No se pudieron cargar los préstamos.");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const normalizedBookFilter = bookFilter.trim().toLowerCase();
  const normalizedBorrowerFilter = borrowerFilter.trim().toLowerCase();
  const filteredLoans = loans.filter((loan) => {
    const matchesBook = loan.bookTitle.toLowerCase().includes(normalizedBookFilter);
    const matchesBorrower = loan.borrowerEmail.toLowerCase().includes(normalizedBorrowerFilter);
    const matchesStatus = !statusFilter || getLoanStatus(loan) === statusFilter;
    return matchesBook && matchesBorrower && matchesStatus;
  });

  return (
    <div className="flex flex-col gap-8">
      <header>
        <p className="mb-2 text-sm font-medium text-indigo-600">Administración</p>
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-900">Préstamos</h1>
        <p className="mt-2 text-zinc-600">Consulta y filtra el historial de préstamos de la biblioteca.</p>
      </header>

      <section className="grid gap-4 rounded-xl border border-zinc-200 bg-white p-4 shadow-sm md:grid-cols-[1fr_1fr_180px_auto]">
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Libro
          <input
            className="rounded-lg border border-zinc-300 px-3 py-2 font-normal text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            placeholder="Buscar por título"
            value={bookFilter}
            onChange={(event) => setBookFilter(event.target.value)}
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Usuario
          <input
            className="rounded-lg border border-zinc-300 px-3 py-2 font-normal text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            placeholder="Buscar por correo"
            value={borrowerFilter}
            onChange={(event) => setBorrowerFilter(event.target.value)}
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Estado
          <select
            className="rounded-lg border border-zinc-300 bg-white px-3 py-2 font-normal text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as LoanFilter)}
          >
            <option value="">Todos</option>
            <option value="ACTIVE">Activos</option>
            <option value="OVERDUE">Vencidos</option>
            <option value="RETURNED">Devueltos</option>
          </select>
        </label>
        <button
          type="button"
          onClick={() => {
            setBookFilter("");
            setBorrowerFilter("");
            setStatusFilter("");
          }}
          className="self-end rounded-lg px-3 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-100"
        >
          Limpiar
        </button>
      </section>

      <div className="flex items-center justify-between text-sm text-zinc-500">
        <span>{filteredLoans.length} préstamo{filteredLoans.length === 1 ? "" : "s"}</span>
        {!isLoading && loans.length > 0 && <span>{loans.length} en total</span>}
      </div>

      {isLoading ? <p className="text-sm text-zinc-500">Cargando préstamos...</p> : null}
      {error ? <p className="text-sm text-red-600">{error}</p> : null}

      {!isLoading && !error && filteredLoans.length === 0 ? (
        <div className="rounded-md border border-dashed border-zinc-200 px-6 py-10 text-center">
          <p className="text-sm text-zinc-500">No hay préstamos que coincidan con los filtros.</p>
        </div>
      ) : null}

      {filteredLoans.length > 0 ? (
        <div className="overflow-x-auto rounded-md border border-zinc-200">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead>
              <tr className="border-b border-stone-200 text-stone-500">
                <th className="px-4 py-2.5 font-medium">Libro</th>
                <th className="px-4 py-2.5 font-medium">Usuario</th>
                <th className="px-4 py-2.5 font-medium">Inicio</th>
                <th className="px-4 py-2.5 font-medium">Vencimiento</th>
                <th className="px-4 py-2.5 font-medium">Devolución</th>
                <th className="px-4 py-2.5 font-medium">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-100">
              {filteredLoans.map((loan) => {
                const status = getLoanStatus(loan);
                return (
                  <tr key={loan.id}>
                    <td className="px-4 py-3 font-medium text-stone-800">{loan.bookTitle}</td>
                    <td className="px-4 py-3 text-stone-500">{loan.borrowerEmail}</td>
                    <td className="px-4 py-3 text-stone-800">{formatDate(loan.loanDate)}</td>
                    <td className="px-4 py-3 text-stone-800">{formatDate(loan.dueDate)}</td>
                    <td className="px-4 py-3 text-stone-800">{formatDate(loan.returnDate)}</td>
                    <td className="px-4 py-3">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                        status === "OVERDUE"
                          ? "bg-red-50 text-red-700"
                          : status === "RETURNED"
                            ? "bg-zinc-100 text-zinc-600"
                            : "bg-emerald-50 text-emerald-700"
                      }`}>
                        {statusLabels[status]}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  );
}
