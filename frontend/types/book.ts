export type BookStatus =
  | "DISPONIBLE"
  | "PRESTADO"
  | "RESERVADO"
  | "ELIMINADO";

export type BookResponse = {
  id: number;
  title: string;
  author: string;
  isbn: string;
  publicationYear: number | null;
  status: BookStatus | string;
  coverUrl: string | null;
};

export type CreateBookRequest = {
  isbn: string;
  title?: string;
  author?: string;
  publicationYear?: number | null;
};

export type BookLookupResponse = {
  title: string | null;
  author: string | null;
  publicationYear: number | null;
  coverUrl: string | null;
};

export type BookFilters = {
  title?: string;
  author?: string;
  status?: BookStatus;
};
