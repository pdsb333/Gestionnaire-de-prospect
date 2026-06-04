import { NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function POST(req: Request, { params }: { params: Promise<{ businessId: string }> }) {
  const payload = await req.json();

  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  const { businessId } = await params;

  if (!token) {
    return NextResponse.json(
      { message: "Non authentifié" },
      { status: 401 }
    );
  }

  try {
    const res = await fetch(`${process.env.API_URL}professionals/${businessId}`, {
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

    return NextResponse.json(await res.json());
  } catch (err) {
    return err
  }
}
