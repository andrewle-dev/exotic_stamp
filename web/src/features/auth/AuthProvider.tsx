import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useQueryClient } from '@tanstack/react-query'
import {
  getCurrentUser,
  login as loginApi,
  logout as logoutApi,
  logoutAll as logoutAllApi,
  refreshSession,
} from './api'
import { AuthContext, type AuthContextValue, type AuthStatus } from './hooks'
import type { AuthUserInfo, UserResponse } from './types'
import {
  clearLegacyPersistedAccessToken,
  tokenStore,
} from '../../lib/auth/tokenStore'
import { parseApiError } from '../../lib/api/errors'
import {
  markAuthBootstrapComplete,
  setSessionInvalidatedHandler,
} from '../../lib/api/client'

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [user, setUser] = useState<AuthUserInfo | null>(null)
  const [profile, setProfile] = useState<UserResponse | null>(null)
  const [status, setStatus] = useState<AuthStatus>('restoring')

  const clearUserScopedCache = useCallback(() => {
    queryClient.clear()
  }, [queryClient])

  const markUnauthenticated = useCallback(() => {
    tokenStore.clear()
    setUser(null)
    setProfile(null)
    setStatus('unauthenticated')
    clearUserScopedCache()
  }, [clearUserScopedCache])

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
    clearLegacyPersistedAccessToken()
  }, [])

  useEffect(() => {
    setSessionInvalidatedHandler(() => {
      markUnauthenticated()
    })
    return () => setSessionInvalidatedHandler(null)
  }, [markUnauthenticated])

  useEffect(() => {
    let cancelled = false

    async function bootstrap() {
      setStatus('restoring')
      try {
        // Always silent-refresh first so access stays memory-only across reloads.
        const refreshed = await refreshSession()
        // Unlock the API client before any authenticated follow-up calls (e.g. /users/me).
        markAuthBootstrapComplete()
        if (!refreshed) {
          if (!cancelled) {
            markUnauthenticated()
          }
          return
        }
        if (!cancelled) {
          setUser(refreshed.userInfo)
        }
        if (!cancelled) {
          await refreshUser()
          setStatus('authenticated')
        }
      } catch {
        markAuthBootstrapComplete()
        if (!cancelled) {
          markUnauthenticated()
        }
      }
    }

    void bootstrap()

    return () => {
      cancelled = true
    }
  }, [refreshUser, markUnauthenticated])

  const login = useCallback(
    async (request: Parameters<AuthContextValue['login']>[0]) => {
      const response = await loginApi(request)
      clearUserScopedCache()
      setUser(response.userInfo)
      await refreshUser()
      setStatus('authenticated')
    },
    [refreshUser, clearUserScopedCache],
  )

  const logout = useCallback(async () => {
    try {
      await logoutApi()
    } catch (error) {
      parseApiError(error)
    } finally {
      markUnauthenticated()
    }
  }, [markUnauthenticated])

  const logoutAll = useCallback(async () => {
    try {
      await logoutAllApi()
    } catch (error) {
      parseApiError(error)
    } finally {
      markUnauthenticated()
    }
  }, [markUnauthenticated])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      profile,
      status,
      isAuthenticated: status === 'authenticated' && Boolean(user && tokenStore.get()),
      isInitializing: status === 'restoring',
      login,
      logout,
      logoutAll,
      refreshUser,
    }),
    [user, profile, status, login, logout, logoutAll, refreshUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
