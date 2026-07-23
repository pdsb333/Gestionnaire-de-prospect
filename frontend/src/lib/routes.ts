// Single source of truth for which routes require an authenticated session, shared between
// proxy.ts (redirect-based UX guard) and GDPProvider (skips the initial data fetch on public
// pages, where it's guaranteed to fail unauthenticated).
export const protectedRoutes = ["/dashboard", "/businesses", "/contacts", "/relaunches"]

export function isProtectedRoute(pathname: string): boolean {
  return protectedRoutes.some((r) => pathname.startsWith(r))
}
