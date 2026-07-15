import { useMemo, useState, type ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { AlertTriangle, Check, Copy, MapPinPlus, Pencil, Trash2 } from 'lucide-react'
import { DetailPageHeader } from '../../../components/navigation/DetailPageHeader'
import { Button } from '../../../components/ui/Button'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/Card'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { LoadingState } from '../../../components/ui/LoadingState'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { EmptyState } from '../../../components/ui/EmptyState'
import { formatDateTime } from '../../../lib/formatting/date'
import {
  formatDateRange,
  getCampaignScheduleState,
} from '../../../lib/campaigns/schedule'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import { ROUTES } from '../../../lib/constants/routes'
import { useCopyToClipboard } from '../../../lib/utils/useCopyToClipboard'
import { useMetroLinesList } from '../../metro-lines/hooks'
import {
  useCampaign,
  useCampaignStations,
  useDeleteCampaign,
} from '../hooks'
import { CampaignFormDrawer } from '../components/CampaignFormDrawer'
import { AssignedStationsTable } from '../components/AssignedStationsTable'
import { AddStationDrawer } from '../components/AddStationDrawer'

function AssetPreview({ label, url }: { label: string; url?: string }) {
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

function WarningItem({ children }: { children: ReactNode }) {
  return (
    <li className="flex items-start gap-2 text-sm text-amber-800">
      <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
      <span>{children}</span>
    </li>
  )
}

export function CampaignDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [editOpen, setEditOpen] = useState(false)
  const [addStationOpen, setAddStationOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)

  const { data: campaign, isLoading, error } = useCampaign(id)
  const {
    data: assignedStations,
    isLoading: stationsLoading,
    error: stationsError,
    refetch: refetchStations,
  } = useCampaignStations(id)
  const { data: linesPage } = useMetroLinesList({ page: 0, size: 100 })
  const deleteMutation = useDeleteCampaign()

  const lines = linesPage?.content ?? []

  const assignedStationIds = useMemo(
    () => new Set((assignedStations ?? []).map((station) => station.stationId)),
    [assignedStations],
  )

  const scheduleState = campaign
    ? getCampaignScheduleState(campaign.startAt, campaign.endAt)
    : null

  const warnings = useMemo(() => {
    if (!campaign) {
      return []
    }
    const items: string[] = []
    if (campaign.status === 'ACTIVE' && (assignedStations?.length ?? 0) === 0) {
      items.push('This campaign is active but has no assigned stations.')
    }
    if (campaign.status === 'ACTIVE' && scheduleState === 'EXPIRED') {
      items.push('This campaign is active but its schedule has expired.')
    }
    if (!campaign.bannerImageUrl) {
      items.push('Banner image is missing.')
    }
    if (!campaign.thumbnailImageUrl) {
      items.push('Thumbnail image is missing.')
    }
    return items
  }, [campaign, assignedStations, scheduleState])

  if (isLoading) {
    return <LoadingState message="Loading campaign…" />
  }

  if (error) {
    if (isForbiddenError(error)) {
      return <PermissionDeniedState title="Campaign access denied" />
    }
    if (isNotFoundError(error)) {
      return (
        <ErrorState
          title="Campaign not found"
          message="This campaign may have been removed or you may not have access."
          action={
            <Button variant="secondary" onClick={() => navigate(ROUTES.campaigns)}>
              Back to Campaigns
            </Button>
          }
        />
      )
    }
    return <ErrorState error={error} />
  }

  if (!campaign) {
    return null
  }

  return (
    <div className="space-y-6">
      <DetailPageHeader
        backLabel="Back to Campaigns"
        backTo={ROUTES.campaigns}
        title={campaign.name}
        subtitle={`${campaign.code} · ${formatDateRange(campaign.startAt, campaign.endAt)}`}
        badges={
          <>
            <StatusBadge status={campaign.campaignType ?? 'STANDARD'} />
            <StatusBadge status={campaign.status} />
            {scheduleState ? <StatusBadge status={scheduleState} /> : null}
          </>
        }
        actions={
          <>
            <Button variant="secondary" size="sm" onClick={() => setEditOpen(true)}>
              <Pencil className="h-4 w-4" />
              Edit campaign
            </Button>
            <Button variant="secondary" size="sm" onClick={() => setAddStationOpen(true)}>
              <MapPinPlus className="h-4 w-4" />
              Add station
            </Button>
            <Button variant="outline" size="sm" onClick={() => setDeleteOpen(true)}>
              <Trash2 className="h-4 w-4 text-destructive" />
              Archive campaign
            </Button>
          </>
        }
      />

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Campaign Overview</CardTitle>
          </CardHeader>
          <CardContent>
            <DetailRow label="Code" value={campaign.code} />
            <DetailRow label="Name" value={campaign.name} />
            <DetailRow label="Display name" value={campaign.displayName ?? '—'} />
            <DetailRow label="Description" value={campaign.description ?? '—'} />
            <DetailRow
              label="Campaign type"
              value={<StatusBadge status={campaign.campaignType ?? 'STANDARD'} />}
            />
            <DetailRow label="Status" value={<StatusBadge status={campaign.status} />} />
            <DetailRow label="Priority" value={campaign.priority ?? '—'} />
            <DetailRow label="Created" value={formatDateTime(campaign.createdAt)} />
            <DetailRow label="Updated" value={formatDateTime(campaign.updatedAt)} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Schedule</CardTitle>
          </CardHeader>
          <CardContent>
            <DetailRow label="Start at" value={formatDateTime(campaign.startAt)} />
            <DetailRow label="End at" value={formatDateTime(campaign.endAt)} />
            <DetailRow
              label="Schedule state"
              value={scheduleState ? <StatusBadge status={scheduleState} /> : '—'}
            />
            {campaign.status === 'ACTIVE' && scheduleState === 'EXPIRED' ? (
              <p className="mt-3 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
                This campaign is marked active but its end date has passed.
              </p>
            ) : null}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Public Assets</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Banner image
              </p>
              <AssetPreview label="Banner image" url={campaign.bannerImageUrl} />
            </div>
            <div>
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Thumbnail image
              </p>
              <AssetPreview label="Thumbnail image" url={campaign.thumbnailImageUrl} />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Operational Warnings</CardTitle>
          </CardHeader>
          <CardContent>
            {warnings.length === 0 ? (
              <EmptyState
                title="No warnings"
                description="This campaign has no operational issues detected."
              />
            ) : (
              <ul className="space-y-2">
                {warnings.map((warning) => (
                  <WarningItem key={warning}>{warning}</WarningItem>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Assigned Stations</CardTitle>
          </CardHeader>
          <CardContent>
            <AssignedStationsTable
              campaignId={campaign.id}
              stations={assignedStations}
              lines={lines}
              isLoading={stationsLoading}
              error={stationsError}
              onRetry={() => void refetchStations()}
            />
          </CardContent>
        </Card>
      </div>

      <CampaignFormDrawer
        open={editOpen}
        campaign={campaign}
        onClose={() => setEditOpen(false)}
      />

      <AddStationDrawer
        open={addStationOpen}
        campaignId={campaign.id}
        assignedStationIds={assignedStationIds}
        lines={lines}
        onClose={() => setAddStationOpen(false)}
      />

      <ConfirmDialog
        open={deleteOpen}
        variant="danger"
        title="Archive campaign?"
        description={
          <>
            This will soft-delete <strong>{campaign.name}</strong> ({campaign.code}). You will be
            redirected to the campaigns list.
          </>
        }
        confirmLabel="Archive campaign"
        loading={deleteMutation.isPending}
        onCancel={() => setDeleteOpen(false)}
        onConfirm={async () => {
          await deleteMutation.mutateAsync(campaign.id)
          navigate(ROUTES.campaigns)
        }}
      />
    </div>
  )
}
