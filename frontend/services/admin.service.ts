import { apiClient } from "@/lib/api/client";
import type { AdminStatsResponse, BlockedUser } from "@/types/admin";

export function getAdminStats(): Promise<AdminStatsResponse> {
  return apiClient<AdminStatsResponse>("/admin/stats");
}

export async function getBlockedUsers(): Promise<BlockedUser[]> {
  return apiClient<BlockedUser[]>("/admin/users/blocked",{
    method: "GET"
  });
}

export async function unblockUser(id: number): Promise<void> {
  return apiClient<void>(`/admin/users/${id}/unblock`, {
    method: "PUT"
  });
}