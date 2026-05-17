"use client";

import Image from "next/image";
import Link from "next/link";
import { FormEvent, useState } from "react";

export function ForgotPasswordForm() {
  const [email, setEmail] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!email.trim()) {
      setError("Email is required.");
      return;
    }

    setError("");
    setSuccess("");
    setPending(true);

    try {
      const response = await fetch("/api/auth/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim() }),
      });

      const body = (await response.json()) as { message?: string };

      if (!response.ok) {
        throw new Error(body.message ?? "Could not send reset email.");
      }

      setSuccess(
        body.message ??
          "If an account exists for that email, check your inbox for reset instructions.",
      );
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "Request failed.");
    } finally {
      setPending(false);
    }
  };

  return (
    <div className="relative w-full max-w-md rounded-3xl border border-white/20 bg-white/5 p-8 shadow-2xl backdrop-blur-xl sm:p-10">
      <div className="relative mx-auto mb-8 h-24 w-24 sm:h-28 sm:w-28">
        <Image src="/logo.png" alt="Agora Logo" fill className="object-contain" />
      </div>

      <h2 className="mb-2 text-center text-lg font-light tracking-[0.2em] text-white/90 sm:text-xl">
        FORGOTTEN PASSWORD
      </h2>
      <p className="mb-6 text-center text-sm text-white/70">
        Enter your email. Keycloak will send a link to choose a new password (requires SMTP in
        dev/production).
      </p>

      <form onSubmit={(event) => void handleSubmit(event)}>
        <label className="mb-6 block">
          <span className="sr-only">Email</span>
          <div className="flex items-center gap-3 border-b border-white/20 pb-2 transition-colors focus-within:border-white">
            <span className="text-lg text-white/50">✉</span>
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              className="w-full bg-transparent text-sm text-white/80 outline-none placeholder:text-white/50"
            />
          </div>
        </label>

        <div className="mb-6 flex justify-between text-[11px] font-light uppercase tracking-wider text-white/70">
          <Link href="/login" className="transition-colors hover:text-white">
            Sign In?
          </Link>
          <Link href="/" className="transition-colors hover:text-white">
            Sign Up?
          </Link>
        </div>

        {error && <p className="mb-4 text-center text-xs text-red-400">{error}</p>}
        {success && <p className="mb-4 text-center text-xs text-emerald-300">{success}</p>}

        <button
          type="submit"
          disabled={pending}
          className="w-full rounded-xl bg-[#143b5d] py-4 text-xs font-bold tracking-[0.2em] text-white shadow-lg transition-all hover:bg-[#1d5485] active:scale-95 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {pending ? "SENDING…" : "SEND RESET LINK"}
        </button>
      </form>
    </div>
  );
}
