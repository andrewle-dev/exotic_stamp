import type { ReactNode } from 'react'
import { Check, Copy } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { Button } from '../../../components/ui/Button'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { SkeletonText } from '../../../components/ui/LoadingSkeleton'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDateTime } from '../../../lib/formatting/date'
import { useCopyToClipboard } from '../../../lib/utils/useCopyToClipboard'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import type { CampaignResponse } from '../../../types/campaigns'
import type { MilestoneResponse } from '../../../types/milestones'
import { resolveCampaignLabel } from '../../stamp-designs/utils/resolve-labels'
import { useMilestone } from '../hooks'

interface MilestoneDetailDrawerProps {
  open: boolean
  milestoneId: string | null
  campaigns: CampaignResponse[]
  fallback?: MilestoneResponse | null
  onClose: () => void
  onEdit?: (milestone: MilestoneResponse) => void
}

function DetailRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="space-y-1">
      <dt className="text-xs font-medium text-muted-foreground">{label}</dt>
      <dd className="text-sm text-foreground">{children}</dd>
    </div>
  )
}

function UrlCopyRow({ label, url }: { label: string; url?: string }) {
  const { copied, copy } = useCopyToClipboard()

  if (!url) {
    return (
      <DetailRow label={label}>
        <span className="text-muted-foreground">—</span>
      </DetailRow>
    )
  }

  return (
    <DetailRow label={label}>
      <div className="flex items-start gap-2">
        <code className="flex-1 break-all rounded bg-secondary px-2 py-1 text-xs">{url}</code>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={() => void copy(url)}
          aria-label={`Copy ${label}`}
        >
          {copied ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
        </Button>
      </div>
    </DetailRow>
  )
}

export function MilestoneDetailDrawer({
  open,
  milestoneId,
  campaigns,
  fallback,
  onClose,
  onEdit,
}: MilestoneDetailDrawerProps) {
  const { data, isLoading, error } = useMilestone(open ? milestoneId ?? undefined : undefined)
  const milestone = data ?? fallback

  const footer =
    milestone && onEdit ? (
      <>
        <Button variant="secondary" size="md" onClick={onClose}>
          Close
        </Button>
        <Button size="md" onClick={() => onEdit(milestone)}>
          Edit milestone
        </Button>
      </>
    ) : undefined

  return (
    <FormDrawer
      open={open}
      title="Milestone Details"
      description={milestone?.name}
      onClose={onClose}
      footer={footer}
      width="lg"
    >
      {isLoading && !milestone ? <SkeletonText lines={6} /> : null}

      {!isLoading && error && isForbiddenError(error) ? (
        <PermissionDeniedState title="Milestone access denied" />
      ) : null}

      {!isLoading && error && isNotFoundError(error) ? (
        <ErrorState title="Milestone not found" message="It may have been deleted." />
      ) : null}

      {!isLoading && error && !isForbiddenError(error) && !isNotFoundError(error) ? (
        <ErrorState title="Could not load milestone" error={error} />
      ) : null}

      {milestone ? (
        <div className="space-y-6">
          <div className="flex flex-wrap gap-2">
            {milestone.rewardType ? <StatusBadge status={milestone.rewardType} /> : null}
            {milestone.status ? <StatusBadge status={milestone.status} /> : null}
          </div>

          {milestone.rewardImageUrl ? (
            <ImageWithFallback
              src={milestone.rewardImageUrl}
              alt={milestone.rewardTitle}
              className="h-32 w-full max-w-xs"
              fallbackClassName="h-32 w-full max-w-xs"
            />
          ) : null}

          <dl className="grid gap-4 sm:grid-cols-2">
            <DetailRow label="Code">
              <span className="font-mono text-xs">{milestone.code}</span>
            </DetailRow>
            <DetailRow label="Required stamps">{milestone.requiredStampCount}</DetailRow>
            <DetailRow label="Name">{milestone.name}</DetailRow>
            <DetailRow label="Sort order">{milestone.sortOrder ?? '—'}</DetailRow>

            <DetailRow label="Campaign">
              {(() => {
                const { label, unknown } = resolveCampaignLabel(milestone.campaignId, campaigns)
                return (
                  <span className={unknown ? 'text-amber-700' : undefined}>
                    {label}
                    <span className="mt-0.5 block font-mono text-xs text-muted-foreground">
                      {milestone.campaignId}
                    </span>
                  </span>
                )
              })()}
            </DetailRow>

            {milestone.description ? (
              <div className="sm:col-span-2">
                <DetailRow label="Description">{milestone.description}</DetailRow>
              </div>
            ) : null}

            <DetailRow label="Reward type">
              <StatusBadge status={milestone.rewardType} />
            </DetailRow>
            <DetailRow label="Reward title">{milestone.rewardTitle}</DetailRow>

            {milestone.rewardDescription ? (
              <div className="sm:col-span-2">
                <DetailRow label="Reward description">{milestone.rewardDescription}</DetailRow>
              </div>
            ) : null}

            <div className="sm:col-span-2">
              <UrlCopyRow label="Reward image URL" url={milestone.rewardImageUrl} />
            </div>

            <DetailRow label="Status">
              {milestone.status ? (
                <StatusBadge status={milestone.status} />
              ) : (
                <span className="text-muted-foreground">—</span>
              )}
            </DetailRow>

            <DetailRow label="Deleted at">
              {milestone.deletedAt ? formatDateTime(milestone.deletedAt) : '—'}
            </DetailRow>

            <DetailRow label="ID">
              <span className="font-mono text-xs">{milestone.id}</span>
            </DetailRow>
          </dl>
        </div>
      ) : null}

      {!isLoading && !error && !milestone ? (
        <ErrorState title="No milestone selected" />
      ) : null}
    </FormDrawer>
  )
}
