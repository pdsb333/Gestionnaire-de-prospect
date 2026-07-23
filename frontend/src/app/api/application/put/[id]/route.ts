import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import { apiRouteError } from "@/lib/api-route-error";
import { parseNumericId } from "@/lib/parse-numeric-id";
import { assertSameOrigin } from "@/lib/assert-same-origin";

export async function PUT(req: Request, { params }: { params: Promise<{ id: string }> }) {
  const originError = assertSameOrigin(req);
  if (originError) {
    return originError;
  }

  const payload = await req.json();

  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const { id } = await params;

  if (!token) {
    return NextResponse.json(
      { message: "Non authentifié" },
      { status: 401 }
    );
  }

  const applicationId = parseNumericId(id);
  if (typeof applicationId !== "number") {
    return applicationId;
  }

  try {
    const res = await fetch(`${process.env.API_URL}application/${applicationId}`, {
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

    return NextResponse.json(await res.json(), { status: res.status });
  } catch (err) {
    return apiRouteError(err);
  }
}
