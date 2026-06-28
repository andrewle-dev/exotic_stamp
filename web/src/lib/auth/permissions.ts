import type { AuthUserInfo } from '../../features/auth/types'

/** Strip Spring Security ROLE_ prefix for comparison. */
export function normalizeRoleName(role: string): string {
  return role.startsWith('ROLE_') ? role.slice(5) : role
}

export function hasRole(user: AuthUserInfo | null | undefined, roleName: string): boolean {
  if (!user?.roles?.length) {
    return false
  }
  const target = normalizeRoleName(roleName).toUpperCase()
  return user.roles.some((role) => normalizeRoleName(role).toUpperCase() === target)
}

/**
 * Role-based helpers only. Auth exposes role names, not fine-grained authorities.
 * Backend 403 remains authoritative for permission-gated actions (e.g. RBAC_ADMIN).
 */
export function canManageRbac(user: AuthUserInfo | null | undefined): boolean {
  return hasRole(user, 'ADMIN')
}

export function canManageMetro(user: AuthUserInfo | null | undefined): boolean {
  return hasRole(user, 'ADMIN')
}

export function canManageRewards(user: AuthUserInfo | null | undefined): boolean {
  return hasRole(user, 'ADMIN')
}

export function canUploadPublicAsset(user: AuthUserInfo | null | undefined): boolean {
  return hasRole(user, 'ADMIN')
}
