"use client";

import Image from "next/image";
import dynamic from "next/dynamic";

const CreateAccountForm = dynamic(
  () => import("./components/CreateAccountForm").then((mod) => mod.CreateAccountForm),
  {
    ssr: false,
    loading: () => (
      <div className="w-full max-w-md rounded-[1.75rem] border border-white/20 bg-white/5 p-6 shadow-2xl backdrop-blur-xl sm:p-8">
        <div className="h-[360px]" />
      </div>
    ),
  },
);

export default function Home() {
  return (
    <div className="relative min-h-screen overflow-hidden bg-[#3c3834]">
      
      <div className="absolute inset-0">
        <Image
          src="/agora_campus.jpg"
          alt="Agora campus"
          fill
          className="object-cover"
          priority
        />
        <div className="absolute inset-0 bg-gradient-to-r from-black/30 via-black/20 to-black/50" />
      </div>
  <h1>AGORA CAMPUS</h1>
      <div className="relative z-10 mx-auto grid min-h-screen w-full max-w-6xl gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[minmax(0,1fr)_420px] lg:px-8">
        <div className="hidden items-end pb-8 lg:flex">
          <div className="max-w-md flex flex-col items-center text-white animate-fadeIn">
  <Image src="/logo.png" alt="Agora logo" width={90} height={90} />

  <p className="mt-4 italic max-w-sm text-sm leading-6 text-white/80">
    Where dialogue becomes opportunity.
  </p>
</div>
        </div>

        <div className="flex items-center justify-center">
          <CreateAccountForm />
        </div>
      </div>
    </div>
  );
}
