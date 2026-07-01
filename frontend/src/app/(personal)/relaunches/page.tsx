"use client"

import { useMemo, useState } from "react"
import Link from "next/link"
import { format, parseISO } from "date-fns"
import { fr } from "date-fns/locale"
import { Bell, AlertCircle, Clock, CalendarDays } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useGDP } from "@/lib/store"
import { cn } from "@/lib/utils"

type RelaunchStatus = "pending" | "today" | "overdue"

type RelaunchApp = {
  id: number
  businessId: number
  businessName: string
  jobOfferId: number
  jobOfferName: string
  dateRelaunch: string | null
  historyOfRelaunches: string[]
  status: RelaunchStatus | null
}

type StatusFilter = "all" | RelaunchStatus
type BusinessFilter = "all" | number

const STATUS_OPTIONS: { value: StatusFilter; label: string }[] = [
  { value: "all", label: "Tous les statuts" },
  { value: "pending", label: "En attente" },
  { value: "today", label: "Aujourd'hui" },
  { value: "overdue", label: "En retard" },
]

const STATUS_BADGE: Record<RelaunchStatus, { label: string; variant: "outline" | "secondary" | "destructive" }> = {
  pending: { label: "En attente", variant: "outline" },
  today: { label: "Aujourd'hui", variant: "secondary" },
  overdue: { label: "En retard", variant: "destructive" },
}

// Calcule le statut d'une candidature à partir de dateRelaunch (logique front, non stockée en base)
function computeRelaunchStatus(dateRelaunch: string | null): RelaunchStatus | null {
  if (!dateRelaunch) return null

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const relaunch = new Date(dateRelaunch)
  relaunch.setHours(0, 0, 0, 0)

  if (relaunch.getTime() === today.getTime()) return "today"
  if (relaunch.getTime() < today.getTime()) return "overdue"
  return "pending"
}

export default function RelaunchesPage() {
  const { businesses } = useGDP()
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("all")
  const [businessFilter, setBusinessFilter] = useState<BusinessFilter>("all")

  // Reconstruction de toutes les candidatures depuis businesses -> jobOffersList -> application
  const allApps: RelaunchApp[] = businesses.flatMap((business) =>
    (business.jobOffersList ?? [])
      .filter((offer) => offer.application != null)
      .map((offer) => ({
        id: offer.application!.id,
        businessId: business.id,
        businessName: business.name,
        jobOfferId: offer.id,
        jobOfferName: offer.name,
        dateRelaunch: offer.application!.dateRelaunch,
        historyOfRelaunches: offer.application!.historyOfRelaunches ?? [],
        status: computeRelaunchStatus(offer.application!.dateRelaunch),
      }))
  )

  // Dropdown entreprise alimenté dynamiquement à partir des businesses de l'utilisateur
  const businessOptions = useMemo(
    () =>
      [...businesses]
        .sort((a, b) => a.name.localeCompare(b.name))
        .map((b) => ({ id: b.id, name: b.name })),
    [businesses]
  )

  const filteredApps = useMemo(() => {
    const sorted = [...allApps].sort((a, b) => {
      if (!a.dateRelaunch) return 1
      if (!b.dateRelaunch) return -1
      return a.dateRelaunch > b.dateRelaunch ? 1 : -1
    })

    return sorted.filter((app) => {
      const matchesStatus = statusFilter === "all" || app.status === statusFilter
      const matchesBusiness = businessFilter === "all" || app.businessId === businessFilter
      return matchesStatus && matchesBusiness
    })
  }, [allApps, statusFilter, businessFilter])

  const overdueCount = allApps.filter((a) => a.status === "overdue").length
  const todayCount = allApps.filter((a) => a.status === "today").length
  const pendingCount = allApps.filter((a) => a.status === "pending").length

  // Labels affichés explicitement dans les triggers (évite d'afficher la value brute "all")
  const selectedBusinessLabel = useMemo(() => {
    if (businessFilter === "all") return "Toutes les entreprises"
    return businessOptions.find((b) => b.id === businessFilter)?.name ?? "Toutes les entreprises"
  }, [businessFilter, businessOptions])

  const selectedStatusLabel = useMemo(() => {
    return STATUS_OPTIONS.find((s) => s.value === statusFilter)?.label ?? "Tous les statuts"
  }, [statusFilter])

  function RelaunchItem({ app }: { app: RelaunchApp }) {
    const statusBadge = app.status ? STATUS_BADGE[app.status] : null
    const Icon =
      app.status === "overdue" ? AlertCircle : app.status === "today" ? Clock : CalendarDays

    return (
      <Card
        className={cn(
          "transition-colors",
          app.status === "overdue" && "border-foreground/20 bg-foreground/[0.02]",
          app.status === "today" && "border-foreground/10"
        )}
      >
        <CardContent className="p-4">
          <div className="flex items-center justify-between gap-4">
            <Link
              href={`/businesses/${app.businessId}`}
              className="flex items-center gap-3 min-w-0 hover:opacity-70 transition-opacity"
            >
              <Icon
                className={cn(
                  "h-4 w-4 shrink-0",
                  app.status === "overdue" && "text-foreground",
                  app.status === "today" && "text-foreground/60",
                  (app.status === "pending" || app.status === null) && "text-muted-foreground"
                )}
              />
              <div className="min-w-0">
                <p className="truncate text-sm font-medium">{app.jobOfferName}</p>
                <p className="text-xs text-muted-foreground">
                  {app.businessName}
                  {" -- "}
                  {app.historyOfRelaunches.length} relance
                  {app.historyOfRelaunches.length !== 1 ? "s" : ""} effectuee
                  {app.historyOfRelaunches.length !== 1 ? "s" : ""}
                </p>
              </div>
            </Link>
            <div className="flex items-center gap-2 shrink-0">
              {statusBadge && (
                <Badge variant={statusBadge.variant} className="text-[10px] font-medium">
                  {statusBadge.label}
                  {app.dateRelaunch && app.status !== "today" && (
                    <> ({format(parseISO(app.dateRelaunch), "d MMM", { locale: fr })})</>
                  )}
                </Badge>
              )}
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="flex flex-col gap-8 p-6 lg:p-8">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Relances</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Gerez vos relances et suivez l{"'"}avancement de vos candidatures
          </p>
        </div>

        <div className="flex flex-col gap-3 sm:flex-row">
          <Select
            value={businessFilter === "all" ? "all" : String(businessFilter)}
            onValueChange={(v) => setBusinessFilter(v === "all" ? "all" : Number(v))}
          >
            <SelectTrigger className="w-full sm:w-56">
              <SelectValue placeholder="Filtrer par entreprise">
                {selectedBusinessLabel}
              </SelectValue>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Toutes les entreprises</SelectItem>
              {businessOptions.map((business) => (
                <SelectItem key={business.id} value={String(business.id)}>
                  {business.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as StatusFilter)}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="Filtrer par statut">
                {selectedStatusLabel}
              </SelectValue>
            </SelectTrigger>
            <SelectContent>
              {STATUS_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-3 gap-3">
        <div
          className={cn(
            "rounded-md border border-border p-3",
            overdueCount > 0 && "border-foreground/20 bg-foreground/[0.03]"
          )}
        >
          <p className="text-xs text-muted-foreground uppercase tracking-wider">En retard</p>
          <p className="text-xl font-bold mt-1">{overdueCount}</p>
        </div>
        <div
          className={cn(
            "rounded-md border border-border p-3",
            todayCount > 0 && "border-foreground/10 bg-foreground/[0.02]"
          )}
        >
          <p className="text-xs text-muted-foreground uppercase tracking-wider">Aujourd{"'"}hui</p>
          <p className="text-xl font-bold mt-1">{todayCount}</p>
        </div>
        <div className="rounded-md border border-border p-3">
          <p className="text-xs text-muted-foreground uppercase tracking-wider">En attente</p>
          <p className="text-xl font-bold mt-1">{pendingCount}</p>
        </div>
      </div>

      {/* List */}
      {filteredApps.length > 0 ? (
        <section className="flex flex-col gap-3">
          {filteredApps.map((app) => (
            <RelaunchItem key={app.id} app={app} />
          ))}
        </section>
      ) : allApps.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted mb-4">
            <Bell className="h-6 w-6 text-muted-foreground" />
          </div>
          <p className="text-sm font-medium">Aucune relance en cours</p>
          <p className="text-xs text-muted-foreground mt-1">
            Toutes vos candidatures sont finalisees
          </p>
        </div>
      ) : (
        <p className="py-12 text-center text-sm text-muted-foreground">
          Aucune candidature trouvee
        </p>
      )}
    </div>
  )
}