// contact-row.tsx
"use client"

import { useState } from "react"
import { Card, CardContent, CardAction } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Pencil, TrashIcon, User } from "lucide-react"
import { EditProfessionalDialog } from "./edit-professional-dialog"
import { Professional } from "@/lib/types"

interface ContactRowProps {
  pro: Professional
  onDelete: (id: number) => void
}

export function ContactRow({ pro, onDelete }: ContactRowProps) {
  const [editOpen, setEditOpen] = useState(false)

  return (
    <>
      <Card>
        <CardContent className="flex items-center justify-between p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-muted">
              <User className="h-4 w-4 text-muted-foreground" />
            </div>
            <div>
              <p className="text-sm font-medium">
                {pro.firstName} {pro.lastName}
              </p>
              {pro.job && (
                <p className="text-xs text-muted-foreground">{pro.job}</p>
              )}
            </div>
          </div>
          {pro.contact && (
            <p className="text-xs text-muted-foreground truncate max-w-[200px]">
              {pro.contact}
            </p>
          )}
          <CardAction>
            <DropdownMenu>
              <DropdownMenuTrigger render={<Button variant="ghost" />}>
                <Pencil className="h-4 w-4" />
              </DropdownMenuTrigger>
              <DropdownMenuContent>
                <DropdownMenuGroup>
                  <DropdownMenuLabel>Actions</DropdownMenuLabel>
                  <DropdownMenuItem closeOnClick={false} onClick={() => {setEditOpen(true)}}>
                    <Pencil className="h-4 w-4" />
                    Modifier
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => onDelete(pro.id)} variant="destructive">
                    <TrashIcon className="h-4 w-4" />
                    Supprimer
                  </DropdownMenuItem>
                </DropdownMenuGroup>
              </DropdownMenuContent>
            </DropdownMenu>
          </CardAction>
        </CardContent>
      </Card>

      {/* Le Dialog est rendu EN DEHORS du DropdownMenu */}
      <EditProfessionalDialog
        professional={pro}
        open={editOpen}
        onOpenChange={setEditOpen}
      />
    </>
  )
}