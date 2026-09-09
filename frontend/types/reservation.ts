export type ReservationStatus =
  | "PENDIENTE"
  | "NOTIFICADO"
  | "CANCELADO"
  | "CUMPLIDO";

export type CreateReservationRequest = {
  bookId: number;
};

export type ReservationResponse = {
  id: number;
  bookTitle: string;
  position: number;
  status: ReservationStatus | string;
};
