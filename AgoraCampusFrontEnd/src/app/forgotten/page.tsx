"use client";

import Link from "next/link";
import Image from "next/image";


function Forgotten() {
  return (
    <div className="relative min-h-screen flex items-center justify-center overflow-hidden bg-[#3c3834] px-4">

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

      <div className="relative w-full max-w-md rounded-3xl bg-white/5 border border-white/20 shadow-2xl p-8 sm:p-10 backdrop-blur-xl flex flex-col justify-center">
        
        <div className="relative w-24 h-24 sm:w-28 sm:h-28 mx-auto mb-8 transition-transform animate-fadeIn">
          <Image
            src="/logo.png"
            alt="Agora Logo"
            fill
            className="object-contain"
          />
        </div>
   
        <h2 className="text-center text-white/90 text-lg sm:text-xl tracking-[0.2em] font-light mb-8">
          FORGOTTEN PASSWORD
        </h2>


        <h3 className="text-center text-[16px] text-white/90 text-sm mb-6">
      Enter your email address to receive an MFA code.
        </h3>

   
        <form>
          <div className="mb-6">
            <div className="flex items-center gap-3 border-b border-white/20 pb-2 focus-within:border-white transition-colors">
              <span className="text-white/50 text-lg">✉</span>
              <input
                type="email"
                placeholder="Email"
                className="w-full bg-transparent outline-none text-white/80 placeholder-white/50 text-sm"
              />
            </div>
          </div>

   
          <div className="flex justify-between text-[11px] text-white/70 mb-8 font-light uppercase tracking-wider">
            <Link href="/login" className="hover:text-white transition-colors">
              Sign In?
            </Link>
            <Link href="/" className="hover:text-white transition-colors">
              Sign Up?
            </Link>
          </div>

<Link href="/mfa_authentication"> 
          <button
            type="submit"
            className="w-full py-4 rounded-xl bg-[#143b5d] hover:bg-[#1d5485] text-white text-xs font-bold tracking-[0.2em] transition-all active:scale-95 shadow-lg"
          >
            SEND
          </button>
          </Link>
        </form>
      </div>
    </div>
  );
}

export default Forgotten;