import { apiClient } from './client'
import { unwrapApiResponse, unwrapApiResponseOptional } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  ApiResponseListPermissionResponse,
  ApiResponseListRoleResponse,
  ApiResponsePermissionResponse,
  ApiResponseRoleResponse,
  AssignPermissionToRoleRequest,
  AssignRoleRequest,
  CreatePermissionRequest,
  CreateRoleRequest,
  PermissionResponse,
  RevokeRoleRequest,
  RoleResponse,
  UpdateRoleRequest,
} from '../../types/rbac'

const ROLES_BASE = '/api/v1/roles'
const PERMISSIONS_BASE = '/api/v1/permissions'

export async function listRoles(): Promise<RoleResponse[]> {
  const { data } = await apiClient.get<ApiResponseListRoleResponse>(ROLES_BASE)
  return unwrapApiResponse(data)
}

export async function createRole(payload: CreateRoleRequest): Promise<RoleResponse> {
  const { data } = await apiClient.post<ApiResponseRoleResponse>(ROLES_BASE, payload)
  return unwrapApiResponse(data)
}

export async function getRole(roleId: string): Promise<RoleResponse> {
  const { data } = await apiClient.get<ApiResponseRoleResponse>(`${ROLES_BASE}/${roleId}`)
  return unwrapApiResponse(data)
}

export async function updateRole(roleId: string, payload: UpdateRoleRequest): Promise<RoleResponse> {
  const { data } = await apiClient.patch<ApiResponseRoleResponse>(`${ROLES_BASE}/${roleId}`, payload)
  return unwrapApiResponse(data)
}

export async function listPermissions(): Promise<PermissionResponse[]> {
  const { data } = await apiClient.get<ApiResponseListPermissionResponse>(PERMISSIONS_BASE)
  return unwrapApiResponse(data)
}

export async function createPermission(payload: CreatePermissionRequest): Promise<PermissionResponse> {
  const { data } = await apiClient.post<ApiResponsePermissionResponse>(PERMISSIONS_BASE, payload)
  return unwrapApiResponse(data)
}

export async function listRolePermissions(roleId: string): Promise<PermissionResponse[]> {
  const { data } = await apiClient.get<ApiResponseListPermissionResponse>(
    `${ROLES_BASE}/${roleId}/permissions`,
  )
  return unwrapApiResponse(data)
}

export async function assignPermissionToRole(
  roleId: string,
  permissionCode: string,
): Promise<void> {
  const payload: AssignPermissionToRoleRequest = { permissionCode }
  const { data } = await apiClient.post<ApiResponse<void>>(
    `${ROLES_BASE}/${roleId}/permissions`,
    payload,
  )
  unwrapApiResponseOptional(data)
}

export async function revokePermissionFromRole(
  roleId: string,
  permissionId: string,
): Promise<void> {
  const { data } = await apiClient.delete<ApiResponse<void>>(
    `${ROLES_BASE}/${roleId}/permissions/${permissionId}`,
  )
  unwrapApiResponseOptional(data)
}

export async function listUserRoles(userId: string): Promise<RoleResponse[]> {
  const { data } = await apiClient.get<ApiResponseListRoleResponse>(`${ROLES_BASE}/${userId}/roles`)
  return unwrapApiResponse(data)
}

export async function assignRoleToUser(userId: string, roleName: string): Promise<void> {
  const payload: AssignRoleRequest = { userId, roleName }
  const { data } = await apiClient.post<ApiResponse<void>>(`${ROLES_BASE}/assign`, payload)
  unwrapApiResponseOptional(data)
}

export async function revokeRoleFromUser(userId: string, roleName: string): Promise<void> {
  const payload: RevokeRoleRequest = { userId, roleName }
  const { data } = await apiClient.post<ApiResponse<void>>(`${ROLES_BASE}/revoke`, payload)
  unwrapApiResponseOptional(data)
}
