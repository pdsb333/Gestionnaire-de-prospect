import type { Auth } from "@/lib/types"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || ""

class ApiClient {
  isConfigured(): boolean {
    return Boolean(API_BASE_URL && API_BASE_URL.trim() !== "")
  }

  getBaseUrl(): string {
    return API_BASE_URL
  }


  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      credentials: "include", // ← envoie et reçoit les cookies httpOnly
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: "Request failed" }))
      throw new Error(error.message || `HTTP ${response.status}`)
    }

    if (response.status === 204) return {} as T
    return response.json()
  }

  // ============ AUTH ============
  async register(data: Auth): Promise<{token:string}>{
    const response = await this.request<{token: string}>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(data),
    })
    return response
  }

  async login(credentials: Omit<Auth, "pseudo">): Promise<{ token: string }> {
    const response = await this.request<{ token: string }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    })
    return response
  }

}

export const apiClient = new ApiClient()