import { useMemo, useState, type ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Check, Copy, KeyRound, MapPin, Pencil, QrCode, Trash2 } from 'lucide-react'
import { Breadcrumbs } from '../../../components/navigation/Breadcrumbs'
import { Button } from '../../../components/ui/Button'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/Card'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { LoadingState } from '../../../components/ui/LoadingState'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { formatDateTime } from '../../../lib/formatting/date'
import { formatNumber } from '../../../lib/formatting/number'
import { gpsReadinessStatus, scanKeyReadinessStatus } from '../../../lib/metro/readiness'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import { ROUTES } from '../../../lib/constants/routes'
import { useCopyToClipboard } from '../../../lib/utils/useCopyToClipboard'
import { useMetroLinesList } from '../../metro-lines/hooks'
import {
  useDeleteStation,
  useStationDetail,
  useStationStats,
} from '../hooks'
import { StationFormDrawer } from '../components/StationFormDrawer'
import { ScanKeyDrawer } from '../components/ScanKeyDrawer'
import { RotateQrDialog } from '../components/RotateQrDialog'
import { LineBadge } from '../components/StationTableCell'

function AssetPreview({
  label,
  url,
}: {
  label: string
  url?: string
}) {
  const { copied, copy } = useCopyToClipboard()

  if (!url) {
    return (
      <div className="flex h-32 items-center justify-center rounded-md border border-dashed border-border bg-secondary text-sm text-muted-foreground">
        No {label.toLowerCase()} set
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <div className="overflow-hidden rounded-md border border-border">
        <img src={url} alt={label} className="h-32 w-full object-cover" />
      </div>
      <div className="flex items-center gap-2">
        <p className="min-w-0 flex-1 truncate font-mono text-xs text-muted-foreground">{url}</p>
        <Button variant="ghost" size="sm" onClick={() => void copy(url)}>
          {copied ? <Check className="h-4 w-4 text-emerald-600" /> : <Copy className="h-4 w-4" />}
        </Button>
      </div>
    </div>
  )
}

function DetailRow({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-4 border-b border-border py-2.5 last:border-0">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span className="text-right text-sm font-medium text-foreground">{value}</span>
    </div>
  )
}

export function StationDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [editOpen, setEditOpen] = useState(false)
  const [scanKeyOpen, setScanKeyOpen] = useState(false)
  const [rotateOpen, setRotateOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const { data: station, isLoading, error } = useStationDetail(id)
  const { data: stats } = useStationStats()
  const { data: linesPage } = useMetroLinesList({ page: 0, size: 100 })
  const deleteMutation = useDeleteStation()

  const collectorCount = useMemo(() => {
    if (!id || !stats) return undefined
    return stats.find((s) => s.stationId === id)?.collectorCount
  }, [id, stats])

  if (isLoading) {
    return <LoadingState message="Loading station…" />
  }

  if (error) {
    if (isForbiddenError(error)) {
      return <PermissionDeniedState title="Station access denied" />
    }
    if (isNotFoundError(error)) {
      return (
        <ErrorState
          title="Station not found"
          message="This station may have been removed or you may not have access."
          action={
            <Button variant="secondary" onClick={() => navigate(ROUTES.stations)}>
              Back to stations
            </Button>
          }
        />
      )
    }
    return <ErrorState error={error} />
  }

  if (!station) {
    return null
  }

  const gpsStatus = gpsReadinessStatus(station)
  const scanStatus = scanKeyReadinessStatus(station)
  const readinessOk = gpsStatus === 'GPS_OK' && scanStatus === 'CONFIGURED'

  return (
    <div className="space-y-6">
      <Breadcrumbs
        items={[
          { label: 'Stations', to: ROUTES.stations },
          { label: station.name },
        ]}
      />

      {successMessage ? (
        <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
          {successMessage}
        </div>
      ) : null}

      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="text-2xl font-semibold text-foreground">{station.name}</h2>
            <LineBadge lineCode={station.lineCode} />
            <StatusBadge status={station.status} />
            <StatusBadge status={gpsStatus} />
            <StatusBadge status={readinessOk ? 'READY' : 'NOT_READY'} />
          </div>
          <p className="font-mono text-sm text-muted-foreground">
            {station.code}
            {station.address ? ` · ${station.address}` : ''}
          </p>
        </div>

        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" size="sm" onClick={() => setEditOpen(true)}>
            <Pencil className="h-4 w-4" />
            Edit station
          </Button>
          <Button variant="secondary" size="sm" onClick={() => setScanKeyOpen(true)}>
            <KeyRound className="h-4 w-4" />
            Manage scan keys
          </Button>
          <Button variant="danger" size="sm" onClick={() => setRotateOpen(true)}>
            <QrCode className="h-4 w-4" />
            Rotate QR
          </Button>
          <Button variant="outline" size="sm" onClick={() => setDeleteOpen(true)}>
            <Trash2 className="h-4 w-4 text-destructive" />
            Soft delete
          </Button>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Station Overview</CardTitle>
          </CardHeader>
          <CardContent>
            <DetailRow label="ID" value={<span className="font-mono text-xs">{station.id}</span>} />
            <DetailRow label="Code" value={station.code} />
            <DetailRow label="Name" value={station.name} />
            <DetailRow label="Display name" value={station.displayName ?? '—'} />
            <DetailRow label="Line" value={`${station.lineCode ?? ''} ${station.lineName ?? ''}`.trim() || '—'} />
            <DetailRow label="Address" value={station.address ?? '—'} />
            <DetailRow label="Description" value={station.description ?? '—'} />
            <DetailRow label="Sort order" value={station.sortOrder ?? '—'} />
            <DetailRow label="Status" value={<StatusBadge status={station.status} />} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>GPS / Geofence</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <DetailRow label="GPS status" value={<StatusBadge status={gpsStatus} />} />
            <DetailRow label="Latitude" value={station.latitude ?? '—'} />
            <DetailRow label="Longitude" value={station.longitude ?? '—'} />
            <DetailRow
              label="Zone radius"
              value={station.zoneRadiusMeters != null ? `${station.zoneRadiusMeters}m` : '—'}
            />
            <div className="flex h-28 items-center justify-center rounded-md border border-border bg-secondary text-center text-sm text-muted-foreground">
              <div>
                <MapPin className="mx-auto mb-1 h-5 w-5" />
                Map preview
                {station.latitude != null && station.longitude != null ? (
                  <p className="mt-1 font-mono text-xs">
                    {station.latitude}, {station.longitude}
                  </p>
                ) : null}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Public Assets</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Station image
              </p>
              <AssetPreview label="Station image" url={station.imageUrl} />
            </div>
            <div>
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Stamp preview
              </p>
              <AssetPreview label="Stamp preview" url={station.stampPreviewUrl} />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Audit Metadata</CardTitle>
          </CardHeader>
          <CardContent>
            <DetailRow label="Created" value={formatDateTime(station.createdAt)} />
            <DetailRow label="Updated" value={formatDateTime(station.updatedAt)} />
            <DetailRow label="Last QR rotated" value={formatDateTime(station.lastQrRotatedAt)} />
            <DetailRow label="Last scan key update" value={formatDateTime(station.lastScanKeyUpdatedAt)} />
          </CardContent>
        </Card>

        {collectorCount !== undefined ? (
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Operational Stats</CardTitle>
            </CardHeader>
            <CardContent>
              <DetailRow label="Collector count" value={formatNumber(collectorCount)} />
            </CardContent>
          </Card>
        ) : null}
      </div>

      <StationFormDrawer
        open={editOpen}
        station={station}
        lines={linesPage?.content ?? []}
        onClose={() => setEditOpen(false)}
      />

      <ScanKeyDrawer
        open={scanKeyOpen}
        stationId={station.id}
        onClose={() => setScanKeyOpen(false)}
      />

      <RotateQrDialog
        open={rotateOpen}
        station={station}
        onClose={() => setRotateOpen(false)}
        onSuccess={(msg) => setSuccessMessage(msg)}
      />

      <ConfirmDialog
        open={deleteOpen}
        variant="danger"
        title="Soft delete station?"
        description={
          <>
            This will soft-delete <strong>{station.name}</strong> ({station.code}). You will be
            redirected to the stations list.
          </>
        }
        confirmLabel="Soft delete station"
        loading={deleteMutation.isPending}
        onCancel={() => setDeleteOpen(false)}
        onConfirm={async () => {
          await deleteMutation.mutateAsync(station.id)
          navigate(ROUTES.stations)
        }}
      />
    </div>
  )
}
