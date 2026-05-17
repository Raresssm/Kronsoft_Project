"use client";

import Link from "next/link";
import Image from "next/image";


function PasswordReset() {
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
         RESET PASSWORD
        </h2>


        <h3 className="text-center text-[16px] text-gray-200 text-sm mb-6">
      You can set now the new password
        </h3>

   
        <form>
    < div className="flex flex-col gap-5 mb-8">
              <div className="flex items-center gap-3 border-b border-white/20 pb-2 focus-within:border-white transition-colors">
                <svg className="w-5 h-5 text-white/50" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M17 8h-1V6a4 4 0 10-8 0v2H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V10a2 2 0 00-2-2zm-6 6.73V16a1 1 0 102 0v-1.27a2 2 0 10-2 0zM10 6a2 2 0 114 0v2h-4V6z" />
                </svg>
                <input
                  type="password"
                  placeholder="Password"
                  className="bg-transparent outline-none text-white w-full placeholder-white/70 text-sm"
                />
              </div> 
              <div className="flex items-center gap-3 border-b border-white/20 pb-2 focus-within:border-white transition-colors">
                <svg className="w-5 h-5 text-white/50" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M17 8h-1V6a4 4 0 10-8 0v2H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V10a2 2 0 00-2-2zm-6 6.73V16a1 1 0 102 0v-1.27a2 2 0 10-2 0zM10 6a2 2 0 114 0v2h-4V6z" />
                </svg>
                <input
                  type="password"
                  placeholder="Confirm Password"
                  className="bg-transparent outline-none text-white w-full placeholder-white/70 text-sm"
                />
              </div> 
            </div>
<Link href="/password_reset"> 
          <button
            type="submit"
            className="w-full py-4 rounded-xl bg-[#143b5d] hover:bg-[#1d5485] text-white text-xs font-bold tracking-[0.2em] transition-all active:scale-95 shadow-lg"
          >
         RESET
          </button>
          </Link>
        </form>
      </div>
    </div>
  );
}

export default PasswordReset;