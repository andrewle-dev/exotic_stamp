import type { ReactNode } from 'react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { Button } from '../../../components/ui/Button'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { SkeletonText } from '../../../components/ui/LoadingSkeleton'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDateTime } from '../../../lib/formatting/date'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import type { CampaignResponse } from '../../../types/campaigns'
import type { StationResponse } from '../../../types/stations'
import type { StampDesignResponse } from '../../../types/stamp-designs'
import { resolveCampaignLabel, resolveStationLabel } from '../utils/resolve-labels'
import { useStampDesign } from '../hooks'

interface StampDesignDetailDrawerProps {
  open: boolean
  stampDesignId: string | null
  campaigns: CampaignResponse[]
  stations: StationResponse[]
  /** Fallback row data when detail fetch is not needed or pending. */
  fallback?: StampDesignResponse | null
  onClose: () => void
  onEdit?: (design: StampDesignResponse) => void
}

function DetailRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="space-y-1">
      <dt className="text-xs font-medium text-muted-foreground">{label}</dt>
      <dd className="text-sm text-foreground">{children}</dd>
    </div>
  )
}

export function StampDesignDetailDrawer({
  open,
  stampDesignId,
  campaigns,
  stations,
  fallback,
  onClose,
  onEdit,
}: StampDesignDetailDrawerProps) {
  const { data, isLoading, error } = useStampDesign(open ? stampDesignId ?? undefined : undefined)
  const design = data ?? fallback

  const footer =
    design && onEdit ? (
      <>
        <Button variant="secondary" size="md" onClick={onClose}>
          Close
        </Button>
        <Button size="md" onClick={() => onEdit(design)}>
          Edit stamp design
        </Button>
      </>
    ) : undefined

  return (
    <FormDrawer
      open={open}
      title="Stamp Design Details"
      description={
        design
          ? `${design.name} — campaign-specific collectible artwork for this station.`
          : 'Campaign-specific collectible stamp artwork for a station.'
      }
      onClose={onClose}
      footer={footer}
      width="lg"
    >
      {isLoading && !design ? <SkeletonText lines={6} /> : null}

      {!isLoading && error && isForbiddenError(error) ? (
        <PermissionDeniedState title="Stamp design access denied" />
      ) : null}

      {!isLoading && error && isNotFoundError(error) ? (
        <ErrorState title="Stamp design not found" message="It may have been deleted." />
      ) : null}

      {!isLoading && error && !isForbiddenError(error) && !isNotFoundError(error) ? (
        <ErrorState title="Could not load stamp design" error={error} />
      ) : null}

      {design ? (
        <div className="space-y-6">
          <div className="flex flex-col gap-4 sm:flex-row">
            <ImageWithFallback
              src={design.previewImageUrl || design.imageUrl}
              alt={design.name}
              className="h-40 w-40 shrink-0"
              fallbackClassName="h-40 w-40 shrink-0"
            />
            <div className="flex flex-wrap gap-2">
              {design.rarity ? <StatusBadge status={design.rarity} /> : null}
              {design.status ? <StatusBadge status={design.status} /> : null}
            </div>
          </div>

          <dl className="grid gap-4 sm:grid-cols-2">
            <DetailRow label="Name">{design.name}</DetailRow>

            <DetailRow label="Campaign">
              {(() => {
                const { label, unknown } = resolveCampaignLabel(design.campaignId, campaigns)
                return (
                  <span className={unknown ? 'text-amber-700' : undefined}>{label}</span>
                )
              })()}
            </DetailRow>

            <DetailRow label="Station">
              {(() => {
                const { label, unknown } = resolveStationLabel(design.stationId, stations)
                return (
                  <span className={unknown ? 'text-amber-700' : undefined}>{label}</span>
                )
              })()}
            </DetailRow>

            {design.description ? (
              <div className="sm:col-span-2">
                <DetailRow label="Description">{design.description}</DetailRow>
              </div>
            ) : null}

            <DetailRow label="Main stamp artwork">
              {design.imageUrl ? 'Uploaded' : '—'}
            </DetailRow>
            <DetailRow label="Thumbnail / preview">
              {design.previewImageUrl ? 'Uploaded' : 'Falls back to main artwork'}
            </DetailRow>

            <DetailRow label="Created at">
              {design.createdAt ? formatDateTime(design.createdAt) : '—'}
            </DetailRow>
            <DetailRow label="Updated at">
              {design.updatedAt ? formatDateTime(design.updatedAt) : '—'}
            </DetailRow>
          </dl>
        </div>
      ) : null}

      {!isLoading && !error && !design ? (
        <ErrorState title="No stamp design selected" />
      ) : null}
    </FormDrawer>
  )
}
