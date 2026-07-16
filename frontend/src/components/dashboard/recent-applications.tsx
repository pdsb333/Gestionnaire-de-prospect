"use client"

import Link from "next/link"
import { format, parseISO } from "date-fns"
import { fr } from "date-fns/locale"
import { ArrowRight } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import type { JobOffer, Application } from "@/lib/types"

// Renvoie le lundi 00:00 de la semaine courante
function startOfWeek(reference: Date) {
  const d = new Date(reference)
  const day = d.getDay() // 0 = dimanche, 1 = lundi, ...
  const diff = day === 0 ? -6 : 1 - day
  d.setDate(d.getDate() + diff)
  d.setHours(0, 0, 0, 0)
  return d
}

// Renvoie le dimanche 23:59:59.999 de la semaine courante
function endOfWeek(reference: Date) {
  const start = startOfWeek(reference)
  const end = new Date(start)
  end.setDate(end.getDate() + 6)
  end.setHours(23, 59, 59, 999)
  return end
}

export function RecentApplications() {
  const { businesses } = useGDP()

  const now = new Date()
  const weekStart = startOfWeek(now)
  const weekEnd = endOfWeek(now)

  const applicationsThisWeek = businesses
    .flatMap((business) =>
      (business.jobOffersList ?? [])
        .filter(
          (offer): offer is JobOffer & { application: Application } =>
            offer.application != null && !!offer.application.initialApplicationDate
        )
        .map((offer) => ({
          businessId: business.id,
          businessName: business.name,
          jobOfferName: offer.name,
          initialApplicationDate: offer.application.initialApplicationDate,
        }))
    )
    .filter((app) => {
      const date = parseISO(app.initialApplicationDate)
      return date >= weekStart && date <= weekEnd
    })
    .sort((a, b) => (a.initialApplicationDate < b.initialApplicationDate ? 1 : -1))

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <CardTitle className="text-lg font-semibold">Candidatures de la semaine</CardTitle>
        <Button variant="ghost" size="sm" className="text-xs text-muted-foreground">
          <Link href="/businesses" className="flex items-center">
            Tout voir <ArrowRight className="ml-1 h-3 w-3" />
          </Link>
        </Button>
      </CardHeader>
      <CardContent className="flex flex-col gap-0">
        {applicationsThisWeek.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Aucune candidature cette semaine
          </p>
        ) : (
          applicationsThisWeek.map((app, index) => (
            <Link
              key={`${app.businessId}-${index}`}
              href={`/businesses/${app.businessId}`}
              className="flex items-center justify-between gap-4 border-b border-border px-1 py-3 last:border-b-0 transition-colors hover:bg-muted/50"
            >
              <div className="min-w-0">
                <p className="truncate text-sm font-medium">{app.jobOfferName}</p>
                <p className="truncate text-xs text-muted-foreground">{app.businessName}</p>
              </div>
              <p className="shrink-0 text-xs text-muted-foreground">
                {format(parseISO(app.initialApplicationDate), "d MMM yyyy", { locale: fr })}
              </p>
            </Link>
          ))
        )}
      </CardContent>
    </Card>
  )
}