"use client"

import { useState, useCallback, useEffect, type ReactNode } from "react"
import { GDPContext, type GDPStore } from "@/lib/store"
import { apiClient } from "@/lib/api-client"
import { Business, type Auth } from "@/lib/types"
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
  

  const store: GDPStore = {
    businesses,
    loading,
    error,
    user,
    isAuthenticated, 
    login,
    register,
  }

  if (!isConfigured) {
    return <ApiNotConfigured />
  }

  return <GDPContext value={store}>{children}</GDPContext>
}