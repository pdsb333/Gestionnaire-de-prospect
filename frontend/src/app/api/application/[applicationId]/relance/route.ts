import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import { apiRouteError } from "@/lib/api-route-error";
import { parseNumericId } from "@/lib/parse-numeric-id";
import { assertSameOrigin } from "@/lib/assert-same-origin";

export async function POST(req: Request, { params }: { params: Promise<{ applicationId: string }> }) {
  const originError = assertSameOrigin(req);
  if (originError) {
    return originError;
  }

  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const { applicationId } = await params;

  if (!token) {
    return NextResponse.json(
      { message: "Non authentifié" },
      { status: 401 }
    );
  }

  const id = parseNumericId(applicationId);
  if (typeof id !== "number") {
    return id;
  }

  try {
    const res = await fetch(`${process.env.API_URL}application/${id}/relance`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
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

    return NextResponse.json(await res.json(), { status: res.status });
  } catch (err) {
    return apiRouteError(err);
  }
}