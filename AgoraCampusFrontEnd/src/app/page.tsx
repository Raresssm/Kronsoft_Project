"use client";

import dynamic from "next/dynamic";

const HomeClient = dynamic(() => import("./home-client"), { ssr: false });

export default function Page() {
  return <HomeClient />;
}
