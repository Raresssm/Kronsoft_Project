"use client";

import type { ReactNode } from "react";
import { AuthProvider as AuthContextProvider } from "@/contexts/AuthContext";

export function AuthProvider({ children }: { children: ReactNode }) {
  return <AuthContextProvider>{children}</AuthContextProvider>;
}
