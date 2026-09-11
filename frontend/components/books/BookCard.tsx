import { type ReactNode } from "react";
import type { BookResponse, BookStatus } from "@/types/book";

type BookCardProps = {
  book: BookResponse;
  children?: ReactNode;
};

const STATUS_STYLES: Record<BookStatus, { label: string; className: string }> = {
  DISPONIBLE: {
    label: "Disponible",
    className: "bg-emerald-200/70 text-emerald-800",
  },
  PRESTADO: {
    label: "Prestado",
    className: "bg-amber-200/70 text-amber-800",
  },
  RESERVADO: {
    label: "Reservado",
    className: "bg-sky-200/70 text-sky-800",
  },
  ELIMINADO: {
    label: "Eliminado",
    className: "bg-zinc-100 text-zinc-800"
  }
};

export function BookCard({ book, children }: BookCardProps) {
  const status = STATUS_STYLES[book.status as BookStatus];

  return (
    <article className="flex w-full max-w-[225px] flex-col overflow-hidden rounded-lg border border-stone-200 bg-white shadow-sm transition-shadow hover:shadow-md">
      <div className="relative aspect-2/3 w-full bg-stone-100">
        {book.coverUrl ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={book.coverUrl}
            alt={`Portada de ${book.title}`}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="flex h-full w-full flex-col items-center justify-center gap-2 px-4 text-center text-stone-400">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth={1.5}
              className="h-8 w-8"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M12 6.042A8.967 8.967 0 0 0 6 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 0 1 6 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 0 1 6-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0 0 18 18a8.967 8.967 0 0 0-6 2.292m0-14.25v14.25"
              />
            </svg>
            <span className="text-xs leading-tight">Sin portada disponible</span>
          </div>
        )}

        <span
          className={`absolute right-2 top-2 rounded-full px-2 py-0.5 text-xs font-medium  ${status.className}`}
        >
          {status.label}
        </span>
      </div>

      <div className="flex flex-1 flex-col gap-1 p-3">
        <h3 className="font-serif text-sm font-semibold leading-snug text-stone-900 line-clamp-2">
          {book.publicationYear ? `${book.title} - ${book.publicationYear}` : book.title}
        </h3>
        <p className="text-xs text-stone-500 line-clamp-1">{book.author}</p>
        <p className="mt-0.5 text-[11px] text-stone-400">ISBN {book.isbn}</p>

        {children && (
          <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-stone-100 pt-3">
            {children}
          </div>
        )}
      </div>
    </article>
  );
}