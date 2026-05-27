"use client"

import { createContext, useContext } from "react"
import type {  Auth } from "./types"

export interface GDPStore {
    isAuthenticated: boolean
    user: {email: string} | null
    loading: boolean
    error: string | null
    login: (credentials: Omit<Auth, "pseudo">) => Promise<void>
    register: (data: Auth) => Promise<void>
}

export const GDPContext = createContext<GDPStore | null>(null)

export function useGDP(): GDPStore {
  const ctx = useContext(GDPContext)
  if (!ctx) throw new Error("useGDP must be used within GDPProvider")
  return ctx
}

