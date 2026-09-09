import { apiClient } from "@/lib/api/client";
import type { AdminStatsResponse } from "@/types/admin";

export function getAdminStats(): Promise<AdminStatsResponse> {
  return apiClient<AdminStatsResponse>("/admin/stats");
}
