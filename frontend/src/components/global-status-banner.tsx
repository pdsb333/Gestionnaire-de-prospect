"use client"

import { useGDP } from "@/lib/store"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2 } from "lucide-react"

export function GlobalStatusBanner() {
  const { loading, error } = useGDP()

  if (error) {
    return (
      <div className="px-6 pt-4">
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="flex items-center gap-2 px-6 pt-4 text-sm text-muted-foreground">
        <Loader2 className="h-4 w-4 animate-spin" />
        Chargement…
      </div>
    )
  }

  return null
}
