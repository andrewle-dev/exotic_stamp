import { apiClient } from '../../lib/api/client'
import { tokenStore } from '../../lib/auth/tokenStore'
import type { AuthResponse, LoginRequest, UserResponse } from './types'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/v1/auth/login', request, {
    withCredentials: true,
  })

  if (data.accessToken) {
    tokenStore.set(data.accessToken)
  }

  return data
}

export async function refreshSession(): Promise<AuthResponse | null> {
  try {
    const { data } = await apiClient.post<AuthResponse>(
      '/api/v1/auth/refresh',
      undefined,
      { withCredentials: true },
    )

    if (data.accessToken) {
      tokenStore.set(data.accessToken)
    }

    return data
  } catch {
    tokenStore.clear()
    return null
  }
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post('/api/v1/auth/logout')
  } finally {
    tokenStore.clear()
  }
}

export async function getCurrentUser(): Promise<UserResponse> {
  const { data } = await apiClient.get<UserResponse>('/api/v1/users/me')
  return data
}
