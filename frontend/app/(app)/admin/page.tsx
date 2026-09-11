"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getAdminStats } from "@/services/admin.service";
import type { AdminStatsResponse } from "@/types/admin";

function StatCard({
  label,
  value,
  href,
  emphasis,
}: {
  label: string;
  value: number;
  href?: string;
  emphasis?: boolean;
}) {
  const content = (
    <div
      className={`flex flex-col gap-1 rounded-md border p-4 transition-colors ${
        emphasis
          ? "border-indigo-200 bg-indigo-50 hover:bg-indigo-100"
          : "border-zinc-200 hover:bg-zinc-50"
      }`}
    >
      <span
        className={`font-serif text-4xl tabular-nums tracking-tight ${
          emphasis ? "text-indigo-700" : "text-zinc-900"
        }`}
      >
        {value}
      </span>
      <span className={`text-sm ${emphasis ? "text-indigo-700" : "text-zinc-500"}`}>{label}</span>
    </div>
  );

  return href ? <Link href={href}>{content}</Link> : content;
}

export default function AdminPage() {
  const [stats, setStats] = useState<AdminStatsResponse | null>(null);

  useEffect(() => {
    getAdminStats()
      .then(setStats)
      .catch(() => setStats(null));
  }, []);

  if (!stats) {
    return <p className="text-sm text-zinc-500">Cargando estadísticas…</p>;
  }

  return (
    <div className="flex flex-col gap-8">
      <header>
        <p className="mb-2 text-sm font-medium text-indigo-600">Biblioteca</p>
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-900">Panel de administración</h1>
        <p className="mt-2 text-zinc-600">Un vistazo general al estado de la biblioteca</p>
      </header>

      <section className="flex flex-col gap-3">
        <h2 className="text-sm font-medium text-zinc-500">Libros</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <StatCard label="Libros totales" value={stats.totalBooks} />
          <StatCard label="Disponibles" value={stats.availableBooks} />
          <StatCard label="Prestados" value={stats.borrowedBooks} />
          <StatCard label="Reservados" value={stats.reservedBooks} />
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-sm font-medium text-zinc-500">Préstamos y cuentas</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          <StatCard label="Préstamos activos" value={stats.activeLoans} />
          <StatCard
            label="Préstamos vencidos"
            value={stats.overdueLoans}
            emphasis={stats.overdueLoans > 0}
          />
          <StatCard
            label="Usuarios bloqueados"
            value={stats.blockedUsers}
            href="/admin/users"
            emphasis={stats.blockedUsers > 0}
          />
        </div>
      </section>
    </div>
  );
}
