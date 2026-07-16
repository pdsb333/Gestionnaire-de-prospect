"use client"

import type { ReactNode } from "react"
import { SidebarProvider, SidebarTrigger, SidebarInset } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { GDPProvider } from "@/components/gdp-provider"
import { GlobalStatusBanner } from "@/components/global-status-banner"
import { Separator } from "@/components/ui/separator"

export function AppLayout({ children }: { children: ReactNode }) {
  return (
    <GDPProvider>
      <SidebarProvider>
        <AppSidebar />
        <SidebarInset>
          <header className="flex h-14 items-center gap-2 border-b border-border px-6">
            <SidebarTrigger className="-ml-2" />
            <Separator orientation="vertical" className="mr-2 !h-4" />
            <span className="text-sm text-muted-foreground font-medium">Gestion De Prospection</span>
          </header>
          <main className="flex-1 overflow-x-hidden">
            <GlobalStatusBanner />
            {children}
          </main>
        </SidebarInset>
      </SidebarProvider>
    </GDPProvider>
  )
}
