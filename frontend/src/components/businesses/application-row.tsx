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
  Trash
} from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useGDP } from "@/lib/store"
import { cn } from "@/lib/utils"
import type { Application, JobOffer } from "@/lib/types"
import {EditOfferDialog} from "./edit-offer-dialog"
import { apiClient } from "@/lib/api-client"

interface ApplicationRowProps {
  application: Application
  businessId: number
  jobOffer: JobOffer
}

export function ApplicationRow({ application, businessId, jobOffer }: ApplicationRowProps) {
  //const { updateRelaunchDate, deleteJobOffer } = useGDP()
  const [expanded, setExpanded] = useState(false)
  const [newRelaunchDate, setNewRelaunchDate] = useState("")

  const relaunchDate = application.dateRelaunch
    ? (() => {
      try { return parseISO(application.dateRelaunch) } catch { return null }
    })()
    : null

  const isOverdue = relaunchDate ? isPast(relaunchDate) && !isToday(relaunchDate) : false
  const isTodayRelaunch = relaunchDate ? isToday(relaunchDate) : false

  const initialDate = (() => {
    try { return parseISO(application.initialApplicationDate) } catch { return null }
  })()

  const historyOfRelaunches = application.historyOfRelaunches ?? []

  //const handleMarkDone = () => {
  //  if (newRelaunchDate) {
  //    const formattedDate = newRelaunchDate + 'T23:00:00';
  //    updateRelaunchDate(application.id, {
  //      initialApplicationDate: application.initialApplicationDate,
  //      dateRelaunch: formattedDate
  //    })      
  //    setNewRelaunchDate("")
  //  }
  //}

  return (
    <Card
      className={cn(
        "transition-colors",
        isOverdue && "border-foreground/20 bg-foreground/2",
        isTodayRelaunch && "border-foreground/10"
      )}
      id={String(jobOffer.id)}
    >
      <CardContent className="p-0">
        {/* Header row — clickable */}
        <button
          onClick={() => setExpanded(!expanded)}
          className="flex w-full items-center justify-between gap-4 p-4 text-left hover:bg-muted/30 transition-colors"
        >
          <div className="flex items-center gap-3 min-w-0">
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
                  className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors mt-0.5"
                >
                  <ExternalLink className="h-3 w-3" />
                  Voir l&apos;offre
                </a>
              )}
              <p className="text-xs text-muted-foreground">
                {initialDate
                  ? `Envoyée le ${format(initialDate, "d MMM yyyy", { locale: fr })}`
                  : "Date d'envoi inconnue"}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {relaunchDate && (
              <Badge
                variant={isOverdue ? "default" : "secondary"}
                className={cn(
                  "text-[10px]",
                  isOverdue && "bg-foreground text-background",
                  isTodayRelaunch && "border-foreground/30 bg-foreground/10 text-foreground"
                )}
              >
                {isOverdue
                  ? "En retard"
                  : isTodayRelaunch
                    ? "Aujourd'hui"
                    : format(relaunchDate, "d MMM", { locale: fr })}
              </Badge>
            )}
            {expanded ? (
              <ChevronUp className="h-4 w-4 text-muted-foreground" />
            ) : (
              <ChevronDown className="h-4 w-4 text-muted-foreground" />
            )}
          </div>
        </button>

        {/* Expanded panel */}
        {expanded && (
          <div className="border-t border-border px-4 py-4 flex flex-col gap-4">
            <div className="flex justify-end">
              <Button variant="ghost" className="text-destructive">
                <span className="sr-only">Supprimer l&apos;offre</span>
                <Trash/>
              </Button>
              <EditOfferDialog
                offer={jobOffer}
              />
            </div>
            {/* Mark relaunch done */}
            <div className="flex flex-col gap-2 rounded-md border border-border p-3">
              <p className="text-xs font-medium">Marquer une relance effectuée</p>
              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  className="h-8 gap-1 text-xs"
                >
                  <Check className="h-3 w-3" />
                  Fait
                </Button>
                <p className="text-[10px] text-muted-foreground">Nouvelle date :</p>
                <Input
                  type="date"
                  className="h-7 text-[10px] w-auto"
                  value={newRelaunchDate}
                  onChange={(e) => setNewRelaunchDate(e.target.value)}
                />
              </div>
            </div>

            {/* History */}
            {historyOfRelaunches.length > 0 && (
              <div className="flex flex-col gap-2">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Historique ({historyOfRelaunches.length})
                </p>
                <div className="flex flex-col">
                  {[...historyOfRelaunches].reverse().map((entry, i) => (
                    <div
                      key={i}
                      className="flex items-start gap-3 py-2 border-b border-border last:border-b-0"
                    >
                      <div className="flex flex-col items-center gap-1 pt-0.5">
                        <div className="h-2 w-2 rounded-full bg-foreground/30" />
                        {i < historyOfRelaunches.length - 1 && (
                          <div className="w-px flex-1 bg-border" />
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