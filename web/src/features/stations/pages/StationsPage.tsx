import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowUpDown, Eye, KeyRound, Pencil, Plus, Trash2 } from 'lucide-react'
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
import { formatNumber } from '../../../lib/formatting/number'
import { gpsReadinessStatus } from '../../../lib/metro/readiness'
import { isForbiddenError } from '../../../lib/api/errors'
import { ROUTES } from '../../../lib/constants/routes'
import { detailFromListState } from '../../../lib/navigation/useSafeBackNavigation'
import type { StationResponse } from '../../../types/stations'
import { useMetroLinesList } from '../../metro-lines/hooks'
import { useDeleteStation, useStationStats, useStationsList } from '../hooks'
import { StationFormDrawer } from '../components/StationFormDrawer'
import { ScanKeyDrawer } from '../components/ScanKeyDrawer'
import { StationTableCell } from '../components/StationTableCell'
import { StationsReorderDrawer } from '../components/StationsReorderDrawer'
import {
  EMPTY_STATION_FILTERS,
  type StationFilters,
  type StationStatusFilter,
} from '../filter-schema'

export function StationsPage() {
  const navigate = useNavigate()
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
  } = useDraftAppliedFilters<StationFilters>({ emptyFilters: EMPTY_STATION_FILTERS })

  const [drawerOpen, setDrawerOpen] = useState(false)
  const [reorderOpen, setReorderOpen] = useState(false)
  const [editingStation, setEditingStation] = useState<StationResponse | null>(null)
  const [deletingStation, setDeletingStation] = useState<StationResponse | null>(null)
  const [scanKeyStationId, setScanKeyStationId] = useState<string | null>(null)

  const { lineId: lineFilter, status: statusFilter } = appliedFilters

  const listParams = useMemo(
    () => ({
      page,
      size,
      search: search || undefined,
      lineId: lineFilter || undefined,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
    }),
    [page, size, search, lineFilter, statusFilter],
  )

  const { data, isLoading, error, refetch } = useStationsList(listParams)
  const { data: stats } = useStationStats()
  const { data: linesPage } = useMetroLinesList({ page: 0, size: 100 })
  const deleteMutation = useDeleteStation()

  const collectorMap = useMemo(() => {
    const map = new Map<string, number>()
    stats?.forEach((s) => map.set(s.stationId, s.collectorCount))
    return map
  }, [stats])

  const lines = useMemo(() => linesPage?.content ?? [], [linesPage?.content])
  const selectedLine = useMemo(
    () => lines.find((line) => line.id === lineFilter),
    [lines, lineFilter],
  )

  const activeAdvancedFilterCount = countAppliedAdvancedFilters([
    Boolean(lineFilter),
    statusFilter !== 'ALL',
  ])

  const activeFilters = collectFilterTags([
    buildSearchFilterTag({ search, onRemove: clearSearch }),
    lineFilter
      ? buildLabeledFilterTag({
          id: 'line',
          label: 'Line',
          value: (() => {
            const line = lines.find((l) => l.id === lineFilter)
            return line ? `${line.code} — ${line.name}` : lineFilter
          })(),
          accent: 'line',
          onRemove: () => removeFilter('lineId', ''),
        })
      : null,
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
    ? `Showing ${count} filtered station${count === 1 ? '' : 's'}`
    : `Showing ${count} station${count === 1 ? '' : 's'}`

  const columns: DataTableColumn<StationResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        ...COL_WIDTH.code,
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      {
        id: 'station',
        header: 'Station',
        defaultWidth: 220,
        minWidth: 160,
        truncate: false,
        cell: (row) => <StationTableCell station={row} />,
      },
      {
        id: 'line',
        header: 'Line',
        ...COL_WIDTH.entity,
        defaultWidth: 140,
        cell: (row) => row.lineCode ?? row.lineName ?? '—',
      },
      {
        id: 'address',
        header: 'Address',
        ...COL_WIDTH.address,
        cell: (row) => <span className="text-sm">{row.address ?? '—'}</span>,
      },
      {
        id: 'status',
        header: 'Status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'gps',
        header: 'GPS',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => <StatusBadge status={gpsReadinessStatus(row)} />,
      },
      {
        id: 'collectors',
        header: 'Collectors',
        align: 'right',
        ...COL_WIDTH.number,
        cell: (row) => formatNumber(collectorMap.get(row.id) ?? 0),
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
    [collectorMap],
  )

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Stations access denied" />
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">Stations</h2>
          <p className="text-sm text-muted-foreground">
            Manage station profiles, GPS and geofence settings, scan keys, and discovery media.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="secondary"
            size="md"
            disabled={!lineFilter}
            title={lineFilter ? undefined : 'Select a line filter to reorder stations'}
            onClick={() => setReorderOpen(true)}
          >
            <ArrowUpDown className="h-4 w-4" />
            Reorder
          </Button>
          <Button size="md" onClick={() => { setEditingStation(null); setDrawerOpen(true) }}>
            <Plus className="h-4 w-4" />
            Create station
          </Button>
        </div>
      </div>

      <ListFilterToolbar
        searchId="station-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by code or name…"
        activeAdvancedFilterCount={activeAdvancedFilterCount}
        filterSubtitle="Narrow the list by metro line and status."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="station-line-filter" label="Line" accent="line">
          <FilterSelect
            id="station-line-filter"
            value={draftFilters.lineId}
            onChange={(e) =>
              setDraftFilters((prev) => ({ ...prev, lineId: e.target.value }))
            }
          >
            <option value="">All lines</option>
            {lines.map((line) => (
              <option key={line.id} value={line.id}>
                {line.code} — {line.name}
              </option>
            ))}
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="station-status-filter" label="Status" accent="status">
          <FilterSelect
            id="station-status-filter"
            value={draftFilters.status}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                status: e.target.value as StationStatusFilter,
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
        tableId="stations"
        columns={columns}
        data={data?.content}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Metro stations"
        actionsWidth={168}
        rowWarning={(row) => gpsReadinessStatus(row) === 'GPS_MISSING'}
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() =>
                navigate(ROUTES.stationDetail(row.id), {
                  state: detailFromListState(ROUTES.stations),
                })
              }
              aria-label="View station"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setScanKeyStationId(row.id)}
              aria-label="Manage scan keys"
            >
              <KeyRound className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => { setEditingStation(row); setDrawerOpen(true) }}
              aria-label="Edit station"
            >
              <Pencil className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDeletingStation(row)}
              aria-label="Soft delete station"
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

      <StationFormDrawer
        open={drawerOpen}
        station={editingStation}
        lines={lines}
        onClose={() => {
          setDrawerOpen(false)
          setEditingStation(null)
        }}
      />

      {lineFilter ? (
        <StationsReorderDrawer
          open={reorderOpen}
          lineId={lineFilter}
          lineLabel={selectedLine ? `${selectedLine.code} · ${selectedLine.name}` : undefined}
          onClose={() => setReorderOpen(false)}
        />
      ) : null}

      <ScanKeyDrawer
        open={Boolean(scanKeyStationId)}
        stationId={scanKeyStationId}
        onClose={() => setScanKeyStationId(null)}
      />

      <ConfirmDialog
        open={Boolean(deletingStation)}
        variant="danger"
        title="Soft delete station?"
        description={
          deletingStation ? (
            <>
              This will soft-delete <strong>{deletingStation.name}</strong> ({deletingStation.code}
              ). The station is deactivated, not permanently removed.
            </>
          ) : null
        }
        confirmLabel="Soft delete station"
        loading={deleteMutation.isPending}
        onCancel={() => setDeletingStation(null)}
        onConfirm={async () => {
          if (!deletingStation) return
          await deleteMutation.mutateAsync(deletingStation.id)
          setDeletingStation(null)
        }}
      />
    </div>
  )
}
