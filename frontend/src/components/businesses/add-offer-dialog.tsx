"use client"

import { useState } from "react"
import { Plus, ArrowRight, ArrowLeft } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { useGDP } from "@/lib/store"

interface AddOfferDialogProps {
  businessId: number
}

type Step = "offer" | "application"

export function AddOfferDialog({ businessId }: AddOfferDialogProps) {
  const { addJobOffer, addApplication } = useGDP()
  const [open, setOpen] = useState(false)
  const [step, setStep] = useState<Step>("offer")

  // Offre fields
  const [name, setName] = useState("")
  const [link, setLink] = useState("")
  const [relaunchFrequency, setRelaunchFrequency] = useState<number>(7)

  // Application fields
  const [dateRelaunch, setDateRelaunch] = useState("")
  const [initialApplicationDate, setInitialApplicationDate] = useState("")

  // Id de l'offre créée, utilisé pour lier la candidature
  const [createdJobOfferId, setCreatedJobOfferId] = useState<number | null>(null)

  const today = new Date().toISOString().split("T")[0]

  const resetAll = () => {
    setStep("offer")
    setName("")
    setLink("")
    setRelaunchFrequency(7)
    setDateRelaunch("")
    setInitialApplicationDate("")
    setCreatedJobOfferId(null)
  }

  const handleOpenChange = (val: boolean) => {
    setOpen(val)
    if (!val) resetAll()
  }

  // Étape 1 : créer l'offre et passer à l'étape 2
  const handleOfferSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    const created = await addJobOffer(businessId, {name : name.trim(), 
                                                   link: link.trim(), 
                                                   relaunchFrequency: relaunchFrequency})
    // addJobOffer doit retourner le JobOfferResponse pour récupérer l'id
    if (created?.id) {
      setCreatedJobOfferId(created.id)
    }
    setStep("application")
  }

  // Étape 2a : ajouter une candidature et fermer
  const handleApplicationSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (createdJobOfferId) {
      const applicationDate = initialApplicationDate + 'T00:00:00';
      const relaunchDate = dateRelaunch + 'T00:00:00';
      await addApplication(createdJobOfferId, {
        initialApplicationDate: applicationDate,
        dateRelaunch: relaunchDate,
      })
    }
    setOpen(false)
    resetAll()
  }

  // Étape 2b : passer sans candidature
  const handleSkip = () => {
    setOpen(false)
    resetAll()
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger render={<Button variant="default" size="sm" className="gap-1.5" />}>
          <Plus className="h-3.5 w-3.5" />
          Offre
      </DialogTrigger>

      <DialogContent>
        {/* ── Étape 1 : Offre ── */}
        {step === "offer" && (
          <>
            <DialogHeader>
              <DialogTitle>Nouvelle offre d&apos;emploi</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleOfferSubmit} className="flex flex-col gap-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="offer-name">Intitulé *</Label>
                <Input
                  id="offer-name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ex: Frontend Engineer"
                  required
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="offer-link">Lien</Label>
                <Input
                  id="offer-link"
                  value={link}
                  onChange={(e) => setLink(e.target.value)}
                  placeholder="https://..."
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="offer-relaunch">Fréquence de relance (jours)</Label>
                <Input
                  id="offer-relaunch"
                  type="number"
                  min={1}
                  value={relaunchFrequency}
                  onChange={(e) => setRelaunchFrequency(Number(e.target.value))}
                  placeholder="Ex: 7"
                  required
                />
              </div>
              <DialogFooter>
                <DialogClose render={<Button variant="outline" size="sm" className="gap-1.5" />}>
                  Annuler
                </DialogClose>
                <Button type="submit" className="gap-1.5">
                  Suivant
                  <ArrowRight className="h-3.5 w-3.5" />
                </Button>
              </DialogFooter>
            </form>
          </>
        )}

        {/* ── Étape 2 : Candidature (optionnelle) ── */}
        {step === "application" && (
          <>
            <DialogHeader>
              <DialogTitle>Ajouter une candidature ?</DialogTitle>
            </DialogHeader>

            <p className="text-sm text-muted-foreground">
              L&apos;offre <span className="font-medium text-foreground">{name}</span> a été créée.
              Voulez-vous y associer une candidature dès maintenant ?
            </p>

            <Separator />

            <form onSubmit={handleApplicationSubmit} className="flex flex-col gap-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="app-relaunch">Date de relance prevue</Label>
                <Input
                  id="app-relaunch"
                  type="date"
                  value={dateRelaunch}
                  onChange={(e) => setDateRelaunch(e.target.value)}
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="app-initial">Date de candidature initiale</Label>
                <Input
                  id="app-initial"
                  type="date"
                  value={initialApplicationDate}
                  onChange={(e) => setInitialApplicationDate(e.target.value)}
                />
              </div>
              <DialogFooter className="flex-col-reverse gap-2 sm:flex-row">
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => setStep("offer")}
                  className="gap-1.5"
                >
                  <ArrowLeft className="h-3.5 w-3.5" />
                  Retour
                </Button>
                <Button type="button" variant="outline" onClick={handleSkip}>
                  Passer
                </Button>
                <Button type="submit">
                  Créer la candidature
                </Button>
              </DialogFooter>
            </form>
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}