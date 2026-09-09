import { apiClient } from "@/lib/api/client";
import type {
  CreateReservationRequest,
  ReservationResponse,
} from "@/types/reservation";

export function createReservation(
  payload: CreateReservationRequest,
): Promise<ReservationResponse> {
  return apiClient<ReservationResponse>("/reservations", {
    method: "POST",
    body: payload,
  });
}

export function cancelReservation(id: number): Promise<void> {
  return apiClient<void>(`/reservations/${id}`, { method: "DELETE" });
}

export function findMyReservations(): Promise<ReservationResponse[]> {
  return apiClient<ReservationResponse[]>("/reservations/mine");
}
