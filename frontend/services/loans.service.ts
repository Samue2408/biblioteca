import { apiClient } from "@/lib/api/client";
import type { CreateLoanRequest, LoanResponse } from "@/types/loan";

export function createLoan(payload: CreateLoanRequest): Promise<LoanResponse> {
  return apiClient<LoanResponse>("/loans", {
    method: "POST",
    body: payload,
  });
}

export function returnLoan(id: number): Promise<LoanResponse> {
  return apiClient<LoanResponse>(`/loans/${id}/return`, { method: "PUT" });
}

export function findMyLoans(): Promise<LoanResponse[]> {
  return apiClient<LoanResponse[]>("/loans/mine");
}
