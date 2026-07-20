"use client"

import { useState } from "react"
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
import { useGDP } from "@/lib/store"

interface AddApplicationDialogProps {
  jobOfferId: number
  jobOfferName: string
}

export function AddApplicationDialog({ jobOfferId, jobOfferName }: AddApplicationDialogProps) {
  const { addApplication } = useGDP()
  const [open, setOpen] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [initialApplicationDate, setInitialApplicationDate] = useState("")

  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`

  const resetAll = () => {
    setInitialApplicationDate("")
    setError(null)
  }

  const handleOpenChange = (val: boolean) => {
    setOpen(val)
    if (!val) resetAll()
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const applicationDate = initialApplicationDate + "T00:00:00"

    setError(null)
    setIsSubmitting(true)
    try {
      await addApplication(jobOfferId, {
        initialApplicationDate: applicationDate,
      })
      setOpen(false)
      resetAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : "Échec de la création de la candidature")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger render={<Button variant="outline" size="lg" />}>
        Ajouter une candidature
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>Ajouter une candidature</DialogTitle>
        </DialogHeader>

        <p className="text-sm text-muted-foreground">
          Associer une candidature à <span className="font-medium text-foreground">{jobOfferName}</span>.
        </p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="add-app-initial">Date de candidature initiale *</Label>
            <Input
              id="add-app-initial"
              type="date"
              value={initialApplicationDate}
              onChange={(e) => setInitialApplicationDate(e.target.value)}
              max={today}
              required
            />
          </div>
          {error && (
            <p className="text-sm text-destructive">{error}</p>
          )}
          <DialogFooter>
            <DialogClose render={<Button variant="ghost" size="sm" disabled={isSubmitting} />}>
              Annuler
            </DialogClose>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Création..." : "Créer la candidature"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}