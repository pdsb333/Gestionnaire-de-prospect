"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { ProfessionalFormDialog } from "./professional-form-dialog"
import { Professional } from "@/lib/types"

export function EditProfessionalDialog({ professional, open, onOpenChange }: {
  professional: Professional
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const { updateProfessional } = useGDP()

  return (
    <ProfessionalFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title="Modifier le contact"
      submitLabel="Enregistrer"
      defaultValues={{
        lastname: professional.lastName,
        firstname: professional.firstName,
        job: professional.job,
        contact: professional.contact,
      }}
      onSubmit={(values) => {
        updateProfessional(professional.id, {
          lastName: values.lastname,
          firstName: values.firstname,
          job: values.job,
          contact: values.contact,
        })
        onOpenChange(false)
      }}
    />
  )
}