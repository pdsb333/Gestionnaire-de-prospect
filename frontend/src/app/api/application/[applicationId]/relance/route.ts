import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import { apiRouteError } from "@/lib/api-route-error";

export async function POST(req: Request, { params }: { params: Promise<{ applicationId: string }> }) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const { applicationId } = await params;

  if (!token) {
    return NextResponse.json(
      { message: "Non authentifié" },
      { status: 401 }
    );
  }

  try {
    const res = await fetch(`${process.env.API_URL}application/${applicationId}/relance`, {
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

    return NextResponse.json(await res.json());
  } catch (err) {
    return apiRouteError(err);
  }
}