import { useState } from 'react'
import { cn } from '../../../lib/utils/cn'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { isForbiddenError } from '../../../lib/api/errors'
import { useRoles } from '../hooks'
import { RolesTab } from '../components/RolesTab'
import { PermissionsTab } from '../components/PermissionsTab'
import { RolePermissionsTab } from '../components/RolePermissionsTab'
import { UserRolesTab } from '../components/UserRolesTab'
import { PermissionMatrixTab } from '../components/PermissionMatrixTab'

type RbacTab = 'roles' | 'permissions' | 'role-permissions' | 'user-roles' | 'matrix'

const TABS: { id: RbacTab; label: string }[] = [
  { id: 'roles', label: 'Roles' },
  { id: 'permissions', label: 'Permissions' },
  { id: 'role-permissions', label: 'Role Permissions' },
  { id: 'user-roles', label: 'User Roles' },
  { id: 'matrix', label: 'Permission Matrix' },
]

export function RbacPage() {
  const [activeTab, setActiveTab] = useState<RbacTab>('roles')
  const { error: rolesError } = useRoles()

  if (rolesError && isForbiddenError(rolesError)) {
    return <PermissionDeniedState title="RBAC access denied" />
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-semibold text-foreground">RBAC</h2>
        <p className="text-sm text-muted-foreground">
          Manage roles, permissions, role assignments, and user role mappings. Sensitive changes
          require confirmation.
        </p>
      </div>

      <div className="border-b border-border">
        <nav className="-mb-px flex flex-wrap gap-1" aria-label="RBAC sections">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={cn(
                'border-b-2 px-4 py-2.5 text-xs font-semibold uppercase tracking-wide transition-colors',
                activeTab === tab.id
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:border-border hover:text-foreground',
              )}
              aria-current={activeTab === tab.id ? 'page' : undefined}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {activeTab === 'roles' ? <RolesTab /> : null}
      {activeTab === 'permissions' ? <PermissionsTab /> : null}
      {activeTab === 'role-permissions' ? <RolePermissionsTab /> : null}
      {activeTab === 'user-roles' ? <UserRolesTab /> : null}
      {activeTab === 'matrix' ? <PermissionMatrixTab active /> : null}
    </div>
  )
}
