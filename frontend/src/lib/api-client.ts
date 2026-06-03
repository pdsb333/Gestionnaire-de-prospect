import type { Application, Auth, Business, JobOffer } from "@/lib/types"

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
      credentials: "include", 
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

  // ============ BUSINESSES ============
  async getBusinesses(): Promise<Business[]> {
    return this.request<Business[]>("/api/business/get")
  }

  async createBusiness(
    data: Omit<Business, "id" | "professionalsList" | "jobOffersList">
  ): Promise<Business> {
    return this.request<Business>("/api/business/post", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async deleteBusiness(id: number): Promise<void> {
    return this.request<void>(`/api/business/delete/${id}`, {
      method: "DELETE",
    })
  }

  async updateBusiness(id:number, data: Partial<Business>) : Promise<Business>{
    return this.request<Business>(`/api/business/put/${id}`,{
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  // ============ JOB OFFERS ============
  async createJobOffer(
    businessId: number,
    data: Omit<JobOffer, "id" | "application">
  ): Promise<JobOffer> {
    return this.request<JobOffer>(`/api/joboffer/post/${businessId}`, {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  // ============ APPLICATIONS ============
  async createApplication(
    jobOfferId: number,
    data: Omit<Application, "id" | "historyOfRelaunches">
  ): Promise<Application> {
    return this.request<Application>(`/api/application/post/${jobOfferId}`, {
      method: "POST",
      body: JSON.stringify(data),
    })
  }


}

export const apiClient = new ApiClient()