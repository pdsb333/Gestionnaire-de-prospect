import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { apiRouteError } from "@/lib/api-route-error";
import { assertSameOrigin } from "@/lib/assert-same-origin";

export async function DELETE(req: Request) {
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
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
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
                { message: errorData.message || "Erreur backend", details: errorData },
                { status: res.status }
            );
        }

        const response = new NextResponse(null, { status: 204 });

        // Le backend vide le cookie JWT à la suppression du compte — relayer comme pour logout.
        const setCookieHeader = res.headers.get("set-cookie");
        if (setCookieHeader) {
            response.headers.set("Set-Cookie", setCookieHeader);
        }

        return response;

    } catch (err) {
        return apiRouteError(err);
    }

}
