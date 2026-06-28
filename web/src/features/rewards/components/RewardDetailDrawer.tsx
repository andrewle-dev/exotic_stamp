import type { ReactNode } from 'react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { Button } from '../../../components/ui/Button'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { SkeletonText } from '../../../components/ui/LoadingSkeleton'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatNumber } from '../../../lib/formatting/number'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import type { MilestoneResponse } from '../../../types/milestones'
import type { PartnerResponse } from '../../../types/partners'
import type { RewardResponse } from '../../../types/rewards'
import {
  formatExpiryDays,
  formatRewardValue,
  formatStock,
  resolveMilestoneLabel,
  resolvePartnerLabel,
} from '../utils/resolve-labels'
import { useReward, useRewardVoucherStats } from '../hooks'

interface RewardDetailDrawerProps {
  open: boolean
  rewardId: string | null
  milestones: MilestoneResponse[]
  partners: PartnerResponse[]
  fallback?: RewardResponse | null
  onClose: () => void
  onEdit?: (reward: RewardResponse) => void
  onBulkUpload?: (reward: RewardResponse) => void
}

function DetailRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="space-y-1">
      <dt className="text-xs font-medium text-muted-foreground">{label}</dt>
      <dd className="text-sm text-foreground">{children}</dd>
    </div>
  )
}

export function RewardDetailDrawer({
  open,
  rewardId,
  milestones,
  partners,
  fallback,
  onClose,
  onEdit,
  onBulkUpload,
}: RewardDetailDrawerProps) {
  const { data, isLoading, error } = useReward(open ? rewardId ?? undefined : undefined)
  const reward = data ?? fallback
  const isVoucher = reward?.rewardType === 'VOUCHER'

  const { data: voucherStats, isLoading: statsLoading } = useRewardVoucherStats(
    open && isVoucher ? reward?.id : undefined,
  )

  const milestoneLabel = reward
    ? resolveMilestoneLabel(reward.milestoneId, milestones)
    : null
  const partnerLabel = reward ? resolvePartnerLabel(reward.partnerId, partners) : null

  const footer =
    reward && (onEdit || (isVoucher && onBulkUpload)) ? (
      <>
        <Button variant="secondary" size="md" onClick={onClose}>
          Close
        </Button>
        {isVoucher && onBulkUpload ? (
          <Button variant="secondary" size="md" onClick={() => onBulkUpload(reward)}>
            Upload vouchers
          </Button>
        ) : null}
        {onEdit ? (
          <Button size="md" onClick={() => onEdit(reward)}>
            Edit reward
          </Button>
        ) : null}
      </>
    ) : undefined

  return (
    <FormDrawer
      open={open}
      title="Reward Details"
      description={reward?.name}
      onClose={onClose}
      footer={footer}
      width="lg"
    >
      {isLoading && !reward ? <SkeletonText lines={8} /> : null}

      {!isLoading && error && isForbiddenError(error) ? (
        <PermissionDeniedState title="Reward access denied" />
      ) : null}

      {!isLoading && error && isNotFoundError(error) ? (
        <ErrorState title="Reward not found" message="It may have been removed." />
      ) : null}

      {!isLoading && error && !isForbiddenError(error) && !isNotFoundError(error) ? (
        <ErrorState title="Could not load reward" error={error} />
      ) : null}

      {reward ? (
        <div className="space-y-6">
          <div className="flex flex-wrap gap-2">
            <StatusBadge status={reward.rewardType} />
            <StatusBadge status={reward.active ? 'ACTIVE' : 'INACTIVE'} />
          </div>

          <dl className="grid gap-4 sm:grid-cols-2">
            <DetailRow label="ID">
              <span className="font-mono text-xs">{reward.id}</span>
            </DetailRow>
            <DetailRow label="Name">{reward.name}</DetailRow>
            <DetailRow label="Milestone">
              <span className={milestoneLabel?.unknown ? 'text-amber-700' : undefined}>
                {milestoneLabel?.label}
              </span>
              <span className="mt-0.5 block font-mono text-xs text-muted-foreground">
                {reward.milestoneId}
              </span>
            </DetailRow>
            <DetailRow label="Partner">
              <span className={partnerLabel?.unknown ? 'text-amber-700' : undefined}>
                {partnerLabel?.label}
              </span>
              {reward.partnerId ? (
                <span className="mt-0.5 block font-mono text-xs text-muted-foreground">
                  {reward.partnerId}
                </span>
              ) : null}
            </DetailRow>
            <DetailRow label="Reward type">
              <StatusBadge status={reward.rewardType} />
            </DetailRow>
            <DetailRow label="Value amount">{formatRewardValue(reward.valueAmount)}</DetailRow>
            <DetailRow label="Expiry days">{formatExpiryDays(reward.expiryDays)}</DetailRow>
            <DetailRow label="Total stock">{formatNumber(reward.totalStock)}</DetailRow>
            <DetailRow label="Issued count">{formatNumber(reward.issuedCount)}</DetailRow>
            <DetailRow label="Stock">
              {formatStock(reward.totalStock, reward.issuedCount)}
            </DetailRow>
            <DetailRow label="Active">
              <StatusBadge status={reward.active ? 'ACTIVE' : 'INACTIVE'} />
            </DetailRow>
            {reward.description ? (
              <div className="sm:col-span-2">
                <DetailRow label="Description">{reward.description}</DetailRow>
              </div>
            ) : null}
          </dl>

          {isVoucher ? (
            <div className="rounded-lg border border-border bg-secondary/30 p-4">
              <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Voucher pool stats
              </h3>
              {statsLoading ? (
                <SkeletonText lines={2} className="mt-3" />
              ) : voucherStats ? (
                <dl className="mt-3 grid gap-3 sm:grid-cols-2">
                  <DetailRow label="Available">
                    {formatNumber(voucherStats.availableCount)}
                  </DetailRow>
                  <DetailRow label="Redeemed">
                    {formatNumber(voucherStats.redeemedCount)}
                  </DetailRow>
                </dl>
              ) : (
                <p className="mt-2 text-sm text-muted-foreground">No voucher stats available.</p>
              )}
            </div>
          ) : null}
        </div>
      ) : null}

      {!isLoading && !error && !reward ? <ErrorState title="No reward selected" /> : null}
    </FormDrawer>
  )
}
