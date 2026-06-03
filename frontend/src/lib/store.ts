"use client"

import { createContext, useContext } from "react"
import type {  Auth, Business } from "./types"

export interface GDPStore {
    businesses: Business[]
    isAuthenticated: boolean
    user: {email: string} | null
    loading: boolean
    error: string | null
    login: (credentials: Omit<Auth, "pseudo">) => Promise<void>
    register: (data: Auth) => Promise<void>
    addBusiness: (b: Omit<Business, "id" | "jobOffersList" | "professionalsList">) => void
    deleteBusiness: (id:number) => Promise<void>
}

export const GDPContext = createContext<GDPStore | null>(null)

export function useGDP(): GDPStore {
  const ctx = useContext(GDPContext)
  if (!ctx) throw new Error("useGDP must be used within GDPProvider")
  return ctx
}

// GUARDS

//Retourne un tableau vide si `businesses` n'est pas un tableau peuplé. 
function safeBusinesses(businesses: Business[]): Business[] {
  if (!Array.isArray(businesses) || businesses.length === 0) return []
  return businesses
}