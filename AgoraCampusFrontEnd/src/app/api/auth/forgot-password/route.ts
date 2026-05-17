import { NextResponse } from "next/server";
import { sendPasswordResetEmail } from "@/lib/keycloak-server";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as { email?: string };
    const email = body.email?.trim();

    if (!email) {
      return NextResponse.json({ message: "Email is required." }, { status: 400 });
    }

    const origin = request.headers.get("origin") ?? "http://localhost:3000";
    await sendPasswordResetEmail(email, `${origin}/login`);

    return NextResponse.json({
      message:
        "If an account exists for that email, Keycloak has sent password reset instructions.",
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Could not send reset email.";
    return NextResponse.json({ message }, { status: 500 });
  }
}
