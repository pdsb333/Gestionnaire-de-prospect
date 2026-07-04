"use client"

import { createContext, useContext } from "react"
import type {  Auth, Business, JobOffer, Application, Professional } from "./types"

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
    updateBusiness:(id:number, data: Partial<Business>) => void
    addJobOffer: (businessId: number, data: Omit<JobOffer, "application" | "id">) => Promise<JobOffer>
    updateJobOffer: (id: number, data: Partial<JobOffer>) => Promise<JobOffer>
    addApplication: (jobofferId: number, data: Omit<Application, "id" | "historyOfRelaunches">) => void
    updateApplication: (id: number, data: Partial<Application>) => void
    markApplicationRelaunched: (id: number) => Promise<void>
    addProfessional: (businessId: number, data: Omit<Professional, "id">) => void
    updateProfessional: (id: number, data: Partial<Professional>) => void
    deleteProfessional: (id: number) => void
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