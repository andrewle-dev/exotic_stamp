import { useMemo, useState } from 'react'
import { Eye, Pencil, Plus } from 'lucide-react'
import { SearchFilterCard } from '../../../components/filters'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import type { RoleResponse } from '../../../types/rbac'
import { useRoles } from '../hooks'
import { RoleFormDrawer } from './RoleFormDrawer'
import { RoleDetailDrawer } from './RoleDetailDrawer'

function filterRoles(roles: RoleResponse[], search: string): RoleResponse[] {
  const needle = search.trim().toLowerCase()
  if (!needle) {
    return roles
  }
  return roles.filter((role) => {
    const code = role.role?.toLowerCase() ?? ''
    const description = role.description?.toLowerCase() ?? ''
    return code.includes(needle) || description.includes(needle)
  })
}

export function RolesTab() {
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingRole, setEditingRole] = useState<RoleResponse | null>(null)
  const [detailRole, setDetailRole] = useState<RoleResponse | null>(null)

  const { data, isLoading, error, refetch } = useRoles()

  const filteredRoles = useMemo(
    () => filterRoles(data ?? [], search),
    [data, search],
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
        header: 'System role',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) =>
          row.systemRole ? <StatusBadge status="ACTIVE" label="System" /> : '—',
      },
    ],
    [],
  )

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-muted-foreground">
          Manage role definitions. System roles are protected from casual edits.
        </p>
        <Button
          size="md"
          onClick={() => {
            setEditingRole(null)
            setDrawerOpen(true)
          }}
        >
          <Plus className="h-4 w-4" />
          Create role
        </Button>
      </div>

      <SearchFilterCard
        id="role-search"
        label="Search"
        placeholder="Search by role or description…"
        value={searchInput}
        onChange={setSearchInput}
        onSubmit={() => setSearch(searchInput.trim())}
      />

      <DataTable
        tableId="rbac-roles"
        columns={columns}
        data={filteredRoles}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Roles"
        actionsWidth={128}
        emptyTitle="No roles found"
        emptyDescription="Create a role or adjust your search."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDetailRole(row)}
              aria-label="View role"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              disabled={row.systemRole}
              title={row.systemRole ? 'System roles cannot be edited' : 'Edit role'}
              onClick={() => {
                setEditingRole(row)
                setDrawerOpen(true)
              }}
              aria-label="Edit role"
            >
              <Pencil className="h-4 w-4" />
            </Button>
          </>
        )}
      />

      <RoleFormDrawer
        open={drawerOpen}
        role={editingRole}
        onClose={() => {
          setDrawerOpen(false)
          setEditingRole(null)
        }}
      />

      <RoleDetailDrawer
        open={Boolean(detailRole)}
        role={detailRole}
        onClose={() => setDetailRole(null)}
        onEdit={(role) => {
          setDetailRole(null)
          setEditingRole(role)
          setDrawerOpen(true)
        }}
      />
    </div>
  )
}
