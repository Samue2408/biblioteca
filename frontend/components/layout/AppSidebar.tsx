"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthProvider";

const links = [
  { href: "/", label: "Catálogo" },
  { href: "/prestamos", label: "Mis préstamos" },
] as const;

export function AppSidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  return (
    <aside className="flex w-full flex-col gap-8 border-b border-zinc-200 p-5 md:w-56 md:border-b-0 md:border-r">
      <p className="text-sm font-semibold tracking-tight text-zinc-900">Biblioteca</p>

      <nav className="flex flex-row gap-1 md:flex-col">
        {links.map((link) => {
          const active = pathname === link.href;
          return (
            <Link
              key={link.href}
              href={link.href}
              className={`rounded-md px-2.5 py-1.5 text-sm transition-colors ${
                active
                  ? "bg-zinc-100 font-medium text-zinc-900"
                  : "text-zinc-500 hover:bg-zinc-50 hover:text-zinc-900"
              }`}
            >
              {link.label}
            </Link>
          );
        })}

        {user?.role === "ADMIN" ? (
          <Link
            href="/admin"
            className={`rounded-md px-2.5 py-1.5 text-sm transition-colors ${
              pathname.startsWith("/admin")
                ? "bg-zinc-100 font-medium text-zinc-900"
                : "text-zinc-500 hover:bg-zinc-50 hover:text-zinc-900"
            }`}
          >
            Administración
          </Link>
        ) : null}
      </nav>

      <div className="mt-auto flex flex-col gap-3 border-t border-zinc-200 pt-4 text-sm">
        <p className="truncate text-zinc-500">{user?.email}</p>
        <button
          type="button"
          onClick={logout}
          className="w-fit text-zinc-500 transition-colors hover:text-zinc-900"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
