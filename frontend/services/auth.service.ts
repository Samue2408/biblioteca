import { apiClient } from "@/lib/api/client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "@/types/auth";

export function login(payload: LoginRequest): Promise<AuthResponse> {
  return apiClient<AuthResponse>("/auth/login", {
    method: "POST",
    body: payload,
    auth: false,
  });
}

export function register(payload: RegisterRequest): Promise<AuthResponse> {
  return apiClient<AuthResponse>("/auth/register", {
    method: "POST",
    body: payload,
    auth: false,
  });
}

export async function getMe(): Promise<AuthResponse> {
  return apiClient<AuthResponse>("/auth/me", {
    method: "GET",
    auth: true
  });
}
