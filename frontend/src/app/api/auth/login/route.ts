import { NextResponse } from "next/server";
import { apiRouteError } from "@/lib/api-route-error";
import { assertSameOrigin } from "@/lib/assert-same-origin";

export async function POST(req: Request) {
  const originError = assertSameOrigin(req);
  if (originError) {
    return originError;
  }

  const credentials = await req.json();
  const apiUrl = `${process.env.API_URL}auth/login`;


  try {
    const res = await fetch(apiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
      credentials: "include",
    });

    if (!res.ok) {
      const text = await res.text();
      let error;

      try {
        error = JSON.parse(text);
      } catch {
        error = { message: text };
      }

      return NextResponse.json(
        { message: error.message || "Erreur backend", details: error },
        { status: res.status }
      );
    }

    const setCookieHeader = res.headers.get("set-cookie");

    const response = NextResponse.json({
      message: "Connexion réussie"
    });

    if (setCookieHeader) {
      response.headers.set("Set-Cookie", setCookieHeader);
    }
    return response;

  } catch (err) {
    return apiRouteError(err);
  }
}