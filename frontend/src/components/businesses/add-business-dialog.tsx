"use client"

import { useState } from "react"
import { Plus } from "lucide-react"
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
import { Textarea } from "@/components/ui/textarea"
import { useGDP } from "@/lib/store"

export function AddBusinessDialog() {
  const { addBusiness } = useGDP()
  const [open, setOpen] = useState(false)
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [contact, setContact] = useState("")

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    addBusiness({
      name: name.trim(),
      description: description.trim(),
      recruitmentServiceContact: contact.trim(),
    })
    setName("")
    setDescription("")
    setContact("")
    setOpen(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger render={<Button size="sm" className="gap-1.5"/>}>
          <Plus className="h-4 w-4" />
          Ajouter
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Nouvelle entreprise</DialogTitle>
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
              placeholder="Email ou telephone"
            />
          </div>
          <DialogFooter>
            <DialogClose render={<Button type="button" variant="outline" />}>
                Annuler
            </DialogClose>
            <Button type="submit">Creer</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
