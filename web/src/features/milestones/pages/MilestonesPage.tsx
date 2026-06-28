import { useMemo, useState } from 'react'
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { Input } from '../../../components/ui/FormField'
import { isForbiddenError } from '../../../lib/api/errors'
import type { MilestoneResponse, MilestoneRewardType, MilestoneStatus } from '../../../types/milestones'
import { useCampaigns } from '../../campaigns/hooks'
import { resolveCampaignLabel } from '../../stamp-designs/utils/resolve-labels'
import { useDeleteMilestone, useMilestones } from '../hooks'
import { MilestoneFormDrawer } from '../components/MilestoneFormDrawer'
import { MilestoneDetailDrawer } from '../components/MilestoneDetailDrawer'

type StatusFilter = MilestoneStatus | 'ALL'
type RewardTypeFilter = MilestoneRewardType | 'ALL'

function filterMilestonesPage(
  milestones: MilestoneResponse[],
  search: string,
  rewardTypeFilter: RewardTypeFilter,
): MilestoneResponse[] {
  const needle = search.trim().toLowerCase()

  return milestones.filter((milestone) => {
    if (rewardTypeFilter !== 'ALL' && milestone.rewardType !== rewardTypeFilter) {
      return false
    }
    if (!needle) {
      return true
    }
    return (
      milestone.code.toLowerCase().includes(needle) ||
      milestone.name.toLowerCase().includes(needle) ||
      milestone.rewardTitle.toLowerCase().includes(needle) ||
      (milestone.description?.toLowerCase().includes(needle) ?? false)
    )
  })
}

export function MilestonesPage() {
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [campaignFilter, setCampaignFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [rewardTypeFilter, setRewardTypeFilter] = useState<RewardTypeFilter>('ALL')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingMilestone, setEditingMilestone] = useState<MilestoneResponse | null>(null)
  const [detailMilestone, setDetailMilestone] = useState<MilestoneResponse | null>(null)
  const [deletingMilestone, setDeletingMilestone] = useState<MilestoneResponse | null>(null)

  const listParams = useMemo(
    () => ({
      page,
      size,
      campaignId: campaignFilter || undefined,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
    }),
    [page, size, campaignFilter, statusFilter],
  )

  const { data, isLoading, error, refetch } = useMilestones(listParams)
  const { data: campaignsPage } = useCampaigns({ page: 0, size: 200 })
  const deleteMutation = useDeleteMilestone()

  const campaigns = useMemo(() => campaignsPage?.content ?? [], [campaignsPage?.content])

  const filteredContent = useMemo(
    () => filterMilestonesPage(data?.content ?? [], search, rewardTypeFilter),
    [data?.content, search, rewardTypeFilter],
  )

  const columns: DataTableColumn<MilestoneResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      { id: 'name', header: 'Name', cell: (row) => row.name },
      {
        id: 'campaign',
        header: 'Campaign',
        cell: (row) => {
          const { label, unknown } = resolveCampaignLabel(row.campaignId, campaigns)
          return (
            <span className={unknown ? 'text-amber-700 text-xs' : 'text-sm'} title={row.campaignId}>
              {label}
            </span>
          )
        },
      },
      {
        id: 'requiredStampCount',
        header: 'Required stamps',
        align: 'right',
        cell: (row) => `${row.requiredStampCount} stamps`,
      },
      {
        id: 'rewardType',
        header: 'Reward type',
        cell: (row) => <StatusBadge status={row.rewardType} />,
      },
      { id: 'rewardTitle', header: 'Reward title', cell: (row) => row.rewardTitle },
      {
        id: 'status',
        header: 'Status',
        cell: (row) =>
          row.status ? <StatusBadge status={row.status} /> : <span className="text-muted-foreground">—</span>,
      },
      {
        id: 'sortOrder',
        header: 'Sort order',
        align: 'right',
        cell: (row) => row.sortOrder ?? '—',
      },
    ],
    [campaigns],
  )

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Milestones access denied" />
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">Milestones</h2>
          <p className="text-sm text-muted-foreground">
            Define stamp collection milestones and linked reward types.
          </p>
        </div>
        <Button
          size="md"
          onClick={() => {
            setEditingMilestone(null)
            setDrawerOpen(true)
          }}
        >
          <Plus className="h-4 w-4" />
          Create milestone
        </Button>
      </div>

      <p className="text-xs text-muted-foreground">
        Search and reward type filter apply to the current page only. Campaign and status filters
        use the server list endpoint.
      </p>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:flex-wrap lg:items-end">
        <div className="min-w-[200px] flex-1 space-y-1">
          <label htmlFor="milestone-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="milestone-search"
            placeholder="Search by code, name, or reward…"
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
          <label htmlFor="milestone-campaign-filter" className="text-xs font-medium text-muted-foreground">
            Campaign
          </label>
          <select
            id="milestone-campaign-filter"
            value={campaignFilter}
            onChange={(e) => {
              setCampaignFilter(e.target.value)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-48"
          >
            <option value="">All campaigns</option>
            {campaigns.map((c) => (
              <option key={c.id} value={c.id}>
                {c.displayName || c.name} ({c.code})
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1">
          <label htmlFor="milestone-status-filter" className="text-xs font-medium text-muted-foreground">
            Status
          </label>
          <select
            id="milestone-status-filter"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as StatusFilter)
              setPage(0)
            }}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-36"
          >
            <option value="ALL">All</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </div>

        <div className="space-y-1">
          <label
            htmlFor="milestone-reward-type-filter"
            className="text-xs font-medium text-muted-foreground"
          >
            Reward type
          </label>
          <select
            id="milestone-reward-type-filter"
            value={rewardTypeFilter}
            onChange={(e) => setRewardTypeFilter(e.target.value as RewardTypeFilter)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-40"
          >
            <option value="ALL">All</option>
            <option value="VOUCHER">Voucher</option>
            <option value="DIGITAL_STICKER">Digital sticker</option>
            <option value="BONUS_STAMP">Bonus stamp</option>
          </select>
        </div>

        <Button
          variant="secondary"
          onClick={() => {
            setSearch(searchInput.trim())
          }}
        >
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
        caption="Milestones"
        emptyTitle="No milestones found"
        emptyDescription="Create a milestone or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDetailMilestone(row)}
              aria-label="View milestone"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setEditingMilestone(row)
                setDrawerOpen(true)
              }}
              aria-label="Edit milestone"
            >
              <Pencil className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDeletingMilestone(row)}
              aria-label="Soft delete milestone"
            >
              <Trash2 className="h-4 w-4 text-destructive" />
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

      <MilestoneFormDrawer
        open={drawerOpen}
        milestone={editingMilestone}
        campaigns={campaigns}
        onClose={() => {
          setDrawerOpen(false)
          setEditingMilestone(null)
        }}
      />

      <MilestoneDetailDrawer
        open={Boolean(detailMilestone)}
        milestoneId={detailMilestone?.id ?? null}
        campaigns={campaigns}
        fallback={detailMilestone}
        onClose={() => setDetailMilestone(null)}
        onEdit={(milestone) => {
          setDetailMilestone(null)
          setEditingMilestone(milestone)
          setDrawerOpen(true)
        }}
      />

      <ConfirmDialog
        open={Boolean(deletingMilestone)}
        variant="danger"
        title="Soft delete milestone?"
        description={
          deletingMilestone ? (
            <>
              This will soft-delete <strong>{deletingMilestone.name}</strong> ({deletingMilestone.code}
              ). The milestone is disabled, not permanently removed.
            </>
          ) : null
        }
        confirmLabel="Soft delete milestone"
        loading={deleteMutation.isPending}
        onCancel={() => setDeletingMilestone(null)}
        onConfirm={async () => {
          if (!deletingMilestone) {
            return
          }
          await deleteMutation.mutateAsync(deletingMilestone.id)
          setDeletingMilestone(null)
        }}
      />
    </div>
  )
}
