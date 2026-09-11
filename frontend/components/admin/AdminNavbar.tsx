"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const LINKS = [
  { href: "/admin", label: "Estadísticas" },
  { href: "/admin/books", label: "Libros" },
  { href: "/admin/users", label: "Cuentas bloqueadas" },
];

export function AdminNavbar() {
  const pathname = usePathname();

  return (
    <nav className="flex gap-1 border-b border-zinc-200 px-1 pb-3">
      {LINKS.map((link) => {
        const active = pathname === link.href;
        return (
          <Link
            key={link.href}
            href={link.href}
            className={`rounded-md px-3 py-1.5 text-sm transition-colors ${
              active
                ? "bg-zinc-100 font-medium text-zinc-900"
                : "text-zinc-500 hover:bg-zinc-50 hover:text-zinc-900"
            }`}
          >
            {link.label}
          </Link>
        );
      })}
    </nav>
  );
}