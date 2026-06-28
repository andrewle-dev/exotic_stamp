import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { getCurrentUser, login as loginApi, logout as logoutApi, refreshSession } from './api'
import { AuthContext, type AuthContextValue } from './hooks'
import type { AuthUserInfo, UserResponse } from './types'
import { tokenStore } from '../../lib/auth/tokenStore'
import { parseApiError } from '../../lib/api/errors'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUserInfo | null>(null)
  const [profile, setProfile] = useState<UserResponse | null>(null)
  const [isInitializing, setIsInitializing] = useState(true)

  const refreshUser = useCallback(async () => {
    const profileData = await getCurrentUser()
    setProfile(profileData)
    setUser((current) =>
      current ?? {
        id: profileData.id,
        email: profileData.email,
        username: profileData.username,
        roles: [],
      },
    )
  }, [])

  useEffect(() => {
    let cancelled = false

    async function bootstrap() {
      try {
        const existingToken = tokenStore.get()
        if (!existingToken) {
          const refreshed = await refreshSession()
          if (!refreshed) {
            return
          }
          if (!cancelled) {
            setUser(refreshed.userInfo)
          }
        }

        if (!cancelled) {
          await refreshUser()
        }
      } catch {
        tokenStore.clear()
        if (!cancelled) {
          setUser(null)
          setProfile(null)
        }
      } finally {
        if (!cancelled) {
          setIsInitializing(false)
        }
      }
    }

    void bootstrap()

    return () => {
      cancelled = true
    }
  }, [refreshUser])

  const login = useCallback(async (request: Parameters<AuthContextValue['login']>[0]) => {
    const response = await loginApi(request)
    setUser(response.userInfo)
    await refreshUser()
  }, [refreshUser])

  const logout = useCallback(async () => {
    try {
      await logoutApi()
    } catch (error) {
      parseApiError(error)
    } finally {
      setUser(null)
      setProfile(null)
      tokenStore.clear()
    }
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      profile,
      isAuthenticated: Boolean(user && tokenStore.get()),
      isInitializing,
      login,
      logout,
      refreshUser,
    }),
    [user, profile, isInitializing, login, logout, refreshUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
