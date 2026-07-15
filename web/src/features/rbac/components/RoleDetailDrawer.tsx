import type { ReactNode } from 'react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { SkeletonText } from '../../../components/ui/LoadingSkeleton'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import type { RoleResponse } from '../../../types/rbac'
import { useRole } from '../hooks'

interface RoleDetailDrawerProps {
  open: boolean
  role: RoleResponse | null
  onClose: () => void
  onEdit?: (role: RoleResponse) => void
}

function DetailRow({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="flex flex-col gap-0.5 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
      <dt className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</dt>
      <dd className="text-sm text-foreground sm:text-right">{value}</dd>
    </div>
  )
}

export function RoleDetailDrawer({ open, role, onClose, onEdit }: RoleDetailDrawerProps) {
  const { data, isLoading, error } = useRole(open ? role?.id : undefined)
  const detail = data ?? role

  return (
    <FormDrawer
      open={open}
      title="Role Details"
      description={detail?.role ?? 'View role metadata'}
      onClose={onClose}
      footer={
        <div className="flex w-full justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded-md border border-border px-4 py-2 text-sm font-medium text-foreground hover:bg-secondary"
          >
            Close
          </button>
          {detail && onEdit && !detail.systemRole ? (
            <button
              type="button"
              onClick={() => onEdit(detail)}
              className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:opacity-90"
            >
              Edit role
            </button>
          ) : null}
        </div>
      }
    >
      {isLoading && !detail ? <SkeletonText lines={4} /> : null}
      {error ? <ApiErrorAlert error={error} /> : null}
      {detail ? (
        <dl className="space-y-4">
          <DetailRow label="Role code" value={detail.role ?? '—'} />
          <DetailRow label="Description" value={detail.description?.trim() || '—'} />
          <DetailRow
            label="Status"
            value={detail.status ? <StatusBadge status={detail.status} /> : '—'}
          />
          <DetailRow
            label="System role"
            value={detail.systemRole ? <StatusBadge status="ACTIVE" label="Yes" /> : 'No'}
          />
          {detail.systemRole ? (
            <p className="text-xs text-muted-foreground">
              System roles are protected and should not be modified casually.
            </p>
          ) : null}
        </dl>
      ) : null}
    </FormDrawer>
  )
}
