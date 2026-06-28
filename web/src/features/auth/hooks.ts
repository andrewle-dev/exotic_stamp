import { createContext, useContext } from 'react'
import type { AuthUserInfo, LoginRequest, UserResponse } from './types'

export interface AuthContextValue {
  user: AuthUserInfo | null
  profile: UserResponse | null
  isAuthenticated: boolean
  isInitializing: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
