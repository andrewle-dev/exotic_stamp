import type { ReactNode } from 'react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { Button } from '../../../components/ui/Button'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { SecretField } from '../../../components/ui/SecretField'
import { SkeletonText } from '../../../components/ui/LoadingSkeleton'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDateTime } from '../../../lib/formatting/date'
import { maskVoucherCode } from '../../../lib/formatting/masking'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import type { MilestoneResponse } from '../../../types/milestones'
import type { VoucherPoolResponse } from '../../../types/vouchers'
import { canDisableVoucher, resolveMilestoneLabel } from '../../rewards/utils/resolve-labels'
import { useVoucher } from '../hooks'

interface VoucherDetailDrawerProps {
  open: boolean
  voucherId: string | null
  milestones: MilestoneResponse[]
  fallback?: VoucherPoolResponse | null
  onClose: () => void
  onDisable?: (voucher: VoucherPoolResponse) => void
}

function DetailRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="space-y-1">
      <dt className="text-xs font-medium text-muted-foreground">{label}</dt>
      <dd className="text-sm text-foreground">{children}</dd>
    </div>
  )
}

export function VoucherDetailDrawer({
  open,
  voucherId,
  milestones,
  fallback,
  onClose,
  onDisable,
}: VoucherDetailDrawerProps) {
  const { data, isLoading, error } = useVoucher(open ? voucherId ?? undefined : undefined)
  const voucher = data ?? fallback

  const milestoneLabel = voucher
    ? resolveMilestoneLabel(voucher.milestoneId, milestones)
    : null

  const disableAllowed = voucher ? canDisableVoucher(voucher.status) : false

  const footer =
    voucher && onDisable && disableAllowed ? (
      <>
        <Button variant="secondary" size="md" onClick={onClose}>
          Close
        </Button>
        <Button variant="danger" size="md" onClick={() => onDisable(voucher)}>
          Disable voucher
        </Button>
      </>
    ) : voucher ? (
      <Button variant="secondary" size="md" onClick={onClose}>
        Close
      </Button>
    ) : undefined

  return (
    <FormDrawer
      open={open}
      title="Voucher Details"
      description="Voucher code is masked by default"
      onClose={onClose}
      footer={footer}
      width="lg"
    >
      {isLoading && !voucher ? <SkeletonText lines={8} /> : null}

      {!isLoading && error && isForbiddenError(error) ? (
        <PermissionDeniedState title="Voucher access denied" />
      ) : null}

      {!isLoading && error && isNotFoundError(error) ? (
        <ErrorState title="Voucher not found" message="It may have been removed." />
      ) : null}

      {!isLoading && error && !isForbiddenError(error) && !isNotFoundError(error) ? (
        <ErrorState title="Could not load voucher" error={error} />
      ) : null}

      {voucher ? (
        <div className="space-y-6">
          <div className="flex flex-wrap gap-2">
            <StatusBadge status={voucher.status} />
          </div>

          <dl className="grid gap-4 sm:grid-cols-2">
            <DetailRow label="ID">
              <span className="font-mono text-xs">{voucher.id}</span>
            </DetailRow>
            <DetailRow label="Milestone">
              <span className={milestoneLabel?.unknown ? 'text-amber-700' : undefined}>
                {milestoneLabel?.label}
              </span>
              <span className="mt-0.5 block font-mono text-xs text-muted-foreground">
                {voucher.milestoneId}
              </span>
            </DetailRow>
            <div className="sm:col-span-2">
              <DetailRow label="Voucher code">
                <SecretField
                  value={voucher.code}
                  revealable
                  copyable
                  maskFn={maskVoucherCode}
                  ariaLabel="voucher code"
                />
              </DetailRow>
            </div>
            <DetailRow label="Status">
              <StatusBadge status={voucher.status} />
            </DetailRow>
            <DetailRow label="Assigned user ID">
              {voucher.assignedUserId ? (
                <span className="font-mono text-xs">{voucher.assignedUserId}</span>
              ) : (
                '—'
              )}
            </DetailRow>
            <DetailRow label="Assigned user reward ID">
              {voucher.assignedUserRewardId ? (
                <span className="font-mono text-xs">{voucher.assignedUserRewardId}</span>
              ) : (
                '—'
              )}
            </DetailRow>
            <DetailRow label="Assigned at">{formatDateTime(voucher.assignedAt)}</DetailRow>
            <DetailRow label="Expires at">{formatDateTime(voucher.expiresAt)}</DetailRow>
            <DetailRow label="Created at">{formatDateTime(voucher.createdAt)}</DetailRow>
          </dl>
        </div>
      ) : null}

      {!isLoading && !error && !voucher ? <ErrorState title="No voucher selected" /> : null}
    </FormDrawer>
  )
}
