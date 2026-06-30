"use client"

import { useState } from "react"
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

interface AddApplicationDialogProps {
  jobOfferId: number
  jobOfferName: string
}

export function AddApplicationDialog({ jobOfferId, jobOfferName }: AddApplicationDialogProps) {
  const { addApplication } = useGDP()
  const [open, setOpen] = useState(false)

  const [dateRelaunch, setDateRelaunch] = useState("")
  const [initialApplicationDate, setInitialApplicationDate] = useState("")

  const resetAll = () => {
    setDateRelaunch("")
    setInitialApplicationDate("")
  }

  const handleOpenChange = (val: boolean) => {
    setOpen(val)
    if (!val) resetAll()
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const applicationDate = initialApplicationDate + "T00:00:00"
    const relaunchDate = dateRelaunch + "T00:00:00"

    await addApplication(jobOfferId, {
      initialApplicationDate: applicationDate,
      dateRelaunch: relaunchDate,
    })

    setOpen(false)
    resetAll()
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger render={<Button variant="outline" size="lg" />}>
        Ajouter une candidature
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>Ajouter une candidature</DialogTitle>
        </DialogHeader>

        <p className="text-sm text-muted-foreground">
          Associer une candidature à <span className="font-medium text-foreground">{jobOfferName}</span>.
        </p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="add-app-relaunch">Date de relance prévue</Label>
            <Input
              id="add-app-relaunch"
              type="date"
              value={dateRelaunch}
              onChange={(e) => setDateRelaunch(e.target.value)}
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="add-app-initial">Date de candidature initiale</Label>
            <Input
              id="add-app-initial"
              type="date"
              value={initialApplicationDate}
              onChange={(e) => setInitialApplicationDate(e.target.value)}
            />
          </div>
          <DialogFooter>
            <DialogClose render={<Button variant="ghost" size="sm" />}>
              Annuler
            </DialogClose>
            <Button type="submit">Créer la candidature</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}