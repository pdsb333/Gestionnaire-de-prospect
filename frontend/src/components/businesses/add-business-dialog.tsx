"use client"

import { useState } from "react"
import { Plus } from "lucide-react"
import { DialogTrigger } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { useGDP } from "@/lib/store"
import { BusinessFormDialog } from "./business-form-dialog"

export function AddBusinessDialog() {
  const { addBusiness } = useGDP()
  const [open, setOpen] = useState(false)

  return (
    <>
      <Button size="sm" className="gap-1.5" onClick={() => setOpen(true)}>
        <Plus className="h-4 w-4" />
        Ajouter
      </Button>

      <BusinessFormDialog
        open={open}
        onOpenChange={setOpen}
        title="Nouvelle entreprise"
        submitLabel="Créer"
        onSubmit={(values) => {
          addBusiness({
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