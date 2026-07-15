import { useMemo, useState } from 'react'
import { BarChart3, Eye, Pencil, Plus, Upload } from 'lucide-react'
import {
  ActiveFilterTags,
  FilterGroup,
  FilterSelect,
  FilterSummaryText,
  ListFilterToolbar,
  ACTIVE_STATE_FILTER_LABELS,
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
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { isForbiddenError } from '../../../lib/api/errors'
import type { MilestoneResponse } from '../../../types/milestones'
import type { PartnerResponse } from '../../../types/partners'
import type { RewardResponse } from '../../../types/rewards'
import {
  formatExpiryDays,
  formatRewardValue,
  formatStock,
  resolveMilestoneLabel,
  resolvePartnerLabel,
  buildMilestoneOptions,
} from '../utils/resolve-labels'
import {
  useActivateReward,
  useDeactivateReward,
  useRewardVoucherStats,
  useRewards,
} from '../hooks'
import {
  EMPTY_REWARD_FILTERS,
  REWARD_TYPE_LABELS,
  type RewardActiveFilter,
  type RewardFilters,
  type RewardTypeFilter,
} from '../filter-schema'
import { RewardFormDrawer } from './RewardFormDrawer'
import { RewardDetailDrawer } from './RewardDetailDrawer'
import { BulkUploadVouchersDrawer } from './BulkUploadVouchersDrawer'

/**
 * Search, inactive, reward type, milestone, and partner filters apply client-side to the
 * current API page. Active-only uses the server list endpoint (activeOnly).
 */
function filterRewardsPage(
  rewards: RewardResponse[],
  search: string,
  activeFilter: RewardActiveFilter,
  rewardTypeFilter: RewardTypeFilter,
  milestoneId: string,
  partnerId: string,
): RewardResponse[] {
  const needle = search.trim().toLowerCase()

  return rewards.filter((reward) => {
    if (activeFilter === 'ACTIVE_ONLY' && !reward.active) {
      return false
    }
    if (activeFilter === 'INACTIVE_ONLY' && reward.active) {
      return false
    }
    if (rewardTypeFilter !== 'ALL' && reward.rewardType !== rewardTypeFilter) {
      return false
    }
    if (milestoneId && reward.milestoneId !== milestoneId) {
      return false
    }
    if (partnerId && reward.partnerId !== partnerId) {
      return false
    }
    if (!needle) {
      return true
    }
    return (
      reward.name.toLowerCase().includes(needle) ||
      (reward.description?.toLowerCase().includes(needle) ?? false)
    )
  })
}

function VoucherStatsButton({
  reward,
  onViewDetail,
}: {
  reward: RewardResponse
  onViewDetail: () => void
}) {
  const { data: stats } = useRewardVoucherStats(reward.id)

  return (
    <Button
      variant="ghost"
      size="sm"
      className="px-2"
      onClick={onViewDetail}
      aria-label="View voucher stats"
      title={
        stats
          ? `Available: ${stats.availableCount ?? 0}, Redeemed: ${stats.redeemedCount ?? 0}`
          : 'View voucher stats'
      }
    >
      <BarChart3 className="h-4 w-4" />
    </Button>
  )
}

interface RewardsTabProps {
  milestones: MilestoneResponse[]
  partners: PartnerResponse[]
}

export function RewardsTab({ milestones, partners }: RewardsTabProps) {
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
  } = useDraftAppliedFilters<RewardFilters>({ emptyFilters: EMPTY_REWARD_FILTERS })

  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingReward, setEditingReward] = useState<RewardResponse | null>(null)
  const [detailReward, setDetailReward] = useState<RewardResponse | null>(null)
  const [bulkUploadReward, setBulkUploadReward] = useState<RewardResponse | null>(null)
  const [togglingReward, setTogglingReward] = useState<RewardResponse | null>(null)

  const {
    active: activeFilter,
    rewardType: rewardTypeFilter,
    milestoneId: milestoneFilter,
    partnerId: partnerFilter,
  } = appliedFilters

  const listParams = useMemo(
    () => ({
      page,
      size,
      activeOnly: activeFilter === 'ACTIVE_ONLY' ? true : undefined,
    }),
    [page, size, activeFilter],
  )

  const { data, isLoading, error, refetch } = useRewards(listParams)
  const activateMutation = useActivateReward()
  const deactivateMutation = useDeactivateReward()
  const toggleMutation = togglingReward?.active ? deactivateMutation : activateMutation

  const milestoneOptions = buildMilestoneOptions(milestones)

  const filteredContent = useMemo(
    () =>
      filterRewardsPage(
        data?.content ?? [],
        search,
        activeFilter,
        rewardTypeFilter,
        milestoneFilter,
        partnerFilter,
      ),
    [data?.content, search, activeFilter, rewardTypeFilter, milestoneFilter, partnerFilter],
  )

  const activeFilters = collectFilterTags([
    buildSearchFilterTag({ search, onRemove: clearSearch }),
    activeFilter !== 'ALL'
      ? buildLabeledFilterTag({
          id: 'active',
          label: 'Active',
          value: ACTIVE_STATE_FILTER_LABELS[activeFilter],
          accent: 'status',
          onRemove: () => removeFilter('active', 'ALL'),
        })
      : null,
    rewardTypeFilter !== 'ALL'
      ? buildLabeledFilterTag({
          id: 'rewardType',
          label: 'Reward type',
          value: REWARD_TYPE_LABELS[rewardTypeFilter],
          accent: 'reward',
          onRemove: () => removeFilter('rewardType', 'ALL'),
        })
      : null,
    milestoneFilter
      ? buildLabeledFilterTag({
          id: 'milestone',
          label: 'Milestone',
          value: resolveMilestoneLabel(milestoneFilter, milestones).label,
          accent: 'milestone',
          onRemove: () => removeFilter('milestoneId', ''),
        })
      : null,
    partnerFilter
      ? buildLabeledFilterTag({
          id: 'partner',
          label: 'Partner',
          value: resolvePartnerLabel(partnerFilter, partners).label,
          accent: 'partner',
          onRemove: () => removeFilter('partnerId', ''),
        })
      : null,
  ])

  const hasActiveFilters = activeFilters.length > 0
  const summaryText = hasActiveFilters
    ? `Showing ${filteredContent.length} filtered reward${filteredContent.length === 1 ? '' : 's'}`
    : `Showing ${filteredContent.length} reward${filteredContent.length === 1 ? '' : 's'}`

  const columns: DataTableColumn<RewardResponse>[] = useMemo(
    () => [
      { id: 'name', header: 'Name', ...COL_WIDTH.name, cell: (row) => row.name },
      {
        id: 'rewardType',
        header: 'Reward type',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => <StatusBadge status={row.rewardType} />,
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
        id: 'partner',
        header: 'Partner',
        ...COL_WIDTH.entity,
        cell: (row) => {
          const { label, unknown } = resolvePartnerLabel(row.partnerId, partners)
          return (
            <span className={unknown ? 'text-amber-700 text-xs' : 'text-sm'}>
              {label}
            </span>
          )
        },
      },
      {
        id: 'valueAmount',
        header: 'Value amount',
        align: 'right',
        ...COL_WIDTH.number,
        defaultWidth: 120,
        cell: (row) => formatRewardValue(row.valueAmount),
      },
      {
        id: 'expiryDays',
        header: 'Expiry days',
        align: 'right',
        ...COL_WIDTH.number,
        defaultWidth: 110,
        cell: (row) => formatExpiryDays(row.expiryDays),
      },
      {
        id: 'stock',
        header: 'Stock',
        align: 'right',
        ...COL_WIDTH.number,
        defaultWidth: 110,
        cell: (row) => formatStock(row.totalStock, row.issuedCount),
      },
      {
        id: 'issued',
        header: 'Issued',
        align: 'right',
        ...COL_WIDTH.number,
        cell: (row) => row.issuedCount ?? '—',
      },
      {
        id: 'active',
        header: 'Active',
        ...COL_WIDTH.badgeSm,
        truncate: false,
        cell: (row) => <StatusBadge status={row.active ? 'ACTIVE' : 'INACTIVE'} />,
      },
    ],
    [milestones, partners],
  )

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Rewards access denied" />
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-muted-foreground">
          Manage reward catalog entries linked to milestones and partners.
        </p>
        <Button
          size="md"
          onClick={() => {
            setEditingReward(null)
            setDrawerOpen(true)
          }}
        >
          <Plus className="h-4 w-4" />
          Create reward
        </Button>
      </div>

      <p className="text-xs text-muted-foreground">
        Search, inactive, reward type, milestone, and partner filters apply to the current page only.
        Active-only uses the server list endpoint.
      </p>

      <ListFilterToolbar
        searchId="reward-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by name or description…"
        activeAdvancedFilterCount={countAppliedAdvancedFilters([
          activeFilter !== 'ALL',
          rewardTypeFilter !== 'ALL',
          Boolean(milestoneFilter),
          Boolean(partnerFilter),
        ])}
        filterSubtitle="Narrow the list by activation status, reward type, milestone, and partner."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="reward-active-filter" label="Active" accent="status">
          <FilterSelect
            id="reward-active-filter"
            value={draftFilters.active}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                active: e.target.value as RewardActiveFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="ACTIVE_ONLY">Active only</option>
            <option value="INACTIVE_ONLY">Inactive only</option>
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="reward-type-filter" label="Reward type" accent="reward">
          <FilterSelect
            id="reward-type-filter"
            value={draftFilters.rewardType}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                rewardType: e.target.value as RewardTypeFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="VOUCHER">Voucher</option>
            <option value="DIGITAL_STICKER">Digital sticker</option>
            <option value="BONUS_STAMP">Bonus stamp</option>
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="reward-milestone-filter" label="Milestone" accent="milestone">
          <FilterSelect
            id="reward-milestone-filter"
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

        <FilterGroup id="reward-partner-filter" label="Partner" accent="partner">
          <FilterSelect
            id="reward-partner-filter"
            value={draftFilters.partnerId}
            onChange={(e) =>
              setDraftFilters((prev) => ({ ...prev, partnerId: e.target.value }))
            }
          >
            <option value="">All partners</option>
            {partners.map((partner) => (
              <option key={partner.id} value={partner.id}>
                {partner.name}
              </option>
            ))}
          </FilterSelect>
        </FilterGroup>
      </ListFilterToolbar>

      <ActiveFilterTags filters={activeFilters} onClearAll={clearAllFilters} />
      <FilterSummaryText text={summaryText} />

      <DataTable
        tableId="rewards"
        columns={columns}
        data={filteredContent}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Rewards"
        actionsWidth={280}
        emptyTitle="No rewards found"
        emptyDescription="Create a reward or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              className="px-2"
              onClick={() => setDetailReward(row)}
              aria-label="View reward"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="px-2"
              onClick={() => {
                setEditingReward(row)
                setDrawerOpen(true)
              }}
              aria-label="Edit reward"
            >
              <Pencil className="h-4 w-4" />
            </Button>
            {row.rewardType === 'VOUCHER' ? (
              <>
                <VoucherStatsButton reward={row} onViewDetail={() => setDetailReward(row)} />
                <Button
                  variant="ghost"
                  size="sm"
                  className="px-2"
                  onClick={() => setBulkUploadReward(row)}
                  aria-label="Upload voucher codes"
                >
                  <Upload className="h-4 w-4" />
                </Button>
              </>
            ) : null}
            <Button
              variant="ghost"
              size="sm"
              className={
                row.active ? 'px-2 text-destructive' : 'px-2 text-emerald-700'
              }
              onClick={() => setTogglingReward(row)}
              aria-label={row.active ? 'Deactivate reward' : 'Activate reward'}
            >
              {row.active ? 'Deactivate' : 'Activate'}
            </Button>
          </>
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

      <RewardFormDrawer
        open={drawerOpen}
        reward={editingReward}
        milestones={milestones}
        partners={partners}
        onClose={() => {
          setDrawerOpen(false)
          setEditingReward(null)
        }}
      />

      <RewardDetailDrawer
        open={Boolean(detailReward)}
        rewardId={detailReward?.id ?? null}
        milestones={milestones}
        partners={partners}
        fallback={detailReward}
        onClose={() => setDetailReward(null)}
        onEdit={(reward) => {
          setDetailReward(null)
          setEditingReward(reward)
          setDrawerOpen(true)
        }}
        onBulkUpload={(reward) => {
          setDetailReward(null)
          setBulkUploadReward(reward)
        }}
      />

      <BulkUploadVouchersDrawer
        open={Boolean(bulkUploadReward)}
        reward={bulkUploadReward}
        onClose={() => setBulkUploadReward(null)}
      />

      <ConfirmDialog
        open={Boolean(togglingReward)}
        variant={togglingReward?.active ? 'danger' : 'default'}
        title={togglingReward?.active ? 'Deactivate reward?' : 'Activate reward?'}
        description={
          togglingReward ? (
            togglingReward.active ? (
              <>
                This will deactivate <strong>{togglingReward.name}</strong>. Users will no longer
                receive this reward from the milestone.
              </>
            ) : (
              <>
                This will activate <strong>{togglingReward.name}</strong>. The reward will be
                available for issuance again.
              </>
            )
          ) : null
        }
        confirmLabel={togglingReward?.active ? 'Deactivate reward' : 'Activate reward'}
        loading={toggleMutation.isPending}
        onCancel={() => setTogglingReward(null)}
        onConfirm={async () => {
          if (!togglingReward) {
            return
          }
          if (togglingReward.active) {
            await deactivateMutation.mutateAsync(togglingReward.id)
          } else {
            await activateMutation.mutateAsync(togglingReward.id)
          }
          setTogglingReward(null)
        }}
      />
    </div>
  )
}
