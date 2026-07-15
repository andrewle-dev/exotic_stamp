import { useMemo, useState } from 'react'
import { Plus } from 'lucide-react'
import { SearchFilterCard } from '../../../components/filters'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
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
        ...COL_WIDTH.name,
        defaultWidth: 220,
        cell: (row) => row.permission ?? '—',
      },
      {
        id: 'description',
        header: 'Description',
        ...COL_WIDTH.description,
        defaultWidth: 360,
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

      <SearchFilterCard
        id="permission-search"
        label="Search"
        placeholder="Search by permission or description…"
        value={searchInput}
        onChange={setSearchInput}
        onSubmit={() => setSearch(searchInput.trim())}
      />

      <DataTable
        tableId="rbac-permissions"
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
