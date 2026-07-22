import { apiClient, refreshAccessTokenOnce } from '../../lib/api/client'
import { tokenStore } from '../../lib/auth/tokenStore'
import type {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  ResetPasswordRequest,
  UserResponse,
} from './types'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>(
    '/api/v1/auth/login',
    request,
    { withCredentials: true },
  )

  if (data.accessToken) {
    tokenStore.set(data.accessToken)
  }

  return data
}

/** Silent session restore via HttpOnly refresh cookie (single-flight). */
export async function refreshSession(): Promise<AuthResponse | null> {
  const data = await refreshAccessTokenOnce(apiClient)
  if (!data?.accessToken) {
    return null
  }
  return data as AuthResponse
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post('/api/v1/auth/logout', undefined, {
      withCredentials: true,
    })
  } finally {
    tokenStore.clear()
  }
}

export async function logoutAll(): Promise<void> {
  try {
    await apiClient.post('/api/v1/auth/logout-all', undefined, {
      withCredentials: true,
    })
  } finally {
    tokenStore.clear()
  }
}

export async function changePassword(payload: {
  currentPassword: string
  newPassword: string
  confirmNewPassword: string
}): Promise<void> {
  await apiClient.post('/api/v1/auth/change-password', payload, {
    withCredentials: true,
  })
  tokenStore.clear()
}

export async function getCurrentUser(): Promise<UserResponse> {
  const { data } = await apiClient.get<UserResponse>('/api/v1/users/me')
  return data
}

export async function forgotPassword(
  request: ForgotPasswordRequest,
): Promise<void> {
  await apiClient.post('/api/v1/auth/forgot-password', request)
}

/** Alias used by LoginForm. */
export const requestPasswordReset = forgotPassword

export async function resetPassword(
  request: ResetPasswordRequest,
): Promise<void> {
  await apiClient.post('/api/v1/auth/reset-password', request)
}
