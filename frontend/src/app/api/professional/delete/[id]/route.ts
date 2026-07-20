import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { apiRouteError } from "@/lib/api-route-error";

export async function DELETE(_req: Request,
    { params }: { params: Promise<{ id: string }> }) {
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
        const apiUrl = `${process.env.API_URL}professionals/${id}`;
        const res = await fetch(apiUrl, {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${token}`,
            },
            cache: "no-store",
        })
        if (!res.ok) {
            const errorText = await res.text();
            let errorData;
            try {
                errorData = JSON.parse(errorText);
            } catch {
                errorData = { message: errorText };
            }

            return NextResponse.json(
                { message: errorData.message || "Erreur backend", details: errorData },
                { status: res.status }
            );
        }
        return new NextResponse(null, { status: 204 });

    } catch (err) {
        return apiRouteError(err);
    }

}


