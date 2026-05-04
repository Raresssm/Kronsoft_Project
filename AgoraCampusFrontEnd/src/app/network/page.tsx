 "use client";
 import React from "react";
 import Image from "next/image";
import { useState, FormEvent } from "react";
import Link from "next/link";

export default function Network() {




  return (

   <div className="relative h-screen w-full bg-[white] overflowX-hidden flex flex-column">
  
      <Image
        src="/blur_cover.png"
        alt="profile"
        fill
        className="object-cover"
      />
       
      <div className="relative w-full h-[50px] bg-white/20 border border-white/20 shadow-2xl flex items-center px-4">
  <div className="relative w-[100px] h-full">
    <Image
      src="/logo.png"
      alt="Agora"
      fill
      className="object-contain"
      priority
    />
  </div>

<div className="relative w-[300px]">
  <input
    type="text"
    placeholder="Search..."
  className="w-full pl-10 pr-10 py-2 rounded-xl bg-white/30 border-b border-gray-300/60 text-black outline-none"
  />

  <div className="absolute left-3 top-1/2  cursor-pointer -translate-y-1/2 text-[#143b5d]">
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#143b5d"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-5 h-5"
  >
    <circle cx="11" cy="11" r="7" />
    <line x1="16.65" y1="16.65" x2="21" y2="21" />
  </svg>
    
  </div>


<Link href="/feed">
<div className="absolute left-100 top-1/2 cursor-pointer -translate-y-1/2 text-[#143b5d]">
    <svg 
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#143b5d"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-6 h-6 text-blue-500"
  >
    
    <path d="M3 10.5L12 3l9 7.5" />
    <path d="M5 9.5V21h14V9.5" />
    <path d="M10 21v-6h4v6" />
  </svg>
      <span className="text-xs mt-1">Home</span>
</div>
</Link>

<Link href="/messages">
<div className="absolute left-120 top-1/2  -translate-y-1/2 cursor-pointer text-[#143b5d]">

<svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#143b5d"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-6 h-6 text-blue-500"
  >
    <path d="M21 15a4 4 0 0 1-4 4H8l-5 3 2-4a4 4 0 0 1-2-3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
  </svg>
    <span className="text-xs mt-1">Messages</span>


</div>
</Link>

<Link href="/jobs">
<div className="absolute left-140 top-1/2 -translate-y-1/2   cursor-pointer text-[#143b5d]">

<svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#143b5d"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-6 h-6 text-blue-500"
  >
    <rect x="3" y="7" width="18" height="14" rx="2" ry="2" />
    <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    <path d="M3 13h18" />
  </svg>

   <span className="text-xs mt-1">Jobs</span>

</div>

</Link>
<Link href="/alerts">
<div className="absolute left-160 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">

 <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#143b5d"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-6 h-6 text-blue-500"
  >
    <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 7h18s-3 0-3-7" />
    <path d="M13.73 21a2 2 0 0 1-3.46 0" />
  </svg>
    <span className="text-xs mt-1">Alerts</span>
</div>
</Link>

<Link href="/network">
<div className="absolute left-180 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">

<svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#143b5d"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-6 h-6 text-blue-500"
  >
    <circle cx="9" cy="8" r="3" />
    <path d="M3 20c0-3.3 2.7-6 6-6" />
    <circle cx="17" cy="10" r="3" />
    <path d="M13 20c0-3.3 2.7-6 6-6" />
  </svg>
  <span className="text-xs mt-1">Network</span>
</div>
</Link>
<Link href="/individual_profile">
<div className="absolute left-200 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">

  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#143b5d"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-6 h-6"
  >
   
    <circle cx="12" cy="12" r="10" />


    <circle cx="12" cy="10" r="3" />

    <path d="M8 18c0-2.2 1.8-4 4-4s4 1.8 4 4" />
  </svg>

  <span className="text-xs mt-1">Profile</span>

</div>
</Link>
</div>

</div>
<div className="w-[700px] absolute mt-[10%] ml-[10%] rounded-3xl bg-white border border-white/20 shadow-2xl backdrop-blur-xl overflow-hidden">

  <div className="w-full h-[200px] relative group">

    <Image
      src="/image.png"
      alt="cover"
      fill
      className="object-cover"
    />


    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition flex items-center justify-center">
      <span className="text-white text-sm">Upload Cover</span>
    </div>

      <input
    type="file"
    accept="image/*"
    className="absolute inset-0 opacity-0 cursor-pointer"
  />
  </div>

  <div className="absolute left-10 top-[140px] w-[120px] h-[120px]">

    <div className="relative w-full h-full rounded-full border-4 border-white shadow-xl overflow-hidden bg-gray-600 group cursor-pointer">



      <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition flex items-center justify-center text-white text-xs">
        Edit photo
      </div>

    </div>

  
    <input
      type="file"
      className="absolute inset-0 opacity-0 cursor-pointer rounded-full"
    />

  </div>

  <div className="pt-[140px] p-6">
  
  </div>
          <form >
        
     
  
           
          </form>
        </div>
  
</div>

  
  );

};