import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { apiRouteError } from "@/lib/api-route-error";
import { parseNumericId } from "@/lib/parse-numeric-id";
import { assertSameOrigin } from "@/lib/assert-same-origin";

export async function DELETE(req: Request,
    { params }: { params: Promise<{ id: string }> }) {
    const originError = assertSameOrigin(req);
    if (originError) {
        return originError;
    }

    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;
    const { id } = await params;

    if (!token) {
        return NextResponse.json(
            { message: "Non authentifié" },
            { status: 401 }
        );
    }

    const professionalId = parseNumericId(id);
    if (typeof professionalId !== "number") {
        return professionalId;
    }

    try {
        const apiUrl = `${process.env.API_URL}professionals/${professionalId}`;
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


