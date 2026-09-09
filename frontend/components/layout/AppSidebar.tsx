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
    <aside className="flex w-full flex-col gap-6 border-b border-zinc-200 p-4 md:w-56 md:border-b-0 md:border-r">
      <p className="font-semibold">Biblioteca</p>
      <nav className="flex flex-row gap-3 md:flex-col">
        {links.map((link) => {
          const active = pathname === link.href;
          return (
            <Link
              key={link.href}
              href={link.href}
              className={active ? "font-medium underline" : "underline-offset-2 hover:underline"}
            >
              {link.label}
            </Link>
          );
        })}
        {user?.role === "ADMIN" ? (
          <Link
            href="/admin"
            className={
              pathname.startsWith("/admin")
                ? "font-medium underline"
                : "underline-offset-2 hover:underline"
            }
          >
            Administración
          </Link>
        ) : null}
      </nav>
      <div className="mt-auto flex flex-col gap-2 text-sm">
        <p className="break-all text-zinc-600">{user?.email}</p>
        <button
          type="button"
          onClick={logout}
          className="w-fit rounded border border-zinc-300 px-3 py-1"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
