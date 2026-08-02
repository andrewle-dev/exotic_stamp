import axios, {
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from 'axios'
import { tokenStore } from '../auth/tokenStore'
import { parseApiError } from './errors'

const LOCAL_API_BASE_URL = 'http://localhost:8080'

function trimTrailingSlashes(value: string): string {
  return value.replace(/\/+$/, '')
}

export function resolveApiBaseUrl(rawValue: string | undefined): string {
  const trimmed = rawValue?.trim()
  if (!trimmed) {
    return LOCAL_API_BASE_URL
  }
  return trimTrailingSlashes(trimmed)
}

const API_BASE_URL = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL)

type Deferred = {
  promise: Promise<void>
  resolve: () => void
}

function createDeferred(): Deferred {
  let resolve!: () => void
  const promise = new Promise<void>((r) => {
    resolve = r
  })
  return { promise, resolve }
}

let refreshPromise: Promise<AuthResponsePayload | null> | null = null
let onSessionInvalidated: (() => void) | null = null
let bootstrapReady = false
let bootstrapDeferred = createDeferred()

export function setSessionInvalidatedHandler(handler: (() => void) | null): void {
  onSessionInvalidated = handler
}

/** Call when silent bootstrap completes so queued protected requests may proceed. */
export function markAuthBootstrapComplete(): void {
  if (bootstrapReady) {
    return
  }
  bootstrapReady = true
  bootstrapDeferred.resolve()
}

export function waitForAuthBootstrap(): Promise<void> {
  return bootstrapReady ? Promise.resolve() : bootstrapDeferred.promise
}

/** Test-only reset between vitest cases. */
export function resetAuthClientCoordinationForTests(): void {
  refreshPromise = null
  onSessionInvalidated = null
  bootstrapReady = false
  bootstrapDeferred = createDeferred()
}

interface AuthResponsePayload {
  accessToken: string
  tokenType?: string
  userInfo?: unknown
  refreshToken?: string
}

/** Single-flight silent refresh shared by bootstrap and 401 interceptor. */
export function refreshAccessTokenOnce(
  client: AxiosInstance = apiClient,
): Promise<AuthResponsePayload | null> {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const response = await client.post<AuthResponsePayload>(
          '/api/v1/auth/refresh',
          undefined,
          { withCredentials: true },
        )
        const token = response.data.accessToken
        if (token) {
          tokenStore.set(token)
          return response.data
        }
        return null
      } catch {
        tokenStore.clear()
        return null
      }
    })().finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    Accept: 'application/json',
    'X-Client-Transport': 'cookie',
  },
  withCredentials: true,
})

apiClient.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const url = config.url ?? ''
  const isAuthBootstrapExempt =
    url.includes('/api/v1/auth/refresh') ||
    url.includes('/api/v1/auth/login') ||
    url.includes('/api/v1/auth/register') ||
    url.includes('/api/v1/auth/forgot-password') ||
    url.includes('/api/v1/auth/reset-password')

  if (!isAuthBootstrapExempt) {
    await waitForAuthBootstrap()
  }

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

    const url = originalRequest?.url ?? ''
    const isRefreshRequest = url.includes('/api/v1/auth/refresh')
    const isLoginRequest = url.includes('/api/v1/auth/login')

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !isRefreshRequest &&
      !isLoginRequest
    ) {
      originalRequest._retry = true

      const refreshed = await refreshAccessTokenOnce(apiClient)
      if (refreshed?.accessToken) {
        originalRequest.headers.Authorization = `Bearer ${refreshed.accessToken}`
        return apiClient(originalRequest)
      }

      onSessionInvalidated?.()
    }

    return Promise.reject(parseApiError(error))
  },
)

export { API_BASE_URL }
