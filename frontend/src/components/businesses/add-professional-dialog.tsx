"use client"

import { useState } from "react"
import { Plus } from "lucide-react"
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

export function AddProfessionalDialog({ businessId }: { businessId: number }) {
  const { addProfessional } = useGDP()
  const [open, setOpen] = useState(false)
  const [firstName, setFirstName] = useState("")
  const [lastName, setLastName] = useState("")
  const [job, setJob] = useState("")
  const [contact, setContact] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const resetAll = () => {
    setFirstName("")
    setLastName("")
    setJob("")
    setContact("")
    setError(null)
  }

  const handleOpenChange = (val: boolean) => {
    setOpen(val)
    if (!val) resetAll()
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!firstName.trim() || !lastName.trim()) return

    setError(null)
    setIsSubmitting(true)
    try {
      await addProfessional(businessId, {
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        job: job.trim(),
        contact: contact.trim(),
      })
      setOpen(false)
      resetAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : "Échec de l'ajout du contact")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger render={<Button variant="default" size="sm" className="gap-1.5" />}>
          <Plus className="h-3.5 w-3.5" />
          Contact
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Nouveau contact</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="pro-first">Prenom *</Label>
              <Input
                id="pro-first"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="pro-last">Nom *</Label>
              <Input
                id="pro-last"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </div>
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="pro-job">Poste</Label>
            <Input
              id="pro-job"
              value={job}
              onChange={(e) => setJob(e.target.value)}
              placeholder="Ex: Recruiter"
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="pro-contact">Contact</Label>
            <Input
              id="pro-contact"
              value={contact}
              onChange={(e) => setContact(e.target.value)}
              placeholder="Email, LinkedIn, telephone"
            />
          </div>
          {error && (
            <p className="text-sm text-destructive">{error}</p>
          )}
          <DialogFooter>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Ajout..." : "Ajouter"}
            </Button>
            <DialogClose render={<Button type="button" variant="destructive" size="sm" className="gap-1.5" disabled={isSubmitting} />}>
              Annuler
            </DialogClose>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
