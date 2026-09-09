"use client";

import { useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { useAuth } from "@/context/AuthProvider";

export function GuestOnly({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { isReady, isAuthenticated } = useAuth();

  useEffect(() => {
    if (isReady && isAuthenticated) {
      router.replace("/");
    }
  }, [isReady, isAuthenticated, router]);

  if (!isReady) {
    return <p className="p-4">Cargando...</p>;
  }

  if (isAuthenticated) {
    return <p className="p-4">Cargando...</p>;
  }

  return children;
}
