import { NextResponse } from "next/server";
import { apiRouteError } from "@/lib/api-route-error";

export async function POST(req: Request) {
  const credentials = await req.json();
  const apiUrl = `${process.env.API_URL}auth/login`;


  try {
    const res = await fetch(apiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
      credentials: "include",
    });

    const responseText = await res.text();

    if (!res.ok) {
      return NextResponse.json(
        { message: "Identifiants invalides", details: responseText },
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