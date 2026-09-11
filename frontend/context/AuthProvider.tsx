"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { getAccessToken, setAccessToken } from "@/lib/api/token";
import { getEmailFromToken } from "@/lib/auth/jwt";
import { resolveRole } from "@/lib/auth/role";
import { readStoredToken, writeStoredToken } from "@/lib/auth/storage";
import {
  login as loginRequest,
  register as registerRequest,
} from "@/services/auth.service";
import type { LoginRequest, RegisterRequest, Role } from "@/types/auth";

export type AuthUser = {
  email: string;
  role: Role;
};

type AuthContextValue = {
  user: AuthUser | null;
  isReady: boolean;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function applyToken(token: string | null): string | null {
  if (!token) {
    setAccessToken(null);
    writeStoredToken(null);
    return null;
  }

  const email = getEmailFromToken(token);
  if (!email) {
    setAccessToken(null);
    writeStoredToken(null);
    return null;
  }

  setAccessToken(token);
  writeStoredToken(token);
  return email;
}

async function toAuthUser(
  email: string,
  roleHint?: Role,
): Promise<AuthUser | null> {
  if (roleHint) {
    return { email, role: roleHint };
  }

  const role = await resolveRole();
  if (role === "UNAUTHENTICATED") {
    applyToken(null);
    return null;
  }

  return { email, role };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    
    async function restoreSession() {
      const email = applyToken(readStoredToken());
      if (!email) {
        setUser(null);
        setIsReady(true);
        return;
      }

      try {
        setUser(await toAuthUser(email));
      } catch {
        // No se pudo verificar la sesión (backend caído, error inesperado, etc).
        applyToken(null);
        setUser(null);
      } finally {
        setIsReady(true);
      }
    }

    void restoreSession();
  }, []);

  const login = useCallback(async (payload: LoginRequest) => {
    const response = await loginRequest(payload);
    const email = applyToken(response.token);
    if (!email) {
      setUser(null);
      return;
    }
    setUser(await toAuthUser(email, response.role));
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    const response = await registerRequest(payload);
    const email = applyToken(response.token);
    if (!email) {
      setUser(null);
      return;
    }
    setUser(await toAuthUser(email, response.role));
  }, []);

  const logout = useCallback(() => {
    applyToken(null);
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isReady,
      isAuthenticated: user !== null,
      login,
      register,
      logout,
    }),
    [user, isReady, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth debe usarse dentro de AuthProvider");
  }
  return context;
}
