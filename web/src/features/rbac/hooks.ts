import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  assignPermissionToRole,
  assignRoleToUser,
  createPermission,
  createRole,
  getRole,
  listPermissions,
  listRolePermissions,
  listRoles,
  listUserRoles,
  revokePermissionFromRole,
  revokeRoleFromUser,
  updateRole,
} from '../../lib/api/rbac.api'
import { rbacKeys } from '../../lib/query/keys/rbac'
import type {
  CreatePermissionRequest,
  CreateRoleRequest,
  UpdateRoleRequest,
} from '../../types/rbac'

export function useRoles() {
  return useQuery({
    queryKey: rbacKeys.roles(),
    queryFn: () => listRoles(),
  })
}

export function useRole(roleId: string | undefined) {
  return useQuery({
    queryKey: rbacKeys.role(roleId ?? ''),
    queryFn: () => getRole(roleId!),
    enabled: Boolean(roleId),
  })
}

export function usePermissions() {
  return useQuery({
    queryKey: rbacKeys.permissions(),
    queryFn: () => listPermissions(),
  })
}

export function useRolePermissions(roleId: string | undefined) {
  return useQuery({
    queryKey: rbacKeys.rolePermissions(roleId ?? ''),
    queryFn: () => listRolePermissions(roleId!),
    enabled: Boolean(roleId),
  })
}

export function useCreateRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateRoleRequest) => createRole(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: rbacKeys.roles() })
    },
  })
}

export function useUpdateRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ roleId, body }: { roleId: string; body: UpdateRoleRequest }) =>
      updateRole(roleId, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: rbacKeys.roles() })
      void queryClient.invalidateQueries({ queryKey: rbacKeys.role(variables.roleId) })
    },
  })
}

export function useCreatePermission() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreatePermissionRequest) => createPermission(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: rbacKeys.permissions() })
    },
  })
}

export function useAssignPermissionToRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ roleId, permissionCode }: { roleId: string; permissionCode: string }) =>
      assignPermissionToRole(roleId, permissionCode),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: rbacKeys.rolePermissions(variables.roleId) })
      void queryClient.invalidateQueries({ queryKey: rbacKeys.roles() })
    },
  })
}

export function useRevokePermissionFromRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ roleId, permissionId }: { roleId: string; permissionId: string }) =>
      revokePermissionFromRole(roleId, permissionId),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: rbacKeys.rolePermissions(variables.roleId) })
      void queryClient.invalidateQueries({ queryKey: rbacKeys.roles() })
    },
  })
}

export function useUserRoles(userId: string | undefined, enabled = true) {
  return useQuery({
    queryKey: rbacKeys.userRoles(userId ?? ''),
    queryFn: () => listUserRoles(userId!),
    enabled: Boolean(userId) && enabled,
  })
}

export function useAssignRoleToUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, roleName }: { userId: string; roleName: string }) =>
      assignRoleToUser(userId, roleName),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: rbacKeys.userRoles(variables.userId) })
    },
  })
}

export function useRevokeRoleFromUser() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, roleName }: { userId: string; roleName: string }) =>
      revokeRoleFromUser(userId, roleName),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: rbacKeys.userRoles(variables.userId) })
    },
  })
}
