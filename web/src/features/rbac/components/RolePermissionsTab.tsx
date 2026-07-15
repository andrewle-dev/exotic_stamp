import { useMemo, useState } from 'react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { FormField, Input } from '../../../components/ui/FormField'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import type { PermissionResponse } from '../../../types/rbac'
import {
  useAssignPermissionToRole,
  usePermissions,
  useRevokePermissionFromRole,
  useRolePermissions,
  useRoles,
} from '../hooks'
import { isSensitivePermissionCode } from '../utils/sensitive-permission'

export function RolePermissionsTab() {
  const [selectedRoleId, setSelectedRoleId] = useState('')
  const [permissionSelection, setPermissionSelection] = useState('')
  const [customPermissionCode, setCustomPermissionCode] = useState('')
  const [useCustomCode, setUseCustomCode] = useState(false)
  const [pendingAssignCode, setPendingAssignCode] = useState<string | null>(null)
  const [revokingPermission, setRevokingPermission] = useState<PermissionResponse | null>(null)

  const { data: roles = [], isLoading: rolesLoading } = useRoles()
  const { data: allPermissions = [] } = usePermissions()
  const {
    data: rolePermissions = [],
    isLoading: permissionsLoading,
    error,
    refetch,
  } = useRolePermissions(selectedRoleId || undefined)

  const assignMutation = useAssignPermissionToRole()
  const revokeMutation = useRevokePermissionFromRole()

  const selectedRole = useMemo(
    () => roles.find((role) => role.id === selectedRoleId) ?? null,
    [roles, selectedRoleId],
  )

  const permissionCodeToAssign = useCustomCode
    ? customPermissionCode.trim()
    : permissionSelection.trim()

  const columns: DataTableColumn<PermissionResponse>[] = useMemo(
    () => [
      {
        id: 'permission',
        header: 'Permission',
        ...COL_WIDTH.name,
        defaultWidth: 220,
        cell: (row) => row.permission ?? '—',
      },
      {
        id: 'description',
        header: 'Description',
        ...COL_WIDTH.description,
        defaultWidth: 320,
        cell: (row) => row.description?.trim() || '—',
      },
    ],
    [],
  )

  async function performAssign(permissionCode: string) {
    if (!selectedRoleId) {
      return
    }
    await assignMutation.mutateAsync({ roleId: selectedRoleId, permissionCode })
    setPermissionSelection('')
    setCustomPermissionCode('')
    setPendingAssignCode(null)
  }

  function handleAssignClick() {
    if (!selectedRoleId || !permissionCodeToAssign) {
      return
    }
    if (isSensitivePermissionCode(permissionCodeToAssign)) {
      setPendingAssignCode(permissionCodeToAssign)
      return
    }
    void performAssign(permissionCodeToAssign)
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-muted-foreground">
        Select a role to view its permissions, then assign or revoke access.
      </p>

      <div className="rounded-lg border border-border bg-card p-4">
        <FormField label="Role" htmlFor="role-permissions-role">
          <select
            id="role-permissions-role"
            value={selectedRoleId}
            onChange={(e) => setSelectedRoleId(e.target.value)}
            disabled={rolesLoading}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm sm:max-w-md"
          >
            <option value="">Select a role…</option>
            {roles.map((role) => (
              <option key={role.id} value={role.id}>
                {role.role ?? role.id}
                {role.systemRole ? ' (system)' : ''}
              </option>
            ))}
          </select>
        </FormField>
      </div>

      {selectedRoleId ? (
        <>
          <div className="rounded-lg border border-border bg-card p-4 space-y-4">
            <h3 className="text-sm font-semibold text-foreground">Assign permission</h3>

            <div className="flex flex-wrap items-center gap-3">
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="radio"
                  checked={!useCustomCode}
                  onChange={() => setUseCustomCode(false)}
                />
                From list
              </label>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="radio"
                  checked={useCustomCode}
                  onChange={() => setUseCustomCode(true)}
                />
                Enter code
              </label>
            </div>

            {!useCustomCode ? (
              <FormField label="Permission" htmlFor="assign-permission-select">
                <select
                  id="assign-permission-select"
                  value={permissionSelection}
                  onChange={(e) => setPermissionSelection(e.target.value)}
                  className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm sm:max-w-md"
                >
                  <option value="">Select a permission…</option>
                  {allPermissions.map((permission) => (
                    <option key={permission.id} value={permission.permission ?? ''}>
                      {permission.permission}
                    </option>
                  ))}
                </select>
              </FormField>
            ) : (
              <FormField label="Permission code" htmlFor="assign-permission-code">
                <Input
                  id="assign-permission-code"
                  placeholder="PERMISSION_CODE"
                  value={customPermissionCode}
                  onChange={(e) => setCustomPermissionCode(e.target.value)}
                  className="sm:max-w-md"
                />
              </FormField>
            )}

            {assignMutation.error ? <ApiErrorAlert error={assignMutation.error} /> : null}

            <Button
              size="md"
              disabled={!permissionCodeToAssign || assignMutation.isPending || selectedRole?.systemRole}
              title={selectedRole?.systemRole ? 'System role permissions should not be changed casually' : undefined}
              onClick={handleAssignClick}
            >
              {assignMutation.isPending ? 'Assigning…' : 'Assign permission'}
            </Button>

            {selectedRole?.systemRole ? (
              <p className="text-xs text-muted-foreground">
                System roles are protected and should not be modified casually.
              </p>
            ) : null}
          </div>

          <DataTable
            tableId="rbac-role-permissions"
            columns={columns}
            data={rolePermissions}
            getRowId={(row) => row.id}
            isLoading={permissionsLoading}
            error={error}
            onRetry={() => void refetch()}
            caption={`Permissions for ${selectedRole?.role ?? 'role'}`}
            actionsWidth={72}
            emptyTitle="No permissions assigned"
            emptyDescription="Assign a permission using the form above."
            rowActions={(row) => (
              <Button
                variant="ghost"
                size="sm"
                className="text-destructive"
                disabled={selectedRole?.systemRole}
                onClick={() => setRevokingPermission(row)}
                aria-label="Revoke permission"
              >
                Revoke
              </Button>
            )}
          />
        </>
      ) : null}

      <ConfirmDialog
        open={Boolean(pendingAssignCode)}
        variant="warning"
        title="Assign sensitive permission?"
        description={
          pendingAssignCode ? (
            <>
              You are about to assign <strong>{pendingAssignCode}</strong> to role{' '}
              <strong>{selectedRole?.role}</strong>. This permission may grant elevated access.
            </>
          ) : null
        }
        confirmLabel="Assign permission"
        loading={assignMutation.isPending}
        onCancel={() => setPendingAssignCode(null)}
        onConfirm={() => {
          if (pendingAssignCode) {
            void performAssign(pendingAssignCode)
          }
        }}
      />

      <ConfirmDialog
        open={Boolean(revokingPermission)}
        variant="danger"
        title="Revoke permission from role?"
        description={
          revokingPermission ? (
            <>
              Remove <strong>{revokingPermission.permission}</strong> from role{' '}
              <strong>{selectedRole?.role}</strong>? Users with this role may immediately lose
              access.
            </>
          ) : null
        }
        confirmLabel="Revoke permission"
        loading={revokeMutation.isPending}
        onCancel={() => setRevokingPermission(null)}
        onConfirm={async () => {
          if (!revokingPermission || !selectedRoleId) {
            return
          }
          await revokeMutation.mutateAsync({
            roleId: selectedRoleId,
            permissionId: revokingPermission.id,
          })
          setRevokingPermission(null)
        }}
      />
    </div>
  )
}
