"use client";

import { useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { useAuth } from "@/context/AuthProvider";
import type { Role } from "@/types/auth";

export function RequireRole({
  role,
  children,
}: {
  role: Role;
  children: ReactNode;
}) {
  const router = useRouter();
  const { isReady, user } = useAuth();
  const allowed = user?.role === role;

  useEffect(() => {
    if (isReady && !allowed) {
      router.replace("/");
    }
  }, [isReady, allowed, router]);

  if (!isReady || !allowed) {
    return <p className="p-4">Cargando...</p>;
  }

  return children;
}
