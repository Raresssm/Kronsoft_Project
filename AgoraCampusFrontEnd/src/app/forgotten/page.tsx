"use client";

import Image from "next/image";
import dynamic from "next/dynamic";

const ForgotPasswordForm = dynamic(
  () => import("../components/ForgotPasswordForm").then((mod) => mod.ForgotPasswordForm),
  {
    ssr: false,
    loading: () => (
      <div className="h-[420px] w-full max-w-md rounded-3xl border border-white/20 bg-white/5" />
    ),
  },
);

export default function Forgotten() {
  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#3c3834] px-4">
      <div className="absolute inset-0">
        <Image
          src="/agora_campus.jpg"
          alt="Agora campus"
          fill
          className="scale-110 object-cover blur-sm"
          priority
        />
        <div className="absolute inset-0 bg-black/40" />
      </div>

      <div className="relative z-10 flex w-full justify-center">
        <ForgotPasswordForm />
      </div>
    </div>
  );
}
