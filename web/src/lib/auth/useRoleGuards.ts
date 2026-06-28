import { useMemo } from 'react'
import { useAuth } from '../../features/auth/hooks'
import {
  canManageMetro,
  canManageRbac,
  canManageRewards,
  canUploadPublicAsset,
  hasRole,
} from './permissions'

/**
 * Role-based guards from auth session. Fine-grained authorities are not exposed by the API;
 * backend 403 remains authoritative.
 */
export function useRoleGuards() {
  const { user } = useAuth()

  return useMemo(
    () => ({
      user,
      hasRole: (roleName: string) => hasRole(user, roleName),
      canManageRbac: () => canManageRbac(user),
      canManageMetro: () => canManageMetro(user),
      canManageRewards: () => canManageRewards(user),
      canUploadPublicAsset: () => canUploadPublicAsset(user),
    }),
    [user],
  )
}
