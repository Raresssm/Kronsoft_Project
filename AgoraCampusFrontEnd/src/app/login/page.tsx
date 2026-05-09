"use client";

import Image from "next/image";
import dynamic from "next/dynamic";

const LoginForm = dynamic(() => import("../components/LoginForm").then((mod) => mod.LoginForm), {
  ssr: false,
  loading: () => (
    <div className="w-full max-w-md rounded-[1.75rem] border border-white/20 bg-white/5 p-6 shadow-2xl backdrop-blur-xl sm:p-8">
      <div className="h-[320px]" />
    </div>
  ),
});

export default function Login() {
  return (
    <div className="relative min-h-screen overflow-hidden bg-[#3c3834]">
      <div className="absolute inset-0">
        <Image
          src="/agora_campus.jpg"
          alt="Agora campus"
          fill
          className="object-cover scale-110 blur-sm"
          priority
        />
        <div className="absolute inset-0 bg-black/40" />
      </div>

      <div className="relative z-10 mx-auto grid min-h-screen w-full max-w-6xl gap-6 px-4 py-6 sm:px-6 ">
{/*lg:grid-cols-[minmax(0,1fr)_420px] lg:px-8*/}
        <div className="flex items-center justify-center">
          <LoginForm />
        </div>
      </div>
    </div>
  );
}
