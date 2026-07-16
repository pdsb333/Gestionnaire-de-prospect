"use client"

import { useState } from "react"
import { Plus } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { ProfessionalFormDialog } from "./professional-form-dialog"

export function AddProfessionalDialog({ businessId }: { businessId: number }) {
  const { addProfessional } = useGDP()
  const [open, setOpen] = useState(false)

  return (
    <>
      <Button variant="default" size="sm" className="gap-1.5" onClick={() => setOpen(true)}>
        <Plus className="h-3.5 w-3.5" />
        Contact
      </Button>

      <ProfessionalFormDialog
        open={open}
        onOpenChange={setOpen}
        title="Nouveau contact"
        submitLabel="Ajouter"
        onSubmit={async (values) => {
          await addProfessional(businessId, {
            firstName: values.firstname,
            lastName: values.lastname,
            job: values.job,
            contact: values.contact,
          })
        }}
      />
    </>
  )
}
