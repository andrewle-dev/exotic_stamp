/**
 * In-memory access-token holder. Refresh stays in HttpOnly cookie (never here).
 * Intentionally not persisted to localStorage/sessionStorage.
 */
let accessToken: string | null = null

export const tokenStore = {
  get(): string | null {
    return accessToken
  },

  set(token: string): void {
    accessToken = token
  },

  clear(): void {
    accessToken = null
  },
}

/** One-time migration: drop legacy persisted access tokens. */
export function clearLegacyPersistedAccessToken(): void {
  try {
    localStorage.removeItem('exotic_stamp_admin_access_token')
  } catch {
    // ignore
  }
}
