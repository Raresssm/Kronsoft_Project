"use client";

import { useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { useAuth } from "@/contexts/AuthContext";

export function RedirectIfAuthenticated({
  children,
  to = "/feed",
}: {
  children: ReactNode;
  to?: string;
}) {
  const router = useRouter();
  const { status } = useAuth();

  useEffect(() => {
    if (status === "authenticated") {
      router.replace(to);
    }
  }, [status, router, to]);

  if (status === "loading" || status === "authenticated") {
    return null;
  }

  return <>{children}</>;
}
