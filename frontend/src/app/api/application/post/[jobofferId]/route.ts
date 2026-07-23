import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import { apiRouteError } from "@/lib/api-route-error";
import { parseNumericId } from "@/lib/parse-numeric-id";

export async function POST(req: Request, { params }: { params: Promise<{ jobofferId: string }> }) {
  const payload = await req.json();

  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const { jobofferId } = await params;

  if (!token) {
    return NextResponse.json(
      { message: "Non authentifié" },
      { status: 401 }
    );
  }

  const id = parseNumericId(jobofferId);
  if (typeof id !== "number") {
    return id;
  }

  try {
    const res = await fetch(`${process.env.API_URL}application/${id}`, {
      method: "POST",
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
