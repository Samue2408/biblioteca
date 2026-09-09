import { apiClient } from "@/lib/api/client";
import type {
  BookFilters,
  BookLookupResponse,
  BookResponse,
  CreateBookRequest,
} from "@/types/book";

export function findBooks(filters: BookFilters = {}): Promise<BookResponse[]> {
  return apiClient<BookResponse[]>("/books", {
    query: {
      title: filters.title,
      author: filters.author,
      status: filters.status,
    },
  });
}

export function createBook(payload: CreateBookRequest): Promise<BookResponse> {
  return apiClient<BookResponse>("/books", {
    method: "POST",
    body: payload,
  });
}

export function deleteBook(id: number): Promise<void> {
  return apiClient<void>(`/books/${id}`, { method: "DELETE" });
}

export function restoreBook(id: number): Promise<void> {
  return apiClient<void>(`/books/${id}/restore`, { method: "PUT" });
}

export function lookupBookByIsbn(isbn: string): Promise<BookLookupResponse> {
  return apiClient<BookLookupResponse>(`/books/lookup/${encodeURIComponent(isbn)}`);
}
