import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { tokenStore } from '../auth/tokenStore'
import { parseApiError } from './errors'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(client: AxiosInstance): Promise<string | null> {
  try {
    const response = await client.post<AuthResponsePayload>(
      '/api/v1/auth/refresh',
      undefined,
      { withCredentials: true },
    )
    const token = response.data.accessToken
    if (token) {
      tokenStore.set(token)
      return token
    }
    return null
  } catch {
    tokenStore.clear()
    return null
  }
}

interface AuthResponsePayload {
  accessToken: string
  tokenType?: string
  userInfo?: unknown
}

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    Accept: 'application/json',
  },
  withCredentials: true,
})

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStore.get()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean
    }

    const isRefreshRequest = originalRequest?.url?.includes('/api/v1/auth/refresh')
    const isLoginRequest = originalRequest?.url?.includes('/api/v1/auth/login')

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !isRefreshRequest &&
      !isLoginRequest
    ) {
      originalRequest._retry = true

      if (!refreshPromise) {
        refreshPromise = refreshAccessToken(apiClient).finally(() => {
          refreshPromise = null
        })
      }

      const newToken = await refreshPromise
      if (newToken) {
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return apiClient(originalRequest)
      }
    }

    return Promise.reject(parseApiError(error))
  },
)

export { API_BASE_URL }
