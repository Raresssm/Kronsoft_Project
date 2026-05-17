import { NextResponse } from "next/server";
import { loginWithPassword } from "@/lib/keycloak-server";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as { email?: string; password?: string };
    const email = body.email?.trim();
    const password = body.password;

    if (!email || !password) {
      return NextResponse.json({ message: "Email and password are required." }, { status: 400 });
    }

    const tokens = await loginWithPassword(email, password);
    return NextResponse.json(tokens);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Login failed.";
    const status = /invalid|credentials|disabled|not found/i.test(message) ? 401 : 500;
    return NextResponse.json({ message }, { status });
  }
}
