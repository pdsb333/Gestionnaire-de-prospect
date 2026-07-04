"use client"

import { useState, useCallback, useEffect, type ReactNode } from "react"
import { GDPContext, type GDPStore } from "@/lib/store"
import { apiClient } from "@/lib/api-client"
import { Application, Business, JobOffer, Professional, type Auth } from "@/lib/types"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { AlertCircle } from "lucide-react"

function ApiNotConfigured() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="max-w-md w-full">
        <CardHeader>
          <div className="flex items-center gap-2">
            <AlertCircle className="h-5 w-5 text-destructive" />
            <CardTitle>API non configurée</CardTitle>
          </div>
          <CardDescription>
            L&apos;application nécessite une connexion API pour fonctionner.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Configurez les variables d&apos;environnement suivantes :
          </p>
          <div className="rounded-md bg-muted p-3 font-mono text-sm space-y-1">
            <div>NEXT_PUBLIC_API_BASE_URL</div>
            <div>NEXT_PUBLIC_API_TOKEN <span className="text-muted-foreground">(optionnel)</span></div>
          </div>
          <p className="text-sm text-muted-foreground">
            Allez dans <strong>Settings</strong> (en haut à droite) → <strong>Vars</strong> pour ajouter ces variables.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

export function GDPProvider({ children }: { children: ReactNode }) {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isConfigured, setIsConfigured] = useState(() => apiClient.isConfigured())
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState<{email: string} | null>(null)
  const [businesses, setBusinesses] = useState<Business[]>([])
  


  const login = useCallback(
    async (credentials: Omit<Auth, "pseudo">) => {
        if (!apiClient.isConfigured()) {
            throw new Error("API not configured")
        }
        try {
            setError(null)
            setLoading(true)
            const response = await apiClient.login(credentials)
            setIsAuthenticated(true)
            setUser({ email: credentials.email })

        } catch (err) {
            console.error("Login failed:", err)
            setError(err instanceof Error ? err.message : "Login failed")
            throw err
        } finally {
            setLoading(false)
        }
    }, 
    []
  )

  const register = useCallback(
    async (data: Auth) => {
        if (!apiClient.isConfigured()) {
            throw new Error("API not configured")
        }
        try {
            setError(null)
            setLoading(true)
            await apiClient.register(data)
            setIsAuthenticated(true)
            setUser({ email: data.email })
        } catch (err) {
            console.error("Register failed:", err)
            setError(err instanceof Error ? err.message : "Register failed")
            throw err
        } finally {
            setLoading(false)
        }
    },
    []
  )

  const fetchBusinesses = useCallback(async () => {
    if (!apiClient.isConfigured()) {
      setIsConfigured(false)
      setLoading(false)
      return
    }
    try {
      setLoading(true)
      const data = await apiClient.getBusinesses()
      setBusinesses(data)
      setError(null)
    } catch (err) {
      console.error("Failed to fetch businesses:", err)
      setError(err instanceof Error ? err.message : "Failed to load data")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchBusinesses()
  }, [fetchBusinesses])

  // Business mutation

  const addBusiness = useCallback(
    async (b: Omit<Business, "id" | "jobOffersList" | "professionalsList">) =>{
      try{
        await apiClient.createBusiness(b)
        await fetchBusinesses()
      } catch (err){
        console.error("failed to create business: ", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  const deleteBusiness = useCallback(
    async (id: number) => {
      try {
        await apiClient.deleteBusiness(id)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to delete business:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  const updateBusiness = useCallback(
    async (id: number, data: Partial<Business>) => {
      try {
        await apiClient.updateBusiness(id, data)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to update business:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  //JobOffer mutation
  const addJobOffer = useCallback(
    async (businessId: number, data: Omit<JobOffer, "application" | "id">) => {
      try {
        const created = await apiClient.createJobOffer(businessId, data)
        await fetchBusinesses()
        return created
      } catch (err) {
        console.error("Failed to create job offer:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  const updateJobOffer = useCallback(
    async (id: number, data: Partial<JobOffer>) => {
      try {
        const updated = await apiClient.updateJobOffer(id, data)
        await fetchBusinesses()
        return updated
      } catch (err) {
        console.error("Failed to update job offer:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  //Application mutations
  const addApplication = useCallback(
    async (
      jobOfferId: number,
      app: Omit<Application, "id" | "historyOfRelaunches">
    ) => {
      try {
        await apiClient.createApplication(jobOfferId, app)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to create application:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )  

  const updateApplication = useCallback(
    async (id: number, data: Partial<Application>) => {
      try {
        await apiClient.updateApplication(id, data)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to update application:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  const markApplicationRelaunched = useCallback(
    async (id: number) => {
      try {
        await apiClient.markApplicationRelaunched(id)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to mark application as relaunched:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  //Professional mutations
  const addProfessional = useCallback(
    async (businessId: number, p: Omit<Professional, "id">) => {
      try {
        await apiClient.createProfessional(businessId, p)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to create professional:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )  

  const updateProfessional = useCallback(
    async (id: number, data: Partial<Professional>) => {
      try {
        await apiClient.updateProfessional(id, data)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to update professional:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )

  const deleteProfessional = useCallback(
    async (id: number) => {
      try {
        await apiClient.deleteProfessional(id)
        await fetchBusinesses()
      } catch (err) {
        console.error("Failed to delete professional:", err)
        throw err
      }
    },
    [fetchBusinesses]
  )


  const store: GDPStore = {
    businesses,
    loading,
    error,
    user,
    isAuthenticated, 
    login,
    register,
    addBusiness,
    deleteBusiness,
    updateBusiness,
    addJobOffer,
    updateJobOffer,
    addApplication,
    updateApplication,
    markApplicationRelaunched,
    addProfessional,
    updateProfessional,
    deleteProfessional
  }

  if (!isConfigured) {
    return <ApiNotConfigured />
  }

  return <GDPContext value={store}>{children}</GDPContext>
}