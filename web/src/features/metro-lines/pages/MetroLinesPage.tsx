import { useMemo, useState } from 'react'
import { ArrowUpDown, Pencil, Plus, Trash2 } from 'lucide-react'
import {
  ActiveFilterTags,
  FilterGroup,
  FilterSelect,
  FilterSummaryText,
  ListFilterToolbar,
  buildLabeledFilterTag,
  buildSearchFilterTag,
  collectFilterTags,
  countAppliedAdvancedFilters,
  useDraftAppliedFilters,
} from '../../../components/filters'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDateTime } from '../../../lib/formatting/date'
import { isForbiddenError } from '../../../lib/api/errors'
import type { LineResponse } from '../../../types/metro-lines'
import { useDeleteMetroLine, useMetroLinesList } from '../hooks'
import { MetroLineFormDrawer } from '../components/MetroLineFormDrawer'
import { MetroLinesReorderDrawer } from '../components/MetroLinesReorderDrawer'
import {
  EMPTY_LINE_FILTERS,
  type MetroLineFilters,
  type MetroLineStatusFilter,
} from '../filter-schema'

export function MetroLinesPage() {
  const {
    draftFilters,
    setDraftFilters,
    appliedFilters,
    search,
    searchInput,
    setSearchInput,
    applySearch,
    clearSearch,
    applyFilters,
    resetFilters,
    removeFilter,
    clearAllFilters,
    page,
    setPage,
    size,
    setSize,
  } = useDraftAppliedFilters<MetroLineFilters>({ emptyFilters: EMPTY_LINE_FILTERS })

  const [drawerOpen, setDrawerOpen] = useState(false)
  const [reorderOpen, setReorderOpen] = useState(false)
  const [editingLine, setEditingLine] = useState<LineResponse | null>(null)
  const [deletingLine, setDeletingLine] = useState<LineResponse | null>(null)

  const { status: statusFilter } = appliedFilters

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

  const activeFilters = collectFilterTags([
    buildSearchFilterTag({ search, onRemove: clearSearch }),
    statusFilter !== 'ALL'
      ? buildLabeledFilterTag({
          id: 'status',
          label: 'Status',
          value: statusFilter,
          accent: 'status',
          onRemove: () => removeFilter('status', 'ALL'),
        })
      : null,
  ])

  const hasActiveFilters = activeFilters.length > 0
  const count = data?.totalElements ?? 0
  const summaryText = hasActiveFilters
    ? `Showing ${count} filtered metro line${count === 1 ? '' : 's'}`
    : `Showing ${count} metro line${count === 1 ? '' : 's'}`

  const columns: DataTableColumn<LineResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        ...COL_WIDTH.code,
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      { id: 'name', header: 'Name', ...COL_WIDTH.name, cell: (row) => row.name },
      {
        id: 'displayName',
        header: 'Display name',
        ...COL_WIDTH.title,
        cell: (row) => row.displayName ?? '—',
      },
      {
        id: 'color',
        header: 'Color',
        ...COL_WIDTH.color,
        truncate: false,
        cell: (row) =>
          row.colorHex ? (
            <span className="inline-flex items-center gap-2">
              <span
                className="h-4 w-4 shrink-0 rounded border border-border"
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
        ...COL_WIDTH.number,
        cell: (row) => row.totalStations ?? 0,
      },
      {
        id: 'status',
        header: 'Status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'updatedAt',
        header: 'Updated',
        ...COL_WIDTH.date,
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
        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" size="md" onClick={() => setReorderOpen(true)}>
            <ArrowUpDown className="h-4 w-4" />
            Reorder
          </Button>
          <Button size="md" onClick={openCreate}>
            <Plus className="h-4 w-4" />
            Create line
          </Button>
        </div>
      </div>

      <ListFilterToolbar
        searchId="line-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by code or name…"
        activeAdvancedFilterCount={countAppliedAdvancedFilters([statusFilter !== 'ALL'])}
        filterSubtitle="Narrow the list by line status."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="line-status-filter" label="Status" accent="status">
          <FilterSelect
            id="line-status-filter"
            value={draftFilters.status}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                status: e.target.value as MetroLineStatusFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </FilterSelect>
        </FilterGroup>
      </ListFilterToolbar>

      <ActiveFilterTags filters={activeFilters} onClearAll={clearAllFilters} />
      <FilterSummaryText text={summaryText} />

      <DataTable
        tableId="metro-lines"
        columns={columns}
        data={data?.content}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Metro lines"
        actionsWidth={112}
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
          onSizeChange={setSize}
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

      <MetroLinesReorderDrawer open={reorderOpen} onClose={() => setReorderOpen(false)} />

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
