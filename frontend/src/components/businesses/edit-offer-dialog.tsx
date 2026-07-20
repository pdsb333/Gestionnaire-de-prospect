"use client"

import { useState } from "react"
import { Pencil } from "lucide-react"
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
import type { JobOffer } from "@/lib/types"

interface EditOfferDialogProps {
  offer: JobOffer
}

export function EditOfferDialog({ offer }: EditOfferDialogProps) {
  const { updateJobOffer } = useGDP()
  const [open, setOpen] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [name, setName] = useState(offer.name)
  const [link, setLink] = useState(offer.link ?? "")
  // Kept as a string so clearing the field doesn't snap back to "0" (Number("") === 0) while
  // the user is retyping a value — converted to a number only at submit time.
  const [relaunchFrequency, setRelaunchFrequency] = useState(String(offer.relaunchFrequency ?? 7))

  const resetAll = () => {
    setName(offer.name)
    setLink(offer.link ?? "")
    setRelaunchFrequency(String(offer.relaunchFrequency ?? 7))
    setError(null)
  }

  const handleOpenChange = (val: boolean) => {
    setOpen(val)
    if (!val) resetAll()
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    setError(null)
    setIsSubmitting(true)
    try {
      await updateJobOffer(offer.id, {
        name: name.trim(),
        link: link.trim(),
        relaunchFrequency: Number(relaunchFrequency),
      })
      setOpen(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Échec de la modification de l'offre")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger render={<Button variant="ghost" size="icon" className="h-7 w-7" />}>
        <Pencil className="h-3.5 w-3.5" />
        <span className="sr-only">Modifier l&apos;offre</span>
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>Modifier l&apos;offre d&apos;emploi</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="edit-offer-name">Intitulé *</Label>
            <Input
              id="edit-offer-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Ex: Frontend Engineer"
              required
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="edit-offer-link">Lien</Label>
            <Input
              id="edit-offer-link"
              value={link}
              onChange={(e) => setLink(e.target.value)}
              placeholder="https://..."
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="edit-offer-relaunch">Fréquence de relance (jours)</Label>
            <Input
              id="edit-offer-relaunch"
              type="number"
              min={1}
              value={relaunchFrequency}
              onChange={(e) => setRelaunchFrequency(e.target.value)}
              placeholder="Ex: 7"
              required
            />
          </div>
          {error && (
            <p className="text-sm text-destructive">{error}</p>
          )}
          <DialogFooter>
            <DialogClose render={<Button variant="outline" size="sm" disabled={isSubmitting} />}>
              Annuler
            </DialogClose>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Enregistrement..." : "Enregistrer"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}