import { NextResponse } from "next/server";

// Next.js route handlers have no built-in CSRF protection (unlike React Server Actions) — a
// forged cross-site request would ride the browser's ambient `token` cookie into these BFF
// mutation endpoints just like a legitimate same-origin call. Comparing the Origin header
// against the request's own Host is the standard mitigation for hand-written fetch-based
// handlers like these.
export function assertSameOrigin(req: Request): NextResponse | null {
  const origin = req.headers.get("origin");
  if (!origin) {
    // Non-browser clients and some older browsers omit Origin on same-origin requests; failing
    // open here avoids breaking those, same-origin is still enforced whenever it IS present.
    return null;
  }

  const host = req.headers.get("host");
  let originHost: string;
  try {
    originHost = new URL(origin).host;
  } catch {
    return NextResponse.json({ message: "Origine non autorisée" }, { status: 403 });
  }

  if (originHost !== host) {
    return NextResponse.json({ message: "Origine non autorisée" }, { status: 403 });
  }

  return null;
}
