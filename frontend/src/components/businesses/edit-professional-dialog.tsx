"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { ProfessionalFormDialog } from "./professional-form-dialog"
import { Professional } from "@/lib/types"

interface EditProfessionalDialogProps {
  professional : Professional
}

export function EditProfessionalDialog({ professional }: EditProfessionalDialogProps) {
  const { updateProfessional } = useGDP()
  const [open, setOpen] = useState(false)

  return (
    <>
      <Button
        size="lg"
        variant="ghost"
        onClick={() => setOpen(true)}
      >
        Modifier
      </Button>

      <ProfessionalFormDialog
        open={open}
        onOpenChange={setOpen}
        title="Modifier l'entreprise"
        submitLabel="Enregistrer"
        defaultValues={{
          lastname: professional.lastName,
          firstname: professional.firstName,
          job: professional.job,
          contact: professional.contact
        }}
        onSubmit={(values) => {
          updateProfessional(professional.id, {
            lastName: values.lastname,
            firstName: values.firstname,
            job: values.job,
            contact: values.contact
          })
          setOpen(false)
        }}
      />
    </>
  )
}