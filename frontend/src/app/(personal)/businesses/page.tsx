"use client"

import Link from "next/link"
import { Building2, ChevronRight, FileText, Users, Briefcase } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { useGDP } from "@/lib/store"
import { apiClient } from "@/lib/api-client"
import { AddBusinessDialog } from "@/components/businesses/add-business-dialog"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

export default function BusinessesPage() {
  const { businesses } = useGDP()

  return (
    <div className="flex flex-col gap-8 p-6 lg:p-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Entreprises</h1>
          <p className="text-sm text-muted-foreground mt-1">
            {businesses.length} entreprise{businesses.length !== 1 ? "s" : ""} suivie{businesses.length !== 1 ? "s" : ""}
          </p>
        </div>
        <AddBusinessDialog />
      </div>

      {businesses.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted mb-4">
            <Building2 className="h-6 w-6 text-muted-foreground" />
          </div>
          <p className="text-sm font-medium">Aucune entreprise</p>
          <p className="text-xs text-muted-foreground mt-1">
            Ajoutez votre première entreprise pour commencer
          </p>
        </div>
      ) : (
        <div className="grid gap-3">
          {businesses.map((biz) => {
            // Offres avec une candidature en retard de relance
            const overdueCount = (biz.jobOffersList ?? []).filter((j) => {
              const app = j.application
              if (!app?.dateRelaunch) return false
              return app.dateRelaunch < new Date().toISOString().split("T")[0]
            }).length

            return (
              <Link key={biz.id} href={`/businesses/${biz.id}`}>
                <Card
                  className={cn(
                    "transition-colors hover:bg-muted/30",
                    overdueCount > 0 && "border-foreground/20"
                  )}
                >
                  <CardContent className="flex items-center justify-between p-5">
                    <div className="flex items-center gap-4 min-w-0">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-md bg-foreground text-background text-sm font-bold">
                        {biz.name?.charAt(0).toUpperCase() ?? "?"}
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="truncate text-sm font-semibold">{biz.name}</p>
                          {overdueCount > 0 && (
                            <Badge
                              variant="default"
                              className="bg-foreground text-background text-[10px] px-1.5"
                            >
                              {overdueCount} en retard
                            </Badge>
                          )}
                        </div>
                        <p className="truncate text-xs text-muted-foreground mt-0.5 w-[800px] h-auto">
                          {biz.description || "Pas de description"}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-6 shrink-0">
                      <div className="hidden sm:flex items-center gap-4 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1" title="Offres d'emploi">
                          <Briefcase className="h-3.5 w-3.5" />
                          {(biz.jobOffersList ?? []).length}
                        </span>
                        <span className="flex items-center gap-1" title="Professionnels">
                          <Users className="h-3.5 w-3.5" />
                          {(biz.professionalsList ?? []).length}
                        </span>
                      </div>
                      <ChevronRight className="h-4 w-4 text-muted-foreground" />
                    </div>
                  </CardContent>
                </Card>
              </Link>
            )
          })}
        </div>
      )}
    </div>
  )
}