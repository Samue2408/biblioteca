"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import { GuestOnly } from "@/components/auth/GuestOnly";
import { useAuth } from "@/context/AuthProvider";
import { ApiException } from "@/lib/api/errors";

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login({ email, password });
      router.replace("/");
    } catch (caught) {
      setError(
        caught instanceof ApiException
          ? caught.message
          : "No se pudo iniciar sesión",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <GuestOnly>
    <main className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center px-4 py-12">
      <h1 className="mb-6 text-2xl font-semibold">Iniciar sesión</h1>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <label className="flex flex-col gap-1 text-sm">
          Correo
          <input
            type="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            className="rounded border border-zinc-300 bg-transparent px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          Contraseña
          <input
            type="password"
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="rounded border border-zinc-300 bg-transparent px-3 py-2"
          />
        </label>
        {error ? <p className="text-sm text-red-600">{error}</p> : null}
        <button
          type="submit"
          disabled={submitting}
          className="rounded bg-zinc-900 px-4 py-2 text-white disabled:opacity-60 dark:bg-zinc-100 dark:text-zinc-900"
        >
          {submitting ? "Entrando..." : "Entrar"}
        </button>
      </form>
      <p className="mt-4 text-sm">
        ¿No tienes cuenta?{" "}
        <Link href="/register" className="underline">
          Regístrate
        </Link>
      </p>
    </main>
    </GuestOnly>
  );
}
