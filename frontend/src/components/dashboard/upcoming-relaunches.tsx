"use client"

import Link from "next/link"
import { format, parseISO, isToday, isPast } from "date-fns"
import { fr } from "date-fns/locale"
import { ArrowRight, AlertCircle, Clock } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { cn } from "@/lib/utils"

export function UpcomingRelaunches() {
  const { businesses } = useGDP()

  const upcomingRelaunches = businesses
    .flatMap((business) =>
      (business.jobOffersList ?? [])
        .filter((offer) => offer.application?.dateRelaunch)
        .map((offer) => ({
          businessId: business.id,
          businessName: business.name,
          jobOfferName: offer.name,
          dateRelaunch: offer.application!.dateRelaunch,
        }))
    )
    .sort((a, b) => (a.dateRelaunch > b.dateRelaunch ? 1 : -1))
    .slice(0, 6)

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <CardTitle className="text-lg font-semibold">Relances à venir</CardTitle>
        <Button variant="ghost" size="sm" className="text-xs text-muted-foreground">
          <Link href="/businesses" className="flex items-center">
            Tout voir <ArrowRight className="ml-1 h-3 w-3" />
          </Link>
        </Button>
      </CardHeader>
      <CardContent className="flex flex-col gap-0">
        {upcomingRelaunches.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Aucune relance planifiee
          </p>
        ) : (
          upcomingRelaunches.map((relaunch, index) => {
            const date = parseISO(relaunch.dateRelaunch)
            const overdue = isPast(date) && !isToday(date)
            const today = isToday(date)
            return (
              <Link
                key={`${relaunch.businessId}-${index}`}
                href={`/businesses/${relaunch.businessId}`}
                className={cn(
                  "flex items-center justify-between gap-4 border-b border-border px-1 py-3 last:border-b-0 transition-colors hover:bg-muted/50",
                  overdue 
                )}
              >
                <div className="flex items-center gap-3 min-w-0">
                  {overdue ? (
                    <AlertCircle className="h-4 w-4 shrink-0 text-foreground" />
                  ) : today ? (
                    <Clock className="h-4 w-4 shrink-0 text-foreground/60" />
                  ) : (
                    <div className="h-4 w-4 shrink-0" />
                  )}
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium">{relaunch.businessName}</p>
                    <p className="truncate text-xs text-muted-foreground">
                      {relaunch.jobOfferName}
                    </p>
                  </div>
                </div>
                <Badge
                  variant={overdue ? "default" : "secondary"}
                  className={cn(
                    "shrink-0 text-[10px] font-medium",
                    overdue && "bg-foreground text-background",
                    today && "border-foreground/30 bg-foreground/10 text-foreground"
                  )}
                >
                  {overdue
                    ? "En retard"
                    : today
                    ? "Aujourd'hui"
                    : format(date, "d MMM", { locale: fr })}
                </Badge>
              </Link>
            )
          })
        )}
      </CardContent>
    </Card>
  )
}