"use client";

import { useEffect, useState } from "react";
import { findBooks, lookupBookByIsbn, createBook, deleteBook } from "@/services/books.service";
import { BookCard } from "@/components/books/BookCard";
import type { BookResponse, BookStatus, CreateBookRequest } from "@/types/book";
import { ApiException } from "@/lib/api/errors";

const statusOptions: { label: string; value: BookStatus }[] = [
  { label: "Disponible", value: "DISPONIBLE" },
  { label: "Prestado", value: "PRESTADO" },
  { label: "Reservado", value: "RESERVADO" },
  { label: "Eliminado", value: "ELIMINADO"}
];

export default function AdminBooksPage() {
  const [books, setBooks] = useState<BookResponse[]>([]);

  const [titleFilter, setTitleFilter] = useState("");
  const [authorFilter, setAuthorFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState<BookStatus | "">("");

  const [isbn, setIsbn] = useState("");
  const [title, setTitle] = useState("");
  const [author, setAuthor] = useState("");
  const [coverUrl, setCoverUrl] = useState("");

  const [isAutofilling, setIsAutofilling] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadBooks() {
    const result = await findBooks({
      title: titleFilter || undefined,
      author: authorFilter || undefined,
      status: statusFilter || undefined,
    });
    setBooks(result);
  }

  useEffect(() => {
    void loadBooks();
  }, []);

  function resetFilters() {
    setTitleFilter("");
    setAuthorFilter("");
    setStatusFilter("");
    void loadBooks();
  }

  function resetForm() {
    setIsbn("");
    setTitle("");
    setAuthor("");
    setCoverUrl("");
  }

  async function handleAutofill() {
    setError(null);
    setIsAutofilling(true);
    try {
      const data = await lookupBookByIsbn(isbn);
      setTitle(data.title ?? "");
      setAuthor(data.author ?? "");
      setCoverUrl(data.coverUrl ?? "");
    } catch {
      setError("No se encontró ese ISBN en Open Library. Escribe los datos a mano.");
    } finally {
      setIsAutofilling(false);
    }
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSaving(true);
    try {
      const payload: CreateBookRequest = {
        isbn,
        title: title || undefined,
        author: author || undefined,
        coverUrl: coverUrl || undefined,
      };
      await createBook(payload);
      resetForm();
      await loadBooks();
    } catch (err) {
      if (err instanceof ApiException && err.code === "DUPLICATE_ISBN") {
        setError("Ya existe un libro con ese ISBN.");
      } else if (err instanceof ApiException && err.code === "MISSING_BOOK_DATA") {
        setError("No se pudo autocompletar. Escribe título y autor.");
      } else {
        setError("No se pudo crear el libro.");
      }
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete(id: number) {
    try {
      await deleteBook(id);
      await loadBooks();
    } catch (err) {
      if (err instanceof ApiException && err.code === "BOOK_NOT_DELETABLE") {
        alert("No se puede eliminar: el libro está prestado o reservado.");
      } else {
        alert("No se pudo eliminar el libro.");
      }
    }
  }

  return (
    <div className="flex flex-col gap-8">
      <header>
        <p className="mb-2 text-sm font-medium text-indigo-600">Biblioteca</p>
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-900">Gestión de libros</h1>
        <p className="mt-2 text-zinc-600">Gestiona los libros registrados en la biblioteca</p>
      </header>

      <form
        onSubmit={handleCreate}
        className="flex flex-col gap-4 rounded-xl border border-zinc-200 p-4 mb-8 shadow-sm"
      >
        <h2 className="text-sm font-medium text-zinc-500">Agregar libro</h2>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <input type="hidden" value={coverUrl} readOnly />

        <div className="grid gap-3 sm:grid-cols-[1fr_auto]">
          <input
            className="rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            placeholder="ISBN"
            value={isbn}
            onChange={(e) => setIsbn(e.target.value)}
            required
          />
          <button
            type="button"
            onClick={handleAutofill}
            disabled={!isbn || isAutofilling}
            className="rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-700 transition-colors hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isAutofilling ? "Buscando…" : "Autocompletar desde ISBN"}
          </button>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <input
            className="rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            placeholder="Título"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <input
            className="rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            placeholder="Autor"
            value={author}
            onChange={(e) => setAuthor(e.target.value)}
          />
        </div>

        {coverUrl && (
          <p className="text-xs text-zinc-400">Portada encontrada, se guardará junto con el libro.</p>
        )}

        <button
          type="submit"
          disabled={isSaving}
          className="w-fit rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSaving ? "Guardando…" : "Guardar"}
        </button>
      </form>

      <div className="mb-8 grid gap-4 rounded-xl border border-zinc-200 bg-white p-4 shadow-sm md:grid-cols-[1fr_1fr_180px_auto]">
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Título
          <input
            className="rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            placeholder="Ej. Cien años de soledad"
            value={titleFilter}
            onChange={(e) => setTitleFilter(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && loadBooks()}
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Autor
          <input
            className="rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            placeholder="Ej. Gabriel García Márquez"
            value={authorFilter}
            onChange={(e) => setAuthorFilter(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && loadBooks()}
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium text-zinc-700">
          Estado
          <select
          className="rounded-lg border border-zinc-300 bg-white px-3 py-2 text-zinc-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
          value={statusFilter} 
          onChange={(e) => setStatusFilter(e.target.value as BookStatus | "")}
          >
            <option value="">Todos los estados</option>
            {statusOptions.map((s) => (
              <option key={s.label} value={s.value}>
                {s.label}
              </option>
            ))}
          </select>
          </label>
        <div className="flex items-end gap-2">
          <button 
           onClick={loadBooks}
           className="rounded-lg bg-indigo-600 px-4 py-2 font-medium text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
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
        
      </div>

      <div className="grid gap-10 lg:grid-cols-4 sm:grid-cols-3 justify-items-center">
        {books.map((book) => (
          <BookCard book={book} key={book.id}>
            <button
              onClick={() => handleDelete(book.id)}
              className="cursor-pointer rounded-md bg-red-50 px-2.5 py-1 text-xs font-medium text-red-700 hover:bg-red-100"
            >
              Eliminar
            </button>
          </BookCard>
        ))}
      </div>
    </div>
  );
}