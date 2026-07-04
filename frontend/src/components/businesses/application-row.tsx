"use client"

import { useState } from "react"
import { format, parseISO, isPast, isToday } from "date-fns"
import { fr } from "date-fns/locale"
import {
  Clock,
  AlertCircle,
  Check,
  ChevronDown,
  ChevronUp,
  ExternalLink,
  Trash,
} from "lucide-react"

import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { cn } from "@/lib/utils"
import type { Application, JobOffer } from "@/lib/types"
import { EditOfferDialog } from "./edit-offer-dialog"

interface ApplicationRowProps {
  application: Application
  businessId: number
  jobOffer: JobOffer
}

export function ApplicationRow({
  application,
  businessId,
  jobOffer,
}: ApplicationRowProps) {
  const { markApplicationRelaunched } = useGDP()

  const [expanded, setExpanded] = useState(false)
  const [isMarking, setIsMarking] = useState(false)
  const [markError, setMarkError] = useState<string | null>(null)

  const relaunchDate = application.dateRelaunch
    ? (() => {
        try {
          return parseISO(application.dateRelaunch)
        } catch {
          return null
        }
      })()
    : null

  const isOverdue =
    relaunchDate ? isPast(relaunchDate) && !isToday(relaunchDate) : false

  const isTodayRelaunch = relaunchDate
    ? isToday(relaunchDate)
    : false

  const initialDate = (() => {
    try {
      return parseISO(application.initialApplicationDate)
    } catch {
      return null
    }
  })()

  const historyOfRelaunches = application.historyOfRelaunches ?? []

  const handleMarkDone = async () => {
    setMarkError(null)
    setIsMarking(true)

    try {
      await markApplicationRelaunched(application.id)
    } catch (err) {
      setMarkError(
        err instanceof Error ? err.message : "Échec de la mise à jour"
      )
    } finally {
      setIsMarking(false)
    }
  }

  return (
    <Card
      id={String(jobOffer.id)}
      className={cn(
        "transition-colors",
        isOverdue && "border-foreground/20 bg-foreground/2",
        isTodayRelaunch && "border-foreground/10"
      )}
    >
      <CardContent className="p-0">
        <div
          role="button"
          tabIndex={0}
          onClick={() => setExpanded(!expanded)}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ") {
              e.preventDefault()
              setExpanded(!expanded)
            }
          }}
          className="flex w-full cursor-pointer items-center justify-between gap-4 p-4 text-left transition-colors hover:bg-muted/30"
        >
          <div className="flex min-w-0 items-center gap-3">
            {isOverdue ? (
              <AlertCircle className="h-4 w-4 shrink-0 text-foreground" />
            ) : isTodayRelaunch ? (
              <Clock className="h-4 w-4 shrink-0 text-foreground/60" />
            ) : (
              <div className="h-4 w-4 shrink-0 rounded-full border border-border" />
            )}

            <div className="min-w-0">
              <p className="truncate text-sm font-medium">
                {jobOffer.name || "Offre inconnue"}
              </p>

              {jobOffer.link && (
                <a
                  href={jobOffer.link}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={(e) => e.stopPropagation()}
                  className="mt-0.5 flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-foreground"
                >
                  <ExternalLink className="h-3 w-3" />
                  Voir l&apos;offre
                </a>
              )}

              <p className="text-xs text-muted-foreground">
                {initialDate
                  ? `Envoyée le ${format(initialDate, "d MMM yyyy", {
                      locale: fr,
                    })}`
                  : "Date d'envoi inconnue"}
              </p>
            </div>
          </div>

          <div className="flex shrink-0 items-center gap-2">
            {relaunchDate && (
              <Badge
                variant={isOverdue ? "default" : "secondary"}
                className={cn(
                  "text-[10px]",
                  isOverdue && "bg-foreground text-background",
                  isTodayRelaunch &&
                    "border-foreground/30 bg-foreground/10 text-foreground"
                )}
              >
                {isOverdue
                  ? "En retard"
                  : isTodayRelaunch
                  ? "Aujourd'hui"
                  : format(relaunchDate, "d MMM", {
                      locale: fr,
                    })}
              </Badge>
            )}

            {expanded ? (
              <ChevronUp className="h-4 w-4 text-muted-foreground" />
            ) : (
              <ChevronDown className="h-4 w-4 text-muted-foreground" />
            )}
          </div>
        </div>

        {expanded && (
          <div className="flex flex-col gap-4 border-t border-border px-4 py-4">
            <div className="flex justify-end gap-2">
              <Button variant="ghost" className="text-destructive">
                <span className="sr-only">
                  Supprimer l&apos;offre
                </span>
                <Trash className="h-4 w-4" />
              </Button>

              <EditOfferDialog offer={jobOffer} />
            </div>

            <div className="flex flex-col gap-2 rounded-md border border-border p-3">
              <p className="text-xs font-medium">
                Marquer une relance effectuée
              </p>

              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  className="h-8 gap-1 text-xs"
                  onClick={handleMarkDone}
                  disabled={isMarking}
                >
                  <Check className="h-3 w-3" />
                  {isMarking ? "En cours..." : "Fait"}
                </Button>

                {relaunchDate && (
                  <p className="text-[10px] text-muted-foreground">
                    Prochaine relance :{" "}
                    {format(relaunchDate, "d MMM yyyy", {
                      locale: fr,
                    })}
                  </p>
                )}
              </div>

              {markError && (
                <p className="text-[10px] text-destructive">
                  {markError}
                </p>
              )}
            </div>

            {historyOfRelaunches.length > 0 && (
              <div className="flex flex-col gap-2">
                <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
                  Historique ({historyOfRelaunches.length})
                </p>

                <div className="flex flex-col">
                  {[...historyOfRelaunches].reverse().map((entry, i) => (
                    <div
                      key={i}
                      className="flex items-start gap-3 border-b border-border py-2 last:border-b-0"
                    >
                      <div className="flex flex-col items-center gap-1 pt-0.5">
                        <div className="h-2 w-2 rounded-full bg-foreground/30" />

                        {i < historyOfRelaunches.length - 1 && (
                          <div className="flex-1 w-px bg-border" />
                        )}
                      </div>

                      <p className="text-xs">{entry}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  )
}