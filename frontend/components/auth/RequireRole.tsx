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
  const { user, isReady, isAuthenticated } = useAuth();
  console.log(user?.role)
  const router = useRouter();

  useEffect(() => {
    if (!isReady) return;
    if (!isAuthenticated || user?.role !== role) {
      router.replace("/login");
    }
  }, [isReady, isAuthenticated, user, router]);

  if (!isReady || user?.role !== role) {
    return <p>Verificando acceso…</p>;
  }

  return <>{children}</>;
}
