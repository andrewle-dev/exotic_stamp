import { useMemo, useState } from 'react'
import { BarChart3, Eye, Pencil, Plus, Upload } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { Input } from '../../../components/ui/FormField'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { isForbiddenError } from '../../../lib/api/errors'
import type { MilestoneResponse } from '../../../types/milestones'
import type { PartnerResponse } from '../../../types/partners'
import type { RewardResponse, RewardType } from '../../../types/rewards'
import {
  formatExpiryDays,
  formatRewardValue,
  formatStock,
  resolveMilestoneLabel,
  resolvePartnerLabel,
} from '../utils/resolve-labels'
import {
  useActivateReward,
  useDeactivateReward,
  useRewardVoucherStats,
  useRewards,
} from '../hooks'
import { RewardFormDrawer } from './RewardFormDrawer'
import { RewardDetailDrawer } from './RewardDetailDrawer'
import { BulkUploadVouchersDrawer } from './BulkUploadVouchersDrawer'

type ActiveFilter = 'ALL' | 'ACTIVE_ONLY' | 'INACTIVE_ONLY'
type RewardTypeFilter = RewardType | 'ALL'

function filterRewardsPage(
  rewards: RewardResponse[],
  search: string,
  activeFilter: ActiveFilter,
  rewardTypeFilter: RewardTypeFilter,
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
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ALL')
  const [rewardTypeFilter, setRewardTypeFilter] = useState<RewardTypeFilter>('ALL')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingReward, setEditingReward] = useState<RewardResponse | null>(null)
  const [detailReward, setDetailReward] = useState<RewardResponse | null>(null)
  const [bulkUploadReward, setBulkUploadReward] = useState<RewardResponse | null>(null)
  const [togglingReward, setTogglingReward] = useState<RewardResponse | null>(null)

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

  const filteredContent = useMemo(
    () => filterRewardsPage(data?.content ?? [], search, activeFilter, rewardTypeFilter),
    [data?.content, search, activeFilter, rewardTypeFilter],
  )

  const columns: DataTableColumn<RewardResponse>[] = useMemo(
    () => [
      { id: 'name', header: 'Name', cell: (row) => row.name },
      {
        id: 'rewardType',
        header: 'Reward type',
        cell: (row) => <StatusBadge status={row.rewardType} />,
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
        id: 'partner',
        header: 'Partner',
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
        cell: (row) => formatRewardValue(row.valueAmount),
      },
      {
        id: 'expiryDays',
        header: 'Expiry days',
        align: 'right',
        cell: (row) => formatExpiryDays(row.expiryDays),
      },
      {
        id: 'stock',
        header: 'Stock',
        align: 'right',
        cell: (row) => formatStock(row.totalStock, row.issuedCount),
      },
      {
        id: 'issued',
        header: 'Issued',
        align: 'right',
        cell: (row) => row.issuedCount ?? '—',
      },
      {
        id: 'active',
        header: 'Active',
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
        Search, inactive filter, and reward type filter apply to the current page only. Active-only
        filter uses the server list endpoint.
      </p>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:flex-wrap lg:items-end">
        <div className="min-w-[200px] flex-1 space-y-1">
          <label htmlFor="reward-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="reward-search"
            placeholder="Search by name or description…"
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
          <label htmlFor="reward-active-filter" className="text-xs font-medium text-muted-foreground">
            Active
          </label>
          <select
            id="reward-active-filter"
            value={activeFilter}
            onChange={(e) => {
              setActiveFilter(e.target.value as ActiveFilter)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-40"
          >
            <option value="ALL">All</option>
            <option value="ACTIVE_ONLY">Active only</option>
            <option value="INACTIVE_ONLY">Inactive only</option>
          </select>
        </div>

        <div className="space-y-1">
          <label
            htmlFor="reward-type-filter"
            className="text-xs font-medium text-muted-foreground"
          >
            Reward type
          </label>
          <select
            id="reward-type-filter"
            value={rewardTypeFilter}
            onChange={(e) => setRewardTypeFilter(e.target.value as RewardTypeFilter)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-44"
          >
            <option value="ALL">All</option>
            <option value="VOUCHER">Voucher</option>
            <option value="DIGITAL_STICKER">Digital sticker</option>
            <option value="BONUS_STAMP">Bonus stamp</option>
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
        caption="Rewards"
        emptyTitle="No rewards found"
        emptyDescription="Create a reward or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDetailReward(row)}
              aria-label="View reward"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
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
              className={row.active ? 'text-destructive' : 'text-emerald-700'}
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
          onSizeChange={(next) => {
            setSize(next)
            setPage(0)
          }}
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
