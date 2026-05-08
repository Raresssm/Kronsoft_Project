 "use client";

import React from "react";
 import Image from "next/image";
import { useState, FormEvent } from "react";

import Link from "next/link";
export default function Login() {
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [error, setError] = useState<string>("");
  //const [account, setTypeAccount]=useState<String>("");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();

    if (!email || !password) {
      setError("All fields are mandatory.");
      return;
    }




  };

  return (

   <div className="relative h-screen w-full bg-[#3c3834] overflow-hidden flex flex-row">
      
      
     <div className="absolute inset-0 w-full h-full z-0">
  <Image
    src="/agora_campus.jpg"
    alt="Agora"
    fill
    className="object-cover scale-110 blur-sm"
    priority
  />

  <div className="absolute inset-0 bg-black/40" />
</div>

<div className="relative z-10 w-full md:w-1/2 h-full flex flex-col items-center justify-center  mx-auto">
        <div className="mb-8 transition-transform hover:scale-105">
          <Image src="/logo.png" alt="Agora Logo" width={110} height={110} />
        </div>

      
        <div className="w-[380px] rounded-3xl bg-white/5 border border-white/20 shadow-2xl p-10 backdrop-blur-xl">
          <h2 className="text-center text-white/90 text-xl tracking-[0.2em] font-light mb-10">
            SIGN IN
          </h2>

          <form onSubmit={handleSubmit}>
        
            <div className="mb-6">
              <div className="flex items-center gap-3 border-b border-white/20 pb-2 focus-within:border-white transition-colors">
                <span className="text-white/50 text-lg">✉</span>
                <input
                  type="email"
                  placeholder="Email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full bg-transparent outline-none text-white placeholder-white/70 text-sm"
                />
              </div>
            </div>
            <div className="mb-8">
              <div className="flex items-center gap-3 border-b border-white/20 pb-2 focus-within:border-white transition-colors">
                <svg className="w-5 h-5 text-white/50" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M17 8h-1V6a4 4 0 10-8 0v2H7a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2V10a2 2 0 00-2-2zm-6 6.73V16a1 1 0 102 0v-1.27a2 2 0 10-2 0zM10 6a2 2 0 114 0v2h-4V6z" />
                </svg>
                <input
                  type="password"
                  placeholder="Password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="bg-transparent outline-none text-white w-full placeholder-white/70 text-sm"
                />
              </div> 
            </div>

            <div className="flex justify-between text-[11px] text-white/70 mb-8 font-light uppercase tracking-wider">
              <a href="#" className="hover:text-white transition-colors">Create Account</a>
              
       
              <a href="#" className="hover:text-white transition-colors">Forgot Password?</a>
            </div>
            {error && <p className="text-red-400 text-xs mb-4 text-center">{error}</p>}




            <button type="button" className="w-full py-4 rounded-xl bg-[#143b5d] hover:bg-[#1d5485] text-white text-xs font-bold tracking-[0.2em] transition-all active:scale-95 shadow-lg">
             
             
              <Link href="profile" className="hover:text-white transition-colors">
       LOG IN
       </Link> 
            </button>
          </form>
        </div>
      </div>
    </div>

  );
}
