import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import { apiRouteError } from "@/lib/api-route-error";
import { assertSameOrigin } from "@/lib/assert-same-origin";

export async function PUT(req: Request) {
  const originError = assertSameOrigin(req);
  if (originError) {
    return originError;
  }

  const payload = await req.json();

  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    return NextResponse.json(
      { message: "Non authentifié" },
      { status: 401 }
    );
  }

  try {
    const res = await fetch(`${process.env.API_URL}user`, {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
      cache: "no-store",
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
        {
          message: error.message || "Erreur backend",
          details: error,
        },
        { status: res.status }
      );
    }

    const data = await res.json();
    const response = NextResponse.json(data, { status: res.status });

    // Le backend réémet le cookie JWT quand l'email (subject du token) change — le relayer ici
    // comme pour login, sinon la session reste sur l'ancien token désormais invalide.
    const setCookieHeader = res.headers.get("set-cookie");
    if (setCookieHeader) {
      response.headers.set("Set-Cookie", setCookieHeader);
    }

    return response;
  } catch (err) {
    return apiRouteError(err);
  }
}
