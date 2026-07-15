import { useMemo, useState } from 'react'
import { Trash2 } from 'lucide-react'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
import { Button } from '../../../components/ui/Button'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import type { CampaignStationResponse } from '../../../types/campaigns'
import type { LineResponse } from '../../../types/metro-lines'
import { useRemoveCampaignStation } from '../hooks'

interface AssignedStationsTableProps {
  campaignId: string
  stations: CampaignStationResponse[] | undefined
  lines: LineResponse[]
  isLoading?: boolean
  error?: unknown
  onRetry?: () => void
}

export function AssignedStationsTable({
  campaignId,
  stations,
  lines,
  isLoading,
  error,
  onRetry,
}: AssignedStationsTableProps) {
  const [removingStation, setRemovingStation] = useState<CampaignStationResponse | null>(null)
  const removeMutation = useRemoveCampaignStation()

  const lineMap = useMemo(() => {
    const map = new Map<string, string>()
    lines.forEach((line) => map.set(line.id, line.code))
    return map
  }, [lines])

  const columns: DataTableColumn<CampaignStationResponse>[] = useMemo(
    () => [
      {
        id: 'station',
        header: 'Station',
        ...COL_WIDTH.entity,
        defaultWidth: 200,
        truncate: false,
        cell: (row) => (
          <div>
            <p className="font-medium text-foreground">{row.name}</p>
          </div>
        ),
      },
      {
        id: 'displayName',
        header: 'Display name',
        ...COL_WIDTH.title,
        cell: (row) => row.displayName ?? '—',
      },
      {
        id: 'line',
        header: 'Line',
        ...COL_WIDTH.code,
        cell: (row) => (row.lineId ? (lineMap.get(row.lineId) ?? '—') : '—'),
      },
      {
        id: 'lineSequence',
        header: 'Line sequence',
        align: 'right',
        ...COL_WIDTH.number,
        defaultWidth: 120,
        cell: (row) => row.sortOrder ?? '—',
      },
    ],
    [lineMap],
  )

  return (
    <>
      <DataTable
        tableId="campaign-assigned-stations"
        columns={columns}
        data={stations}
        getRowId={(row) => row.stationId}
        isLoading={isLoading}
        error={error}
        onRetry={onRetry}
        caption="Assigned stations"
        actionsWidth={72}
        emptyTitle="No stations assigned"
        emptyDescription="Add stations to make them available in this campaign for mobile collectors."
        rowActions={(row) => (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setRemovingStation(row)}
            aria-label="Remove station from campaign"
          >
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        )}
      />

      <ConfirmDialog
        open={Boolean(removingStation)}
        variant="danger"
        title="Remove station from campaign?"
        description={
          removingStation ? (
            <>
              Remove <strong>{removingStation.name}</strong> from this campaign? Mobile users may no
              longer see or collect this station in this campaign.
            </>
          ) : null
        }
        confirmLabel="Remove station"
        loading={removeMutation.isPending}
        onCancel={() => setRemovingStation(null)}
        onConfirm={async () => {
          if (!removingStation) {
            return
          }
          await removeMutation.mutateAsync({
            campaignId,
            stationId: removingStation.stationId,
          })
          setRemovingStation(null)
        }}
      />
    </>
  )
}
