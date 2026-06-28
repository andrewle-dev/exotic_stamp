import type { ApiResponse } from './api'

export interface RoleResponse {
  id: string
  role?: string
  description?: string
  status?: string
  systemRole?: boolean
}

export interface PermissionResponse {
  id: string
  permission?: string
  description?: string
}

export interface CreateRoleRequest {
  roleCode: string
  description?: string
}

export interface UpdateRoleRequest {
  roleCode?: string
  description?: string
  status?: string
}

export interface CreatePermissionRequest {
  permissionCode: string
  description?: string
}

export interface AssignPermissionToRoleRequest {
  permissionCode: string
}

export interface AssignRoleRequest {
  userId: string
  roleName: string
}

export interface RevokeRoleRequest {
  userId: string
  roleName: string
}

export type ApiResponseListRoleResponse = ApiResponse<RoleResponse[]>
export type ApiResponseListPermissionResponse = ApiResponse<PermissionResponse[]>
export type ApiResponseRoleResponse = ApiResponse<RoleResponse>
export type ApiResponsePermissionResponse = ApiResponse<PermissionResponse>
