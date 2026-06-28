import { useMemo, useState } from 'react'
import { useQueries } from '@tanstack/react-query'
import { Check } from 'lucide-react'
import { Input } from '../../../components/ui/FormField'
import { Button } from '../../../components/ui/Button'
import { LoadingSkeleton } from '../../../components/ui/LoadingSkeleton'
import { listRolePermissions } from '../../../lib/api/rbac.api'
import { rbacKeys } from '../../../lib/query/keys/rbac'
import { usePermissions, useRoles } from '../hooks'

interface PermissionMatrixTabProps {
  active: boolean
}

function filterByNeedle(value: string | undefined, needle: string): boolean {
  if (!needle) {
    return true
  }
  return (value ?? '').toLowerCase().includes(needle)
}

export function PermissionMatrixTab({ active }: PermissionMatrixTabProps) {
  const [roleSearchInput, setRoleSearchInput] = useState('')
  const [permissionSearchInput, setPermissionSearchInput] = useState('')
  const [roleSearch, setRoleSearch] = useState('')
  const [permissionSearch, setPermissionSearch] = useState('')

  const { data: roles = [], isLoading: rolesLoading } = useRoles()
  const { data: permissions = [], isLoading: permissionsLoading } = usePermissions()

  const roleNeedle = roleSearch.trim().toLowerCase()
  const permissionNeedle = permissionSearch.trim().toLowerCase()

  const filteredRoles = useMemo(
    () =>
      roles.filter(
        (role) =>
          filterByNeedle(role.role, roleNeedle) || filterByNeedle(role.description, roleNeedle),
      ),
    [roles, roleNeedle],
  )

  const filteredPermissions = useMemo(
    () =>
      permissions.filter(
        (permission) =>
          filterByNeedle(permission.permission, permissionNeedle) ||
          filterByNeedle(permission.description, permissionNeedle),
      ),
    [permissions, permissionNeedle],
  )

  const rolePermissionQueries = useQueries({
    queries: filteredRoles.map((role) => ({
      queryKey: rbacKeys.rolePermissions(role.id),
      queryFn: () => listRolePermissions(role.id),
      enabled: active && Boolean(role.id),
      staleTime: 60_000,
    })),
  })

  const matrix = useMemo(() => {
    const permissionCodesByRole = new Map<string, Set<string>>()
    filteredRoles.forEach((role, index) => {
      const query = rolePermissionQueries[index]
      const codes = new Set(
        (query?.data ?? [])
          .map((permission) => permission.permission)
          .filter((code): code is string => Boolean(code)),
      )
      permissionCodesByRole.set(role.id, codes)
    })
    return permissionCodesByRole
  }, [filteredRoles, rolePermissionQueries])

  const isMatrixLoading =
    active &&
    (rolesLoading ||
      permissionsLoading ||
      rolePermissionQueries.some((query) => query.isLoading))

  const loadedCount = rolePermissionQueries.filter((query) => query.isSuccess).length

  return (
    <div className="space-y-4">
      <p className="text-sm text-muted-foreground">
        Read-only overview of which permissions are assigned to each role. Role permissions are
        loaded when this tab is open.
      </p>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:items-end">
        <div className="min-w-[200px] flex-1 space-y-1">
          <label htmlFor="matrix-role-search" className="text-xs font-medium text-muted-foreground">
            Filter roles
          </label>
          <Input
            id="matrix-role-search"
            placeholder="Search roles…"
            value={roleSearchInput}
            onChange={(e) => setRoleSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setRoleSearch(roleSearchInput.trim())
              }
            }}
          />
        </div>
        <div className="min-w-[200px] flex-1 space-y-1">
          <label
            htmlFor="matrix-permission-search"
            className="text-xs font-medium text-muted-foreground"
          >
            Filter permissions
          </label>
          <Input
            id="matrix-permission-search"
            placeholder="Search permissions…"
            value={permissionSearchInput}
            onChange={(e) => setPermissionSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setPermissionSearch(permissionSearchInput.trim())
              }
            }}
          />
        </div>
        <Button
          variant="secondary"
          onClick={() => {
            setRoleSearch(roleSearchInput.trim())
            setPermissionSearch(permissionSearchInput.trim())
          }}
        >
          Apply filters
        </Button>
      </div>

      {isMatrixLoading ? (
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <LoadingSkeleton className="h-4 w-4 rounded-full" />
          Loading matrix… ({loadedCount}/{filteredRoles.length} roles)
        </div>
      ) : null}

      <div className="overflow-x-auto rounded-lg border border-border">
        <table className="w-full min-w-[640px] border-collapse text-left text-sm">
          <caption className="sr-only">Role permission matrix</caption>
          <thead>
            <tr className="border-b border-border bg-secondary/50">
              <th
                scope="col"
                className="sticky left-0 z-10 bg-secondary/95 px-3 py-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground"
              >
                Role
              </th>
              {filteredPermissions.map((permission) => (
                <th
                  key={permission.id}
                  scope="col"
                  className="px-2 py-2 text-center text-[10px] font-semibold uppercase tracking-wide text-muted-foreground"
                  title={permission.description}
                >
                  <span className="block max-w-[6rem] truncate">
                    {permission.permission ?? permission.id}
                  </span>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filteredRoles.length === 0 ? (
              <tr>
                <td colSpan={Math.max(filteredPermissions.length, 1) + 1} className="px-4 py-8 text-center text-muted-foreground">
                  No roles match the current filter.
                </td>
              </tr>
            ) : (
              filteredRoles.map((role) => {
                const assigned = matrix.get(role.id) ?? new Set<string>()
                return (
                  <tr key={role.id} className="border-b border-border last:border-0">
                    <th
                      scope="row"
                      className="sticky left-0 z-10 bg-card px-3 py-2 font-medium text-foreground"
                    >
                      <span className="block max-w-[12rem] truncate" title={role.description}>
                        {role.role ?? role.id}
                      </span>
                    </th>
                    {filteredPermissions.map((permission) => {
                      const code = permission.permission ?? ''
                      const hasPermission = code ? assigned.has(code) : false
                      return (
                        <td key={permission.id} className="px-2 py-2 text-center">
                          {hasPermission ? (
                            <span className="inline-flex text-emerald-600" title="Assigned">
                              <Check className="h-4 w-4" aria-label="Assigned" />
                            </span>
                          ) : (
                            <span className="text-muted-foreground/40" aria-hidden="true">
                              —
                            </span>
                          )}
                        </td>
                      )
                    })}
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
