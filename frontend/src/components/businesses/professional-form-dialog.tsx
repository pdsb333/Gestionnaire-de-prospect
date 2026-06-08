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

export interface ProfessionalFormValues  {
  lastname: string
  firstname: string
  job: string
  contact: string
}

interface ProfessionalFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  submitLabel: string
  defaultValues?: Partial<ProfessionalFormValues>
  onSubmit: (values: ProfessionalFormValues) => void
}

export function ProfessionalFormDialog({
  open,
  onOpenChange,
  title,
  submitLabel,
  defaultValues = {},
  onSubmit,
}: ProfessionalFormDialogProps) {
  const [lastname, setLastName] = useState(defaultValues.lastname ?? "")
  const [firstname, setFirstName] = useState(defaultValues.firstname ?? "")
  const [job, setJob] = useState(defaultValues.job ?? "")
  const [contact, setContact] = useState(defaultValues.contact ?? "")

  useEffect(() => {
    setLastName(defaultValues.lastname ?? "")
    setFirstName(defaultValues.firstname ?? "")
    setJob(defaultValues.job ?? "")
    setContact(defaultValues.contact ?? "")
  }, [open])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!lastname.trim()) return
    onSubmit({
      lastname: lastname.trim(),
      firstname: firstname.trim(),
      job: job.trim(),
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
            <Label htmlFor="biz-lastname">Nom *</Label>
            <Input
              id="biz-lastname"
              value={lastname}
              onChange={(e) => setLastName(e.target.value)}
              placeholder="Nom"
              required
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="biz-firstname">Prénom</Label>
            <Input
              id="biz-firstname"
              value={firstname}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder="Prénom"
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="biz-job">Emploi</Label>
            <Input
              id="biz-job"
              value={job}
              onChange={(e) => setJob(e.target.value)}
              placeholder="Manager..."
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="biz-contact">Contact</Label>
            <Input
              id="biz-contact"
              value={contact}
              onChange={(e) => setContact(e.target.value)}
              placeholder="Numéro de téléphone ou linkedin ou mail"
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