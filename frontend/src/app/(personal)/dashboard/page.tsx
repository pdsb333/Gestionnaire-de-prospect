"use client"

import { parseISO, isPast, isToday } from "date-fns"
import {
    Building2,
    FileText,
    AlertCircle,
    Clock,
    RotateCw,
    Send,
    CircleAlert,
} from "lucide-react"
import { useGDP } from "@/lib/store"
import { StatCard } from "@/components/dashboard/stat-card"
import { UpcomingRelaunches } from "@/components/dashboard/upcoming-relaunches"
import { RecentApplications } from "@/components/dashboard/recent-applications"

// Renvoie la date du jour normalisée à minuit (pour comparer uniquement les jours)
function startOfToday() {
    const d = new Date()
    d.setHours(0, 0, 0, 0)
    return d
}

// Renvoie le lundi 00:00 de la semaine courante
function startOfWeek(reference: Date) {
    const d = new Date(reference)
    const day = d.getDay() // 0 = dimanche, 1 = lundi, ...
    // décalage pour ramener au lundi (si dimanche, on recule de 6 jours)
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

export default function DashboardPage() {
    const { businesses } = useGDP()

    const today = startOfToday()
    const weekStart = startOfWeek(today)
    const weekEnd = endOfWeek(today)

    // Toutes les offres de toutes les entreprises
    const allJobOffers = businesses.flatMap((b) => b.jobOffersList ?? [])

    // Candidatures = offres ayant une "application" non nulle
    const allApplications = allJobOffers
        .map((o) => o.application)
        .filter((app): app is NonNullable<typeof app> => app != null)

    // Relances en retard : dateRelaunch strictement avant aujourd'hui
    // (même logique que UpcomingRelaunches/ApplicationRow/BusinessDetail : isPast && !isToday)
    const overdue = allApplications.filter((app) => {
        if (!app.dateRelaunch) return false
        const relaunchDate = parseISO(app.dateRelaunch)
        return isPast(relaunchDate) && !isToday(relaunchDate)
    })

    // Relances prévues aujourd'hui
    const todayRelaunches = allApplications.filter((app) => {
        if (!app.dateRelaunch) return false
        return isToday(parseISO(app.dateRelaunch))
    })

    // À relancer = aujourd'hui + en retard
    const toRelaunch = todayRelaunches.length + overdue.length

    // Total des relances déjà effectuées (historique)
    const totalRelaunches = allApplications.reduce(
        (acc, app) => acc + (app.historyOfRelaunches?.length ?? 0),
        0
    )

    // Candidatures réalisées durant la semaine courante (lundi 00:00 -> dimanche 23:59)
    const applicationsThisWeek = allApplications.filter((app) => {
        const initialDate = new Date(app.initialApplicationDate)
        return initialDate >= weekStart && initialDate <= weekEnd
    })

    return (
        <div className="flex flex-col gap-8 p-6 lg:p-8">
            <div>
                <h1 className="text-2xl font-bold tracking-tight">Tableau de bord</h1>
                <p className="text-sm text-muted-foreground mt-1">
                    Vue d{"'"}ensemble de votre recherche d{"'"}emploi
                </p>
            </div>

            {/* Stats Grid */}
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-3">
                <StatCard
                    label="Candidatures"
                    value={allApplications.length}
                    icon={FileText}
                />
                <StatCard
                    label="Cette semaine"
                    value={applicationsThisWeek.length}
                    icon={Send}
                    description="Nouvelles candidatures"
                />
                <StatCard
                    label="À relancer"
                    value={toRelaunch}
                    icon={CircleAlert}
                    variant={toRelaunch > 0 ? "urgent" : "default"}
                    description={toRelaunch > 0 ? "Action requise" : undefined}
                />
            </div>

            {/* Main Content */}
            <div className="grid gap-6 lg:grid-cols-2">
                <UpcomingRelaunches />
                <RecentApplications /> 
            </div>
        </div>
    )
}