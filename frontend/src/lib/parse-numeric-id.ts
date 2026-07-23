import { NextResponse } from "next/server";

// Route params here are always meant to be numeric entity ids forwarded straight into the
// backend fetch URL; rejecting anything non-numeric up front keeps malformed/crafted path
// segments from ever reaching that URL construction.
export function parseNumericId(raw: string): number | NextResponse {
  if (!/^\d+$/.test(raw)) {
    return NextResponse.json({ message: "Identifiant invalide" }, { status: 400 });
  }
  return Number(raw);
}
