import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, KeyRound, Pencil, Plus, Trash2 } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { Input } from '../../../components/ui/FormField'
import { formatDateTime } from '../../../lib/formatting/date'
import { formatNumber } from '../../../lib/formatting/number'
import { gpsReadinessStatus } from '../../../lib/metro/readiness'
import { isForbiddenError } from '../../../lib/api/errors'
import { ROUTES } from '../../../lib/constants/routes'
import type { MetroStatus } from '../../../types/common'
import type { StationResponse } from '../../../types/stations'
import { useMetroLinesList } from '../../metro-lines/hooks'
import { useDeleteStation, useStationStats, useStationsList } from '../hooks'
import { StationFormDrawer } from '../components/StationFormDrawer'
import { ScanKeyDrawer } from '../components/ScanKeyDrawer'
import { StationTableCell } from '../components/StationTableCell'

type StatusFilter = MetroStatus | 'ALL'

export function StationsPage() {
  const navigate = useNavigate()
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [lineFilter, setLineFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingStation, setEditingStation] = useState<StationResponse | null>(null)
  const [deletingStation, setDeletingStation] = useState<StationResponse | null>(null)
  const [scanKeyStationId, setScanKeyStationId] = useState<string | null>(null)

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

  const lines = linesPage?.content ?? []

  const columns: DataTableColumn<StationResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      {
        id: 'station',
        header: 'Station',
        cell: (row) => <StationTableCell station={row} />,
      },
      {
        id: 'line',
        header: 'Line',
        cell: (row) => row.lineCode ?? row.lineName ?? '—',
      },
      {
        id: 'address',
        header: 'Address',
        cell: (row) => (
          <span className="max-w-[200px] truncate text-sm">{row.address ?? '—'}</span>
        ),
      },
      {
        id: 'status',
        header: 'Status',
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'gps',
        header: 'GPS',
        cell: (row) => <StatusBadge status={gpsReadinessStatus(row)} />,
      },
      {
        id: 'collectors',
        header: 'Collectors',
        align: 'right',
        cell: (row) => formatNumber(collectorMap.get(row.id) ?? 0),
      },
      {
        id: 'updatedAt',
        header: 'Updated',
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
            Manage station records, GPS configuration, and scan key readiness.
          </p>
        </div>
        <Button size="md" onClick={() => { setEditingStation(null); setDrawerOpen(true) }}>
          <Plus className="h-4 w-4" />
          Create station
        </Button>
      </div>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:items-end">
        <div className="flex-1 space-y-1">
          <label htmlFor="station-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="station-search"
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
          <label htmlFor="station-line" className="text-xs font-medium text-muted-foreground">
            Line
          </label>
          <select
            id="station-line"
            value={lineFilter}
            onChange={(e) => {
              setLineFilter(e.target.value)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-48"
          >
            <option value="">All lines</option>
            {lines.map((line) => (
              <option key={line.id} value={line.id}>
                {line.code}
              </option>
            ))}
          </select>
        </div>
        <div className="space-y-1">
          <label htmlFor="station-status" className="text-xs font-medium text-muted-foreground">
            Status
          </label>
          <select
            id="station-status"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as StatusFilter)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-40"
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
        caption="Metro stations"
        rowWarning={(row) => gpsReadinessStatus(row) === 'GPS_MISSING'}
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate(ROUTES.stationDetail(row.id))}
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
          onSizeChange={(next) => {
            setSize(next)
            setPage(0)
          }}
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
