"use client";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

export function LoginForm() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!email.trim() || !password.trim()) {
      setError("All fields are mandatory.");
      return;
    }

    setError("");
    router.push("/feed");
  };

  return (
    <div className="w-full max-w-md mx-auto rounded-[1.75rem] border border-white/20 bg-white/5 p-6 shadow-2xl backdrop-blur-xl sm:p-8">
          <div className="max-w-md flex flex-col items-center text-white animate-fadeIn">
        <Image src="/logo.png" alt="Agora logo" width={90} height={90} />
      </div>
      <h1 className="text-center text-xl font-light tracking-[0.2em] text-white/90">
        SIGN IN
      </h1>

      <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
        <label className="block">
          <span className="sr-only">Email</span>
          <div className="flex items-center gap-3 border-b border-white/20 pb-2 text-white/50 transition focus-within:border-white">
            <span className="text-lg text-white/50">✉</span>
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              spellCheck={false}
              className="w-full bg-transparent text-sm text-white outline-none placeholder:text-white/70"
            />
          </div>
        </label>

        <label className="block">
          <span className="sr-only">Password</span>
          <div className="flex items-center gap-3 border-b border-white/20 pb-2 text-white/50 transition focus-within:border-white">
            <svg className="h-5 w-5 shrink-0" fill="currentColor" viewBox="0 0 24 24">
              <path d="M17 8h-1V6a4 4 0 10-8 0v2H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V10a2 2 0 00-2-2zm-6 6.73V16a1 1 0 102 0v-1.27a2 2 0 10-2 0zM10 6a2 2 0 114 0v2h-4V6z" />
            </svg>
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              spellCheck={false}
              className="w-full bg-transparent text-sm text-white outline-none placeholder:text-white/70"
            />
          </div>
        </label>

        <div className="flex items-center justify-between gap-4 text-[11px] uppercase tracking-wider text-white/70">
          <Link href="/" className="transition hover:text-white">
            Create Account
          </Link>
          <a href="/forgotten" className="transition hover:text-white">
            Forgot Password?
          </a>
        </div>

        {error && <p className="text-center text-xs text-red-400">{error}</p>}

        <button
          type="submit"
          className="w-full rounded-xl bg-[#143b5d] py-4 text-xs font-bold tracking-[0.2em] text-white shadow-lg transition-all hover:bg-[#1d5485] active:scale-95"
        >
          LOG IN
        </button>
      </form>
    </div>
  );
}
