"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { BusinessFormDialog } from "./business-form-dialog"
import { Business } from "@/lib/types"

interface EditBusinessDialogProps {
  business : Business
}

export function EditBusinessDialog({ business }: EditBusinessDialogProps) {
  const { updateBusiness } = useGDP()
  const [open, setOpen] = useState(false)

  return (
    <>
      <Button
        size="lg"
        variant="secondary"
        className="h-8 w-25"
        onClick={() => setOpen(true)}
      >
        Modifier
      </Button>

      <BusinessFormDialog
        open={open}
        onOpenChange={setOpen}
        title="Modifier l'entreprise"
        submitLabel="Enregistrer"
        defaultValues={{
          name: business.name,
          description: business.description,
          contact: business.recruitmentServiceContact,
        }}
        onSubmit={(values) => {
          updateBusiness(business.id, {
            name: values.name,
            description: values.description,
            recruitmentServiceContact: values.contact,
          })
          setOpen(false)
        }}
      />
    </>
  )
}