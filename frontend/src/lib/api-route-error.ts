import { NextResponse } from "next/server";

export function apiRouteError(err: unknown) {
  console.error(err);
  return NextResponse.json({ message: "Erreur serveur" }, { status: 500 });
}
