"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogClose,
} from "@/components/ui/alert-dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useGDP } from "@/lib/store"

interface DeleteAccountDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function DeleteAccountDialog({ open, onOpenChange }: DeleteAccountDialogProps) {
  const router = useRouter()
  const { deleteAccount } = useGDP()
  const [currentPassword, setCurrentPassword] = useState("")
  const [isDeleting, setIsDeleting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleOpenChange = (val: boolean) => {
    if (isDeleting) return
    onOpenChange(val)
    if (!val) {
      setError(null)
      setCurrentPassword("")
    }
  }

  const handleConfirm = async () => {
    setError(null)
    setIsDeleting(true)
    try {
      await deleteAccount({ currentPassword })
      onOpenChange(false)
      router.push("/connexion")
    } catch (err) {
      setError(err instanceof Error ? err.message : "Échec de la suppression")
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <AlertDialog open={open} onOpenChange={handleOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Supprimer mon compte</AlertDialogTitle>
          <AlertDialogDescription>
            Cette action est irréversible. Toutes vos entreprises, offres, candidatures et
            contacts seront définitivement supprimés.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <div className="flex flex-col gap-2">
          <Label htmlFor="delete-account-password">Mot de passe actuel</Label>
          <Input
            id="delete-account-password"
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            placeholder="••••••••"
            disabled={isDeleting}
          />
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <AlertDialogFooter>
          <AlertDialogClose render={<Button variant="outline" disabled={isDeleting} />}>
            Annuler
          </AlertDialogClose>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={isDeleting || !currentPassword}
          >
            {isDeleting ? "Suppression..." : "Supprimer mon compte"}
          </Button>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
