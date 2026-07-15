import type { ReactNode } from 'react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { Button } from '../../../components/ui/Button'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { SkeletonText } from '../../../components/ui/LoadingSkeleton'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDate } from '../../../lib/formatting/date'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import type { PartnerResponse } from '../../../types/partners'
import { deriveContractStatus } from '../utils/contract-status'
import { usePartner } from '../hooks'

interface PartnerDetailDrawerProps {
  open: boolean
  partnerId: string | null
  fallback?: PartnerResponse | null
  onClose: () => void
  onEdit?: (partner: PartnerResponse) => void
}

function DetailRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="space-y-1">
      <dt className="text-xs font-medium text-muted-foreground">{label}</dt>
      <dd className="text-sm text-foreground">{children}</dd>
    </div>
  )
}

export function PartnerDetailDrawer({
  open,
  partnerId,
  fallback,
  onClose,
  onEdit,
}: PartnerDetailDrawerProps) {
  const { data, isLoading, error } = usePartner(open ? partnerId ?? undefined : undefined)
  const partner = data ?? fallback

  const footer =
    partner && onEdit ? (
      <>
        <Button variant="secondary" size="md" onClick={onClose}>
          Close
        </Button>
        <Button size="md" onClick={() => onEdit(partner)}>
          Edit partner
        </Button>
      </>
    ) : undefined

  const contractStatus = partner
    ? deriveContractStatus(partner.contractStartDate, partner.contractEndDate)
    : null

  return (
    <FormDrawer
      open={open}
      title="Partner Details"
      description={partner?.name}
      onClose={onClose}
      footer={footer}
      width="lg"
    >
      {isLoading && !partner ? <SkeletonText lines={6} /> : null}

      {!isLoading && error && isForbiddenError(error) ? (
        <PermissionDeniedState title="Partner access denied" />
      ) : null}

      {!isLoading && error && isNotFoundError(error) ? (
        <ErrorState title="Partner not found" message="It may have been removed." />
      ) : null}

      {!isLoading && error && !isForbiddenError(error) && !isNotFoundError(error) ? (
        <ErrorState title="Could not load partner" error={error} />
      ) : null}

      {partner ? (
        <div className="space-y-6">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start">
            <ImageWithFallback
              src={partner.logoUrl}
              alt={partner.name}
              className="h-20 w-20 shrink-0 rounded-lg"
              fallbackClassName="h-20 w-20 shrink-0 rounded-lg"
            />
            <div className="flex flex-wrap gap-2">
              {contractStatus ? <StatusBadge status={contractStatus} /> : null}
              <StatusBadge status={partner.active ? 'ACTIVE' : 'INACTIVE'} />
            </div>
          </div>

          <dl className="grid gap-4 sm:grid-cols-2">
            <DetailRow label="Name">{partner.name}</DetailRow>
            <DetailRow label="Contact email">{partner.contactEmail ?? '—'}</DetailRow>
            <DetailRow label="Contract start">
              {formatDate(partner.contractStartDate)}
            </DetailRow>
            <DetailRow label="Contract end">{formatDate(partner.contractEndDate)}</DetailRow>

            {partner.bannerImageUrl ? (
              <div className="sm:col-span-2">
                <DetailRow label="Banner">
                  <ImageWithFallback
                    src={partner.bannerImageUrl}
                    alt={`${partner.name} banner`}
                    className="aspect-video h-auto w-full max-w-md object-cover"
                    fallbackClassName="aspect-video h-auto w-full max-w-md"
                  />
                </DetailRow>
              </div>
            ) : null}
          </dl>
        </div>
      ) : null}

      {!isLoading && !error && !partner ? (
        <ErrorState title="No partner selected" />
      ) : null}
    </FormDrawer>
  )
}
