"use client"

import Link from "next/link"
import { isPast, parseISO, isToday } from "date-fns"
import {
  ArrowLeft,
  ExternalLink,
  Mail,
  User,
  AlertCircle,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useGDP } from "@/lib/store"
import { ApplicationRow } from "./application-row"
import { useRouter } from "next/navigation"

export function BusinessDetail({ businessId }: { businessId: number }) {
  const { businesses, deleteBusiness} = useGDP()
  const business = businesses.find((b) => b.id === businessId)
  const router = useRouter();
  const handleDelete = async () => {
    await deleteBusiness(businessId)
    router.push("/businesses")
  }

  console.log(businesses);
  if (!business) {
    return (
      <div className="flex flex-col items-center justify-center py-20 p-6">
        <p className="text-sm text-muted-foreground">Entreprise introuvable</p>
        <Button variant="ghost" size="sm" className="mt-4">
          <Link href="/businesses">
            <ArrowLeft className="mr-1 h-4 w-4" />
            Retour
          </Link>
        </Button>
      </div>
    )
  }

  // Offres dont la candidature est en retard de relance
  const jobOffersList = business.jobOffersList ?? []
  const professionalsList = business.professionalsList ?? []

  const overdueOffers = jobOffersList.filter((j) => {
    const date = j.application?.dateRelaunch
    if (!date) return false
    try {
      const parsed = parseISO(date)
      return isPast(parsed) && !isToday(parsed)
    } catch {
      return false
    }
  })

  // Offres ayant une candidature associée
  const offersWithApplication = jobOffersList.filter((j) => !!j.application)

  // Offres sans candidature
  const offersWithoutApplication = jobOffersList.filter((j) => !j.application)

  // Total des relances effectuées
  const totalRelaunches = jobOffersList.reduce(
    (sum, j) => sum + (j.application?.historyOfRelaunches?.length ?? 0),
    0
  )

  return (
    <div className="flex flex-col gap-6 p-6 lg:p-8">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" size="sm" className="w-fit -ml-2 text-muted-foreground">
          <Link href="/businesses" className="flex items-center">
            <ArrowLeft className="mr-1 h-4 w-4" />
            Entreprises
          </Link>
        </Button>

        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-foreground text-background text-lg font-bold">
              {business.name?.charAt(0).toUpperCase() ?? "?"}
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight">{business.name}</h1>
              {business.description && (
                <p className="text-sm text-muted-foreground mt-0.5">
                  {business.description}
                </p>
              )}
            </div>
          </div>

          {/* Supprimer le business*/}
          <Button variant="destructive" onClick={handleDelete}>
            Supprimer l&apos;entreprise
          </Button>
        </div>

        {business.recruitmentServiceContact && (
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            <Mail className="h-3.5 w-3.5" />
            {business.recruitmentServiceContact}
          </div>
        )}

        {overdueOffers.length > 0 && (
          <div className="flex items-center gap-2 rounded-md border border-foreground/20 bg-foreground/[0.03] p-3">
            <AlertCircle className="h-4 w-4 shrink-0 text-foreground" />
            <p className="text-sm">
              <span className="font-medium">
                {overdueOffers.length} relance{overdueOffers.length > 1 ? "s" : ""} en retard
              </span>
              <span className="text-muted-foreground"> — action requise</span>
            </p>
          </div>
        )}
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <div className="rounded-md border border-border p-3">
          <p className="text-xs text-muted-foreground uppercase tracking-wider">Candidatures</p>
          <p className="text-xl font-bold mt-1">{jobOffersList.length}</p>
        </div>
        <div className="rounded-md border border-border p-3">
          <p className="text-xs text-muted-foreground uppercase tracking-wider">Contacts</p>
          <p className="text-xl font-bold mt-1">{professionalsList.length}</p>
        </div>
        <div className="rounded-md border border-border p-3">
          <p className="text-xs text-muted-foreground uppercase tracking-wider">Relances</p>
          <p className="text-xl font-bold mt-1">{totalRelaunches}</p>
        </div>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="applications" className="flex flex-col gap-4">
        <TabsList className="w-fit">
          <TabsTrigger value="applications">
            Candidatures
            {offersWithApplication.length > 0 && (
              <span className="ml-1.5 text-xs text-muted-foreground">
                ({offersWithApplication.length})
              </span>
            )}
          </TabsTrigger>
          <TabsTrigger value="contacts">
            Contacts
            {professionalsList.length > 0 && (
              <span className="ml-1.5 text-xs text-muted-foreground">
                ({professionalsList.length})
              </span>
            )}
          </TabsTrigger>
        </TabsList>

        {/* Applications Tab — une ligne par offre ayant une candidature */}
        <TabsContent value="applications" className="flex flex-col gap-3 mt-0">
            <div className="flex justify-end">
              <Button variant="secondary">Ajouter une offre d'emploi</Button>
            </div>
          {offersWithApplication.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center py-10">
                <p className="text-sm text-muted-foreground">Aucune candidature</p>
              </CardContent>
            </Card>
          ) : (
            offersWithApplication.map((offer) => (
              <ApplicationRow
                key={offer.id}
                application={offer.application!}
                businessId={business.id}
                jobOffer={offer}
              />
            ))
          )}

          {/* Section offres sans candidature */}
          {offersWithoutApplication.length > 0 && (
            <div className="flex flex-col gap-3 mt-4">
              <div className="flex items-center gap-3">
                <p className="text-sm font-medium text-muted-foreground whitespace-nowrap">
                  Démarche à renseigner
                </p>
                <div className="h-px flex-1 bg-border" />
                <span className="text-xs text-muted-foreground">
                  {offersWithoutApplication.length}
                </span>
              </div>
              {offersWithoutApplication.map((offer) => (
                <Card key={offer.id} className="border-dashed">
                  <CardContent className="flex items-center justify-between p-4">
                    <div className="flex flex-col gap-0.5">
                      <p className="text-sm font-medium">{offer.name ?? "Offre sans titre"}</p>
                      {offer.link && (
                        <a
                          href={offer.link}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors w-fit"
                        >
                          <ExternalLink className="h-3 w-3" />
                          Voir l&apos;offre
                        </a>
                      )}
                    </div>
                    <Button variant="outline" size="lg">Ajouter une candidature</Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>


        {/* Contacts Tab */}
        <TabsContent value="contacts" className="flex flex-col gap-3 mt-0">
          <div className="flex justify-end">
            <Button variant="secondary">Ajouter un contact de l'entreprise</Button>
          </div>
          {professionalsList.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center py-10">
                <p className="text-sm text-muted-foreground">Aucun contact</p>
              </CardContent>
            </Card>
          ) : (
            professionalsList.map((pro) => (
              <Card key={pro.id}>
                <CardContent className="flex items-center justify-between p-4">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-muted">
                      <User className="h-4 w-4 text-muted-foreground" />
                    </div>
                    <div>
                      <p className="text-sm font-medium">
                        {pro.firstName} {pro.lastName}
                      </p>
                      {pro.job && (
                        <p className="text-xs text-muted-foreground">{pro.job}</p>
                      )}
                    </div>
                  </div>
                  {pro.contact && (
                    <p className="text-xs text-muted-foreground truncate max-w-[200px]">
                      {pro.contact}
                    </p>
                  )}
                </CardContent>
              </Card>
            ))
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}