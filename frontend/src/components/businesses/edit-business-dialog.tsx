"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { BusinessFormDialog } from "./business-form-dialog"
import { Business } from "@/lib/types"
import { Pencil } from "lucide-react"

interface EditBusinessDialogProps {
  business : Business
}

export function EditBusinessDialog({ business }: EditBusinessDialogProps) {
  const { updateBusiness } = useGDP()
  const [open, setOpen] = useState(false)

  return (
    <>
      <Button variant="ghost" onClick={() => setOpen(true)} size="lg">
                <span className="sr-only">
                  Modifier l&apos;entreprise
                </span>
                <Pencil className="h-4 w-4" />
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
        onSubmit={async (values) => {
          await updateBusiness(business.id, {
            name: values.name,
            description: values.description,
            recruitmentServiceContact: values.contact,
          })
        }}
      />
    </>
  )
}