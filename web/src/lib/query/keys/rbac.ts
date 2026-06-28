export const rbacKeys = {
  all: ['rbac'] as const,
  roles: () => [...rbacKeys.all, 'roles'] as const,
  role: (roleId: string) => [...rbacKeys.all, 'role', roleId] as const,
  permissions: () => [...rbacKeys.all, 'permissions'] as const,
  rolePermissions: (roleId: string) => [...rbacKeys.all, 'rolePermissions', roleId] as const,
  userRoles: (userId: string) => [...rbacKeys.all, 'userRoles', userId] as const,
}
