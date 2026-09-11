import { ApiException } from "@/lib/api/errors";
import { getMe } from "@/services/auth.service";
import type { Role } from "@/types/auth";

export async function resolveRole(): Promise<Role | "UNAUTHENTICATED"> {
  try {
    const response = await getMe();
    return response.role;
  } catch (error) {
    if (error instanceof ApiException && error.status === 401) {
      return "UNAUTHENTICATED";
    }
    throw error;
  }
}