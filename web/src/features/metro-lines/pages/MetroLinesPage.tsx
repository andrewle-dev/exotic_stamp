import { useMemo, useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { Input } from '../../../components/ui/FormField'
import { formatDateTime } from '../../../lib/formatting/date'
import { isForbiddenError } from '../../../lib/api/errors'
import type { LineResponse, MetroStatus } from '../../../types/metro-lines'
import { useDeleteMetroLine, useMetroLinesList } from '../hooks'
import { MetroLineFormDrawer } from '../components/MetroLineFormDrawer'

type StatusFilter = MetroStatus | 'ALL'

export function MetroLinesPage() {
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingLine, setEditingLine] = useState<LineResponse | null>(null)
  const [deletingLine, setDeletingLine] = useState<LineResponse | null>(null)

  const listParams = useMemo(
    () => ({
      page,
      size,
      search: search || undefined,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
    }),
    [page, size, search, statusFilter],
  )

  const { data, isLoading, error, refetch } = useMetroLinesList(listParams)
  const deleteMutation = useDeleteMetroLine()

  const columns: DataTableColumn<LineResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      { id: 'name', header: 'Name', cell: (row) => row.name },
      {
        id: 'displayName',
        header: 'Display name',
        cell: (row) => row.displayName ?? '—',
      },
      {
        id: 'color',
        header: 'Color',
        cell: (row) =>
          row.colorHex ? (
            <span className="inline-flex items-center gap-2">
              <span
                className="h-4 w-4 rounded border border-border"
                style={{ backgroundColor: row.colorHex }}
              />
              <span className="font-mono text-xs">{row.colorHex}</span>
            </span>
          ) : (
            '—'
          ),
      },
      {
        id: 'totalStations',
        header: 'Stations',
        align: 'right',
        cell: (row) => row.totalStations ?? 0,
      },
      {
        id: 'status',
        header: 'Status',
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'sortOrder',
        header: 'Sort',
        align: 'right',
        cell: (row) => row.sortOrder ?? '—',
      },
      {
        id: 'updatedAt',
        header: 'Updated',
        cell: (row) => (
          <span className="text-xs text-muted-foreground">{formatDateTime(row.updatedAt)}</span>
        ),
      },
    ],
    [],
  )

  function openCreate() {
    setEditingLine(null)
    setDrawerOpen(true)
  }

  function openEdit(line: LineResponse) {
    setEditingLine(line)
    setDrawerOpen(true)
  }

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Metro lines access denied" />
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">Metro Lines</h2>
          <p className="text-sm text-muted-foreground">Manage metro line configuration and status.</p>
        </div>
        <Button size="md" onClick={openCreate}>
          <Plus className="h-4 w-4" />
          Create line
        </Button>
      </div>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 sm:flex-row sm:items-end">
        <div className="flex-1 space-y-1">
          <label htmlFor="line-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="line-search"
            placeholder="Search by code or name…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setSearch(searchInput.trim())
                setPage(0)
              }
            }}
          />
        </div>
        <div className="space-y-1">
          <label htmlFor="line-status" className="text-xs font-medium text-muted-foreground">
            Status
          </label>
          <select
            id="line-status"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as StatusFilter)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm sm:w-40"
          >
            <option value="ALL">All</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>
        <Button
          variant="secondary"
          onClick={() => {
            setSearch(searchInput.trim())
            setPage(0)
          }}
        >
          Apply
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={data?.content}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Metro lines"
        rowActions={(row) => (
          <>
            <Button variant="ghost" size="sm" onClick={() => openEdit(row)} aria-label="Edit line">
              <Pencil className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDeletingLine(row)}
              aria-label="Soft delete line"
            >
              <Trash2 className="h-4 w-4 text-destructive" />
            </Button>
          </>
        )}
      />

      {data ? (
        <Pagination
          page={data.page}
          size={data.size}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          onPageChange={setPage}
          onSizeChange={(next) => {
            setSize(next)
            setPage(0)
          }}
        />
      ) : null}

      <MetroLineFormDrawer
        open={drawerOpen}
        line={editingLine}
        onClose={() => {
          setDrawerOpen(false)
          setEditingLine(null)
        }}
      />

      <ConfirmDialog
        open={Boolean(deletingLine)}
        variant="danger"
        title="Soft delete metro line?"
        description={
          deletingLine ? (
            <>
              This will soft-delete <strong>{deletingLine.name}</strong> ({deletingLine.code}). The
              line can be deactivated but is not permanently removed from the database.
            </>
          ) : null
        }
        confirmLabel="Soft delete line"
        loading={deleteMutation.isPending}
        onCancel={() => setDeletingLine(null)}
        onConfirm={async () => {
          if (!deletingLine) return
          await deleteMutation.mutateAsync(deletingLine.id)
          setDeletingLine(null)
        }}
      />
    </div>
  )
}
