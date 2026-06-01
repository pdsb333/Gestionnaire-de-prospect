import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function GET() {

  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    return NextResponse.json("Accés refusé");
  }

  const apiUrl = `${process.env.API_URL}business`;

  try {
    const res = await fetch(apiUrl, {
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      cache: "no-store",
    });


    if (!res.ok) {
      const errorText = await res.text();

      let errorData;
      try {
        errorData = JSON.parse(errorText);
      } catch {
        errorData = { message: errorText };
      }

      return NextResponse.json(
        { message: errorData.message || "Accès refusé", details: errorData },
        { status: res.status }
      );
    }

    const data = await res.json();
    console.log("Runtime:", process.env.NEXT_RUNTIME);


    return NextResponse.json(data);
  } catch (err) {
    return err
  }
}