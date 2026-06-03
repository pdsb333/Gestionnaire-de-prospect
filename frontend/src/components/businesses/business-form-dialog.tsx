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
  onSubmit: (values: BusinessFormValues) => void
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

  useEffect(() => {
    setName(defaultValues.name ?? "")
    setDescription(defaultValues.description ?? "")
    setContact(defaultValues.contact ?? "")
  }, [open])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    onSubmit({
      name: name.trim(),
      description: description.trim(),
      contact: contact.trim(),
    })
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
          <DialogFooter>
            <DialogClose render={<Button type="button" variant="outline" />}>
              Annuler
            </DialogClose>
            <Button type="submit">{submitLabel}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}