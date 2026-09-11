import type { BlockedUser } from "@/types/admin";

type BlockedUsersTableProps = {
  users: BlockedUser[];
  onUnblock: (id: number) => void;
};

function formatBlockedUntil(iso: string): string {
  const date = new Date(iso);
  return date.toLocaleDateString("es-ES", {
    day: "numeric",
    month: "long",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function BlockedUsersTable({ users, onUnblock }: BlockedUsersTableProps) {
  if (users.length === 0) {
    return (
      <div className="rounded-md border border-dashed border-stone-200 px-6 py-10 text-center">
        <p className="text-sm text-stone-500">No hay cuentas bloqueadas en este momento.</p>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-md border border-stone-200">
      <table className="w-full text-left text-sm">
        <thead>
          <tr className="border-b border-stone-200 text-stone-500">
            <th className="px-4 py-2.5 font-medium">Nombre</th>
            <th className="px-4 py-2.5 font-medium">Correo</th>
            <th className="px-4 py-2.5 font-medium">Bloqueado hasta</th>
            <th className="px-4 py-2.5"></th>
          </tr>
        </thead>
        <tbody className="divide-y divide-stone-100">
          {users.map((user) => (
            <tr key={user.id}>
              <td className="px-4 py-3 text-stone-800">{user.name}</td>
              <td className="px-4 py-3 text-stone-500">{user.email}</td>
              <td className="px-4 py-3 text-stone-800">{formatBlockedUntil(user.blockedUntil)}</td>
              <td className="px-4 py-3 text-right">
                <button
                  onClick={() => onUnblock(user.id)}
                  className="rounded border border-stone-200 px-2.5 py-1 text-xs text-stone-600 hover:border-stone-300 hover:bg-stone-50"
                >
                  Desbloquear
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}