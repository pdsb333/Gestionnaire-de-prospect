"use client"

import type { LucideIcon } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { cn } from "@/lib/utils"

interface StatCardProps {
  label: string
  value: number | string
  icon: LucideIcon
  variant?: "default" | "urgent" | "warning"
  description?: string
}

export function StatCard({ label, value, icon: Icon, variant = "default", description }: StatCardProps) {
  return (
    <Card
      className={cn(
        "border transition-colors",
        variant === "urgent" && "border-foreground/30 bg-foreground/[0.03]",
        variant === "warning" && "border-foreground/15 bg-foreground/[0.02]"
      )}
    >
      <CardContent className="flex items-start justify-between p-5">
        <div className="flex flex-col gap-1">
          <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
            {label}
          </p>
          <p
            className={cn(
              "text-2xl font-bold tracking-tight",
              variant === "urgent" && "text-foreground",
              variant === "warning" && "text-foreground/80"
            )}
          >
            {value}
          </p>
          {description && (
            <p className="text-xs text-muted-foreground">{description}</p>
          )}
        </div>
        <div
          className={cn(
            "flex h-9 w-9 items-center justify-center rounded-md",
            variant === "urgent"
              ? "bg-foreground text-background"
              : variant === "warning"
              ? "bg-foreground/10 text-foreground"
              : "bg-muted text-muted-foreground"
          )}
        >
          <Icon className="h-4 w-4" />
        </div>
      </CardContent>
    </Card>
  )
}
