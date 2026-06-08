import { NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function PUT(req: Request, { params }: { params: Promise<{ id: string }> }) {
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

  try {
    const res = await fetch(`${process.env.API_URL}professionals/${id}`, {
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

    return NextResponse.json(await res.json());
  } catch (err) {
    return err
  }
}
