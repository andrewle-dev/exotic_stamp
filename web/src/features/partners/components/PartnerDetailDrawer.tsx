import type { ReactNode } from 'react'
import { Check, Copy } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { Button } from '../../../components/ui/Button'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { SkeletonText } from '../../../components/ui/LoadingSkeleton'
import { ErrorState } from '../../../components/ui/ErrorState'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDate } from '../../../lib/formatting/date'
import { useCopyToClipboard } from '../../../lib/utils/useCopyToClipboard'
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

            <div className="sm:col-span-2">
              <UrlCopyRow label="Logo URL" url={partner.logoUrl} />
            </div>

            {partner.logoUrl ? (
              <div className="sm:col-span-2">
                <DetailRow label="Logo preview">
                  <ImageWithFallback
                    src={partner.logoUrl}
                    alt={`${partner.name} logo`}
                    className="h-24 w-24"
                    fallbackClassName="h-24 w-24"
                  />
                </DetailRow>
              </div>
            ) : null}

            <DetailRow label="ID">
              <span className="font-mono text-xs">{partner.id}</span>
            </DetailRow>
          </dl>
        </div>
      ) : null}

      {!isLoading && !error && !partner ? (
        <ErrorState title="No partner selected" />
      ) : null}
    </FormDrawer>
  )
}
