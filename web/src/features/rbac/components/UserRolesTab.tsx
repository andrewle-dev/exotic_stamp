import { useMemo, useState } from 'react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { FormField, Input } from '../../../components/ui/FormField'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { useAuth } from '../../auth/hooks'
import type { RoleResponse } from '../../../types/rbac'
import {
  useAssignRoleToUser,
  useRevokeRoleFromUser,
  useRoles,
  useUserRoles,
} from '../hooks'
import { isSelfUserId, SELF_LOCKOUT_WARNING } from '../utils/self-lockout'

const UUID_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

type PendingUserRoleAction =
  | { type: 'assign'; roleName: string }
  | { type: 'revoke'; roleName: string }
  | null

export function UserRolesTab() {
  const { user } = useAuth()
  const [userIdInput, setUserIdInput] = useState('')
  const [loadedUserId, setLoadedUserId] = useState<string | undefined>()
  const [selectedRoleName, setSelectedRoleName] = useState('')
  const [inputError, setInputError] = useState<string | null>(null)
  const [pendingAction, setPendingAction] = useState<PendingUserRoleAction>(null)

  const { data: allRoles = [] } = useRoles()
  const {
    data: userRoles = [],
    isLoading,
    error,
    refetch,
    isFetched,
  } = useUserRoles(loadedUserId)

  const assignMutation = useAssignRoleToUser()
  const revokeMutation = useRevokeRoleFromUser()
  const activeMutation = pendingAction?.type === 'assign' ? assignMutation : revokeMutation

  const isSelf = isSelfUserId(loadedUserId ?? userIdInput, user?.id)

  const assignableRoles = useMemo(
    () =>
      allRoles.filter(
        (role) => role.role && !userRoles.some((userRole) => userRole.role === role.role),
      ),
    [allRoles, userRoles],
  )

  const columns: DataTableColumn<RoleResponse>[] = useMemo(
    () => [
      {
        id: 'role',
        header: 'Role',
        ...COL_WIDTH.name,
        cell: (row) => row.role ?? '—',
      },
      {
        id: 'description',
        header: 'Description',
        ...COL_WIDTH.description,
        cell: (row) => row.description?.trim() || '—',
      },
      {
        id: 'status',
        header: 'Status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => (row.status ? <StatusBadge status={row.status} /> : '—'),
      },
      {
        id: 'systemRole',
        header: 'System',
        ...COL_WIDTH.badgeSm,
        cell: (row) => (row.systemRole ? 'Yes' : '—'),
      },
    ],
    [],
  )

  function handleLoadRoles() {
    const trimmed = userIdInput.trim()
    if (!trimmed) {
      setInputError('User ID is required')
      return
    }
    if (!UUID_PATTERN.test(trimmed)) {
      setInputError('User ID must be a valid UUID')
      return
    }
    setInputError(null)
    if (trimmed === loadedUserId) {
      void refetch()
      return
    }
    setLoadedUserId(trimmed)
    setSelectedRoleName('')
  }

  function requestAssign() {
    if (!loadedUserId || !selectedRoleName) {
      return
    }
    setPendingAction({ type: 'assign', roleName: selectedRoleName })
  }

  function requestRevoke(roleName: string) {
    if (!loadedUserId) {
      return
    }
    setPendingAction({ type: 'revoke', roleName })
  }

  async function confirmPendingAction() {
    if (!loadedUserId || !pendingAction) {
      return
    }
    if (pendingAction.type === 'assign') {
      await assignMutation.mutateAsync({
        userId: loadedUserId,
        roleName: pendingAction.roleName,
      })
      setSelectedRoleName('')
    } else {
      await revokeMutation.mutateAsync({
        userId: loadedUserId,
        roleName: pendingAction.roleName,
      })
    }
    setPendingAction(null)
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-muted-foreground">
        Enter a user ID to load and manage role assignments. There is no user list API — use a known
        UUID from your identity system.
      </p>

      <div className="rounded-lg border border-border bg-card p-4 space-y-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="min-w-[280px] flex-1 space-y-1">
            <FormField label="User ID" htmlFor="user-roles-id" error={inputError ?? undefined}>
              <Input
                id="user-roles-id"
                placeholder="00000000-0000-0000-0000-000000000000"
                value={userIdInput}
                onChange={(e) => {
                  setUserIdInput(e.target.value)
                  if (inputError) {
                    setInputError(null)
                  }
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleLoadRoles()
                  }
                }}
              />
            </FormField>
          </div>
          <Button variant="secondary" onClick={handleLoadRoles}>
            Load user roles
          </Button>
        </div>

        {isSelf && loadedUserId ? (
          <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            {SELF_LOCKOUT_WARNING}
          </p>
        ) : null}
      </div>

      {loadedUserId ? (
        <>
          <div className="rounded-lg border border-border bg-card p-4 space-y-4">
            <h3 className="text-sm font-semibold text-foreground">Assign role</h3>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
              <FormField label="Role" htmlFor="assign-user-role">
                <select
                  id="assign-user-role"
                  value={selectedRoleName}
                  onChange={(e) => setSelectedRoleName(e.target.value)}
                  className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm sm:min-w-[240px]"
                >
                  <option value="">Select a role…</option>
                  {assignableRoles.map((role) => (
                    <option key={role.id} value={role.role ?? ''}>
                      {role.role}
                    </option>
                  ))}
                </select>
              </FormField>
              <Button
                size="md"
                disabled={!selectedRoleName || assignMutation.isPending}
                onClick={requestAssign}
              >
                Assign role
              </Button>
            </div>
            {assignMutation.error ? <ApiErrorAlert error={assignMutation.error} /> : null}
          </div>

          <DataTable
            tableId="rbac-user-roles"
            columns={columns}
            data={userRoles}
            getRowId={(row) => row.id}
            isLoading={isLoading}
            error={error}
            onRetry={() => void refetch()}
            caption={`Roles for user ${loadedUserId}`}
            actionsWidth={72}
            emptyTitle={isFetched ? 'No roles assigned' : 'Loading…'}
            emptyDescription="Assign a role using the form above."
            rowActions={(row) => (
              <Button
                variant="ghost"
                size="sm"
                className="text-destructive"
                onClick={() => requestRevoke(row.role ?? '')}
                disabled={!row.role}
                aria-label="Revoke role"
              >
                Revoke
              </Button>
            )}
          />
        </>
      ) : null}

      <ConfirmDialog
        open={Boolean(pendingAction)}
        variant={pendingAction?.type === 'revoke' || isSelf ? 'danger' : 'warning'}
        title={
          pendingAction?.type === 'assign'
            ? 'Assign role to user?'
            : 'Revoke role from user?'
        }
        description={
          pendingAction && loadedUserId ? (
            <>
              {pendingAction.type === 'assign' ? 'Assign' : 'Revoke'}{' '}
              <strong>{pendingAction.roleName}</strong>{' '}
              {pendingAction.type === 'assign' ? 'to' : 'from'} user{' '}
              <strong className="font-mono text-xs">{loadedUserId}</strong>?
              {pendingAction.type === 'revoke' ? (
                <> The user may immediately lose access granted by this role.</>
              ) : null}
              {isSelf ? (
                <p className="mt-2 font-medium text-foreground">{SELF_LOCKOUT_WARNING}</p>
              ) : null}
            </>
          ) : null
        }
        confirmLabel={pendingAction?.type === 'assign' ? 'Assign role' : 'Revoke role'}
        loading={activeMutation.isPending}
        onCancel={() => setPendingAction(null)}
        onConfirm={() => void confirmPendingAction()}
      />
    </div>
  )
}
