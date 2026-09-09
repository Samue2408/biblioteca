import { ApiException } from "@/lib/api/errors";
import { getAdminStats } from "@/services/admin.service";
import type { Role } from "@/types/auth";

export async function resolveRole(): Promise<Role | "UNAUTHENTICATED"> {
  try {
    await getAdminStats();
    return "ADMIN";
  } catch (error) {
    if (error instanceof ApiException && error.status === 401) {
      return "UNAUTHENTICATED";
    }
    return "BIBLIOTECARIO";
  }
}
