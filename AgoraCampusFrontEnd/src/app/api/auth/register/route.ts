import { NextResponse } from "next/server";
import { loginWithPassword, registerUser } from "@/lib/keycloak-server";

export async function POST(request: Request) {
  try {
    const body = (await request.json()) as {
      email?: string;
      password?: string;
      accountType?: string;
      firstName?: string;
      lastName?: string;
    };
    const email = body.email?.trim();
    const password = body.password;
    const accountType = body.accountType?.trim();
    const firstName = body.firstName?.trim();
    const lastName = body.lastName?.trim();

    if (!email || !password) {
      return NextResponse.json({ message: "Email and password are required." }, { status: 400 });
    }

    if (password.length < 8) {
      return NextResponse.json(
        { message: "Password must be at least 8 characters." },
        { status: 400 },
      );
    }

    await registerUser({ email, password, accountType, firstName, lastName });
    const tokens = await loginWithPassword(email, password);
    return NextResponse.json(tokens, { status: 201 });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Registration failed.";
    const status = /already exists/i.test(message)
      ? 409
      : /password/i.test(message)
        ? 400
        : 500;
    return NextResponse.json({ message }, { status });
  }
}
