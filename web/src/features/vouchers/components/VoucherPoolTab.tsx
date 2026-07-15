import { useMemo, useState } from 'react'
import { Eye } from 'lucide-react'
import {
  ActiveFilterTags,
  FilterGroup,
  FilterSelect,
  FilterSummaryText,
  ListFilterToolbar,
  buildLabeledFilterTag,
  buildSearchFilterTag,
  collectFilterTags,
  countAppliedAdvancedFilters,
  useDraftAppliedFilters,
} from '../../../components/filters'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { MaskedValue } from '../../../components/ui/SecretField'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { formatDateTime } from '../../../lib/formatting/date'
import { maskVoucherCode } from '../../../lib/formatting/masking'
import { isForbiddenError } from '../../../lib/api/errors'
import type { MilestoneResponse } from '../../../types/milestones'
import type { VoucherPoolResponse, VoucherStatus } from '../../../types/vouchers'
import { resolveMilestoneLabel, buildMilestoneOptions } from '../../rewards/utils/resolve-labels'
import { useDisableVoucher, useVouchers } from '../hooks'
import { EMPTY_VOUCHER_FILTERS, type VoucherFilters } from '../filter-schema'
import { VoucherDetailDrawer } from './VoucherDetailDrawer'

const VOUCHER_STATUSES: VoucherStatus[] = [
  'AVAILABLE',
  'ASSIGNED',
  'REDEEMED',
  'EXPIRED',
  'DISABLED',
]

/**
 * Search applies client-side to the current API page (ID and assigned user).
 * Milestone and status filters use the server list endpoint.
 */
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
  const {
    draftFilters,
    setDraftFilters,
    appliedFilters,
    search,
    searchInput,
    setSearchInput,
    applySearch,
    clearSearch,
    applyFilters,
    resetFilters,
    removeFilter,
    clearAllFilters,
    page,
    setPage,
    size,
    setSize,
  } = useDraftAppliedFilters<VoucherFilters>({ emptyFilters: EMPTY_VOUCHER_FILTERS })

  const [detailVoucher, setDetailVoucher] = useState<VoucherPoolResponse | null>(null)
  const [disablingVoucher, setDisablingVoucher] = useState<VoucherPoolResponse | null>(null)

  const { milestoneId: milestoneFilter, status: statusFilter } = appliedFilters

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

  const activeFilters = collectFilterTags([
    buildSearchFilterTag({ search, onRemove: clearSearch }),
    milestoneFilter
      ? buildLabeledFilterTag({
          id: 'milestone',
          label: 'Milestone',
          value: resolveMilestoneLabel(milestoneFilter, milestones).label,
          accent: 'milestone',
          onRemove: () => removeFilter('milestoneId', ''),
        })
      : null,
    statusFilter
      ? buildLabeledFilterTag({
          id: 'status',
          label: 'Status',
          value: statusFilter.replace(/_/g, ' '),
          accent: 'status',
          onRemove: () => removeFilter('status', ''),
        })
      : null,
  ])

  const hasActiveFilters = activeFilters.length > 0
  const summaryText = hasActiveFilters
    ? `Showing ${filteredContent.length} filtered voucher${filteredContent.length === 1 ? '' : 's'}`
    : `Showing ${filteredContent.length} voucher${filteredContent.length === 1 ? '' : 's'}`

  const columns: DataTableColumn<VoucherPoolResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Voucher code',
        defaultWidth: 160,
        minWidth: 120,
        truncate: false,
        cell: (row) => <MaskedValue value={row.code} maskFn={maskVoucherCode} />,
      },
      {
        id: 'milestone',
        header: 'Milestone',
        ...COL_WIDTH.entity,
        cell: (row) => {
          const { label, unknown } = resolveMilestoneLabel(row.milestoneId, milestones)
          return (
            <span className={unknown ? 'text-amber-700 text-xs' : 'text-sm'}>
              {label}
            </span>
          )
        },
      },
      {
        id: 'status',
        header: 'Status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'assigned',
        header: 'Assignment',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => (row.assignedUserId ? 'Assigned' : 'Unassigned'),
      },
      {
        id: 'assignedAt',
        header: 'Assigned at',
        ...COL_WIDTH.date,
        cell: (row) => formatDateTime(row.assignedAt),
      },
      {
        id: 'expiresAt',
        header: 'Expires at',
        ...COL_WIDTH.date,
        cell: (row) => formatDateTime(row.expiresAt),
      },
      {
        id: 'createdAt',
        header: 'Created at',
        ...COL_WIDTH.date,
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

      <ListFilterToolbar
        searchId="voucher-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by ID or assigned user…"
        activeAdvancedFilterCount={countAppliedAdvancedFilters([
          Boolean(milestoneFilter),
          Boolean(statusFilter),
        ])}
        filterSubtitle="Narrow the list by milestone and voucher status."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="voucher-milestone-filter" label="Milestone" accent="milestone">
          <FilterSelect
            id="voucher-milestone-filter"
            value={draftFilters.milestoneId}
            onChange={(e) =>
              setDraftFilters((prev) => ({ ...prev, milestoneId: e.target.value }))
            }
          >
            <option value="">All milestones</option>
            {milestoneOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="voucher-status-filter" label="Status" accent="status">
          <FilterSelect
            id="voucher-status-filter"
            value={draftFilters.status}
            onChange={(e) =>
              setDraftFilters((prev) => ({ ...prev, status: e.target.value }))
            }
          >
            <option value="">All statuses</option>
            {VOUCHER_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </FilterSelect>
        </FilterGroup>
      </ListFilterToolbar>

      <ActiveFilterTags filters={activeFilters} onClearAll={clearAllFilters} />
      <FilterSummaryText text={summaryText} />

      <DataTable
        tableId="voucher-pool"
        columns={columns}
        data={filteredContent}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Voucher pool"
        actionsWidth={72}
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
          onSizeChange={setSize}
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
