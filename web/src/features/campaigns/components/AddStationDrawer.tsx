import { useMemo, useState } from 'react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { FormField, Input } from '../../../components/ui/FormField'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { EmptyState } from '../../../components/ui/EmptyState'
import { gpsReadinessStatus } from '../../../lib/metro/readiness'
import { isConflictError } from '../../../lib/api/errors'
import type { MetroStatus } from '../../../types/common'
import type { StationResponse } from '../../../types/stations'
import type { LineResponse } from '../../../types/metro-lines'
import { useStationsList } from '../../stations/hooks'
import { useAssignCampaignStation } from '../hooks'

/** Station picker loads the first page only; full inventory search is not available via API. */
const STATION_PICKER_PAGE_SIZE = 100

interface AddStationDrawerProps {
  open: boolean
  campaignId: string
  assignedStationIds: Set<string>
  lines: LineResponse[]
  onClose: () => void
  onSuccess?: () => void
}

type StatusFilter = MetroStatus | 'ALL'

export function AddStationDrawer({
  open,
  campaignId,
  assignedStationIds,
  lines,
  onClose,
  onSuccess,
}: AddStationDrawerProps) {
  const [search, setSearch] = useState('')
  const [lineFilter, setLineFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [selectedStationId, setSelectedStationId] = useState<string | null>(null)

  const { data: stationsPage, isLoading } = useStationsList({
    page: 0,
    size: STATION_PICKER_PAGE_SIZE,
  })

  const assignMutation = useAssignCampaignStation()

  const lineMap = useMemo(() => {
    const map = new Map<string, LineResponse>()
    lines.forEach((line) => map.set(line.id, line))
    return map
  }, [lines])

  const availableStations = useMemo(() => {
    const content = stationsPage?.content ?? []
    const needle = search.trim().toLowerCase()

    return content.filter((station) => {
      if (assignedStationIds.has(station.id)) {
        return false
      }
      if (lineFilter && station.lineId !== lineFilter) {
        return false
      }
      if (statusFilter !== 'ALL' && station.status !== statusFilter) {
        return false
      }
      if (!needle) {
        return true
      }
      return (
        station.code.toLowerCase().includes(needle) ||
        station.name.toLowerCase().includes(needle) ||
        (station.displayName?.toLowerCase().includes(needle) ?? false)
      )
    })
  }, [stationsPage?.content, assignedStationIds, search, lineFilter, statusFilter])

  function handleClose() {
    setSearch('')
    setLineFilter('')
    setStatusFilter('ALL')
    setSelectedStationId(null)
    onClose()
  }

  async function handleAssign() {
    if (!selectedStationId) {
      return
    }
    await assignMutation.mutateAsync({
      campaignId,
      body: { stationId: selectedStationId },
    })
    onSuccess?.()
    handleClose()
  }

  const conflictMessage = assignMutation.error && isConflictError(assignMutation.error)
    ? assignMutation.error.message
    : undefined

  return (
    <FormDrawer
      open={open}
      title="Add Station"
      description={`Select a station to assign. Showing up to ${STATION_PICKER_PAGE_SIZE} stations from the first page only.`}
      isSubmitting={assignMutation.isPending}
      saveLabel="Assign station"
      saveDisabled={!selectedStationId}
      error={assignMutation.error}
      onClose={handleClose}
      onSubmit={() => void handleAssign()}
      width="lg"
    >
      <div className="space-y-4">
        {conflictMessage ? (
          <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            {conflictMessage}
          </p>
        ) : null}

        <div className="grid gap-3 sm:grid-cols-3">
          <FormField label="Search" htmlFor="add-station-search">
            <Input
              id="add-station-search"
              placeholder="Code or name…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </FormField>
          <FormField label="Line" htmlFor="add-station-line">
            <select
              id="add-station-line"
              value={lineFilter}
              onChange={(e) => setLineFilter(e.target.value)}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            >
              <option value="">All lines</option>
              {lines.map((line) => (
                <option key={line.id} value={line.id}>
                  {line.code}
                </option>
              ))}
            </select>
          </FormField>
          <FormField label="Status" htmlFor="add-station-status">
            <select
              id="add-station-status"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            >
              <option value="ALL">All</option>
              <option value="DRAFT">Draft</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </FormField>
        </div>

        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading stations…</p>
        ) : availableStations.length === 0 ? (
          <EmptyState
            title="No stations available"
            description="All stations in the loaded page may already be assigned, or none match your filters."
          />
        ) : (
          <ul className="max-h-[420px] space-y-2 overflow-y-auto">
            {availableStations.map((station) => (
              <StationOption
                key={station.id}
                station={station}
                lineLabel={lineMap.get(station.lineId)?.code ?? station.lineCode ?? '—'}
                selected={selectedStationId === station.id}
                onSelect={() => setSelectedStationId(station.id)}
              />
            ))}
          </ul>
        )}
      </div>
    </FormDrawer>
  )
}

function StationOption({
  station,
  lineLabel,
  selected,
  onSelect,
}: {
  station: StationResponse
  lineLabel: string
  selected: boolean
  onSelect: () => void
}) {
  return (
    <li>
      <button
        type="button"
        onClick={onSelect}
        className={`w-full rounded-md border px-3 py-3 text-left transition-colors ${
          selected
            ? 'border-primary bg-secondary'
            : 'border-border bg-card hover:bg-secondary/60'
        }`}
      >
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-mono text-xs text-muted-foreground">{station.code}</span>
          <span className="font-medium text-foreground">{station.name}</span>
          <StatusBadge status={station.status} />
          <StatusBadge status={gpsReadinessStatus(station)} />
        </div>
        <p className="mt-1 text-xs text-muted-foreground">
          Line: {lineLabel}
          {station.displayName ? ` · ${station.displayName}` : ''}
        </p>
      </button>
    </li>
  )
}
