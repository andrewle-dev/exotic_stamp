import { useMemo, useState } from 'react'
import { Plus } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Input } from '../../../components/ui/FormField'
import type { PermissionResponse } from '../../../types/rbac'
import { usePermissions } from '../hooks'
import { PermissionFormDrawer } from './PermissionFormDrawer'

function filterPermissions(permissions: PermissionResponse[], search: string): PermissionResponse[] {
  const needle = search.trim().toLowerCase()
  if (!needle) {
    return permissions
  }
  return permissions.filter((permission) => {
    const code = permission.permission?.toLowerCase() ?? ''
    const description = permission.description?.toLowerCase() ?? ''
    return code.includes(needle) || description.includes(needle)
  })
}

export function PermissionsTab() {
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)

  const { data, isLoading, error, refetch } = usePermissions()

  const filteredPermissions = useMemo(
    () => filterPermissions(data ?? [], search),
    [data, search],
  )

  const columns: DataTableColumn<PermissionResponse>[] = useMemo(
    () => [
      {
        id: 'permission',
        header: 'Permission',
        cell: (row) => row.permission ?? '—',
      },
      {
        id: 'description',
        header: 'Description',
        cell: (row) => row.description?.trim() || '—',
      },
    ],
    [],
  )

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-muted-foreground">
          Define permission codes that can be assigned to roles.
        </p>
        <Button size="md" onClick={() => setDrawerOpen(true)}>
          <Plus className="h-4 w-4" />
          Create permission
        </Button>
      </div>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 sm:flex-row sm:items-end">
        <div className="min-w-[200px] flex-1 space-y-1">
          <label htmlFor="permission-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="permission-search"
            placeholder="Search by permission or description…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setSearch(searchInput.trim())
              }
            }}
          />
        </div>
        <Button variant="secondary" onClick={() => setSearch(searchInput.trim())}>
          Apply
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={filteredPermissions}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Permissions"
        emptyTitle="No permissions found"
        emptyDescription="Create a permission or adjust your search."
      />

      <PermissionFormDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} />
    </div>
  )
}
