import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { parseJwt } from "@/lib/parseJWT";
import { isProtectedRoute } from "@/lib/routes";

const authRoutes = ["/connexion", "/inscription"];

export default function proxy(req: NextRequest) {
    const token = req.cookies.get("token")?.value;
    const { pathname } = req.nextUrl;

    const isAuthRoute = authRoutes.some(r => pathname.startsWith(r));

    if (token) {
        const decoded = parseJwt(token);
        if (!decoded || decoded.exp < Date.now() / 1000) {
            const response = NextResponse.redirect(new URL("/connexion", req.url));
            response.cookies.delete("token");
            return response;
        }
    }

    if (isAuthRoute && token) {
        return NextResponse.redirect(new URL("/dashboard", req.url));
    }

    if (isProtectedRoute(pathname) && !token) {
        return NextResponse.redirect(new URL("/connexion", req.url));
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        "/dashboard/:path*",
        "/connexion",
        "/inscription",
        "/businesses/:path*",
        "/contacts/:path*",
        "/relaunches/:path*",
    ],
};