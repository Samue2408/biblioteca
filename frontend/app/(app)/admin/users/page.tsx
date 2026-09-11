"use client";

import { useEffect, useState } from "react";
import { getBlockedUsers, unblockUser } from "@/services/admin.service";
import { BlockedUsersTable } from "@/components/admin/BlockedUsersTable";
import type { BlockedUser } from "@/types/admin";

export default function AdminUsersPage() {
  const [users, setUsers] = useState<BlockedUser[]>([]);

  async function load() {
    setUsers(await getBlockedUsers());
  }

  useEffect(() => {
    void load();
  }, []);

  async function handleUnblock(id: number) {
    await unblockUser(id);
    await load();
  }

  return (
    <div className="max-w-3xl">
      <header className="mb-8">
        <p className="mb-2 text-sm font-medium text-indigo-600">Biblioteca</p>
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-900">Cuentas bloqueadas</h1>
        <p className="mt-2 text-zinc-600">Cuentas suspendidas por atrasos y su fecha de desbloqueo</p>
      </header>
      <BlockedUsersTable users={users} onUnblock={handleUnblock} />
    </div>
  );
}