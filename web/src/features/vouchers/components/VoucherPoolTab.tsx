import { useMemo, useState } from 'react'
import { Eye } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { MaskedValue } from '../../../components/ui/SecretField'
import { Input } from '../../../components/ui/FormField'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDateTime } from '../../../lib/formatting/date'
import { maskVoucherCode } from '../../../lib/formatting/masking'
import { isForbiddenError } from '../../../lib/api/errors'
import type { MilestoneResponse } from '../../../types/milestones'
import type { VoucherPoolResponse, VoucherStatus } from '../../../types/vouchers'
import { resolveMilestoneLabel, buildMilestoneOptions } from '../../rewards/utils/resolve-labels'
import { useDisableVoucher, useVouchers } from '../hooks'
import { VoucherDetailDrawer } from './VoucherDetailDrawer'

const VOUCHER_STATUSES: VoucherStatus[] = [
  'AVAILABLE',
  'ASSIGNED',
  'REDEEMED',
  'EXPIRED',
  'DISABLED',
]

function filterVouchersPage(
  vouchers: VoucherPoolResponse[],
  search: string,
): VoucherPoolResponse[] {
  const needle = search.trim().toLowerCase()
  if (!needle) {
    return vouchers
  }

  return vouchers.filter((voucher) => {
    return (
      voucher.id.toLowerCase().includes(needle) ||
      (voucher.assignedUserId?.toLowerCase().includes(needle) ?? false)
    )
  })
}

interface VoucherPoolTabProps {
  milestones: MilestoneResponse[]
}

export function VoucherPoolTab({ milestones }: VoucherPoolTabProps) {
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [milestoneFilter, setMilestoneFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [detailVoucher, setDetailVoucher] = useState<VoucherPoolResponse | null>(null)
  const [disablingVoucher, setDisablingVoucher] = useState<VoucherPoolResponse | null>(null)

  const listParams = useMemo(
    () => ({
      page,
      size,
      milestoneId: milestoneFilter || undefined,
      status: statusFilter || undefined,
    }),
    [page, size, milestoneFilter, statusFilter],
  )

  const { data, isLoading, error, refetch } = useVouchers(listParams)
  const disableMutation = useDisableVoucher()

  const milestoneOptions = buildMilestoneOptions(milestones)

  const filteredContent = useMemo(
    () => filterVouchersPage(data?.content ?? [], search),
    [data?.content, search],
  )

  const columns: DataTableColumn<VoucherPoolResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Voucher code',
        cell: (row) => <MaskedValue value={row.code} maskFn={maskVoucherCode} />,
      },
      {
        id: 'milestone',
        header: 'Milestone',
        cell: (row) => {
          const { label, unknown } = resolveMilestoneLabel(row.milestoneId, milestones)
          return (
            <span className={unknown ? 'text-amber-700 text-xs' : 'text-sm'} title={row.milestoneId}>
              {label}
            </span>
          )
        },
      },
      {
        id: 'status',
        header: 'Status',
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'assignedUserId',
        header: 'Assigned user ID',
        cell: (row) =>
          row.assignedUserId ? (
            <span className="font-mono text-xs">{row.assignedUserId}</span>
          ) : (
            '—'
          ),
      },
      {
        id: 'assignedAt',
        header: 'Assigned at',
        cell: (row) => formatDateTime(row.assignedAt),
      },
      {
        id: 'expiresAt',
        header: 'Expires at',
        cell: (row) => formatDateTime(row.expiresAt),
      },
      {
        id: 'createdAt',
        header: 'Created at',
        cell: (row) => formatDateTime(row.createdAt),
      },
    ],
    [milestones],
  )

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Voucher pool access denied" />
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-muted-foreground">
        Browse and manage voucher inventory. Codes are masked by default in the table.
      </p>

      <p className="text-xs text-muted-foreground">
        Search applies to the current page only (ID and assigned user). Milestone and status filters
        use the server list endpoint.
      </p>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:flex-wrap lg:items-end">
        <div className="min-w-[200px] flex-1 space-y-1">
          <label htmlFor="voucher-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="voucher-search"
            placeholder="Search by ID or assigned user…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setSearch(searchInput.trim())
              }
            }}
          />
        </div>

        <div className="space-y-1">
          <label
            htmlFor="voucher-milestone-filter"
            className="text-xs font-medium text-muted-foreground"
          >
            Milestone
          </label>
          <select
            id="voucher-milestone-filter"
            value={milestoneFilter}
            onChange={(e) => {
              setMilestoneFilter(e.target.value)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-48"
          >
            <option value="">All milestones</option>
            {milestoneOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1">
          <label htmlFor="voucher-status-filter" className="text-xs font-medium text-muted-foreground">
            Status
          </label>
          <select
            id="voucher-status-filter"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-40"
          >
            <option value="">All statuses</option>
            {VOUCHER_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>

        <Button variant="secondary" onClick={() => setSearch(searchInput.trim())}>
          Apply
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={filteredContent}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Voucher pool"
        emptyTitle="No vouchers found"
        emptyDescription="Import voucher codes or adjust filters on this page."
        rowActions={(row) => (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setDetailVoucher(row)}
            aria-label="View voucher"
          >
            <Eye className="h-4 w-4" />
          </Button>
        )}
      />

      {data ? (
        <Pagination
          page={data.page}
          size={data.size}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          onPageChange={setPage}
          onSizeChange={(next) => {
            setSize(next)
            setPage(0)
          }}
        />
      ) : null}

      <VoucherDetailDrawer
        open={Boolean(detailVoucher)}
        voucherId={detailVoucher?.id ?? null}
        milestones={milestones}
        fallback={detailVoucher}
        onClose={() => setDetailVoucher(null)}
        onDisable={(voucher) => {
          setDetailVoucher(null)
          setDisablingVoucher(voucher)
        }}
      />

      <ConfirmDialog
        open={Boolean(disablingVoucher)}
        variant="danger"
        title="Disable voucher?"
        description={
          disablingVoucher ? (
            <>
              This will permanently disable the voucher for milestone{' '}
              <strong>
                {resolveMilestoneLabel(disablingVoucher.milestoneId, milestones).label}
              </strong>
              . Disabled vouchers can no longer be issued or redeemed.
            </>
          ) : null
        }
        confirmLabel="Disable voucher"
        loading={disableMutation.isPending}
        onCancel={() => setDisablingVoucher(null)}
        onConfirm={async () => {
          if (!disablingVoucher) {
            return
          }
          await disableMutation.mutateAsync({ id: disablingVoucher.id })
          setDisablingVoucher(null)
        }}
      />
    </div>
  )
}
