"use client"

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { useEffect, useState } from "react"

export interface BusinessFormValues {
  name: string
  description: string
  contact: string
}

interface BusinessFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  submitLabel: string
  defaultValues?: Partial<BusinessFormValues>
  onSubmit: (values: BusinessFormValues) => Promise<void>
}

export function BusinessFormDialog({
  open,
  onOpenChange,
  title,
  submitLabel,
  defaultValues = {},
  onSubmit,
}: BusinessFormDialogProps) {
  const [name, setName] = useState(defaultValues.name ?? "")
  const [description, setDescription] = useState(defaultValues.description ?? "")
  const [contact, setContact] = useState(defaultValues.contact ?? "")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setName(defaultValues.name ?? "")
    setDescription(defaultValues.description ?? "")
    setContact(defaultValues.contact ?? "")
    setError(null)
  }, [open])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return

    setError(null)
    setIsSubmitting(true)
    try {
      await onSubmit({
        name: name.trim(),
        description: description.trim(),
        contact: contact.trim(),
      })
      onOpenChange(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Échec de l'enregistrement")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="biz-name">Nom *</Label>
            <Input
              id="biz-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Nom de l'entreprise"
              required
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="biz-desc">Description</Label>
            <Textarea
              id="biz-desc"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Description de l'entreprise"
              rows={3}
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="biz-contact">Contact recrutement</Label>
            <Input
              id="biz-contact"
              value={contact}
              onChange={(e) => setContact(e.target.value)}
              placeholder="Email ou téléphone"
            />
          </div>
          {error && (
            <p className="text-sm text-destructive">{error}</p>
          )}
          <DialogFooter>
            <DialogClose render={<Button type="button" variant="outline" disabled={isSubmitting} />}>
              Annuler
            </DialogClose>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Enregistrement..." : submitLabel}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}