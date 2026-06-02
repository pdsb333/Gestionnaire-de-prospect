"use client"

import { use } from "react"
import { BusinessDetail } from "@/components/businesses/business-detail"

export default function BusinessDetailPage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const businessId = Number(id)

  if (isNaN(businessId)) {
    return <p className="p-6 text-sm text-muted-foreground">Identifiant invalide.</p>
  }

  return <BusinessDetail businessId={businessId} />
}