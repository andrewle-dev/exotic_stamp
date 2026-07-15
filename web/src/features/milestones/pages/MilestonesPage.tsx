import { useMemo, useState } from 'react'
import { ArrowUpDown, Eye, Pencil, Plus, Trash2 } from 'lucide-react'
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
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { isForbiddenError } from '../../../lib/api/errors'
import type { MilestoneResponse } from '../../../types/milestones'
import { useCampaigns } from '../../campaigns/hooks'
import { resolveCampaignLabel } from '../../stamp-designs/utils/resolve-labels'
import { useDeleteMilestone, useMilestones } from '../hooks'
import { MilestoneFormDrawer } from '../components/MilestoneFormDrawer'
import { MilestoneDetailDrawer } from '../components/MilestoneDetailDrawer'
import { MilestonesReorderDrawer } from '../components/MilestonesReorderDrawer'
import {
  EMPTY_MILESTONE_FILTERS,
  MILESTONE_REWARD_TYPE_LABELS,
  type MilestoneFilters,
  type MilestoneRewardTypeFilter,
  type MilestoneStatusFilter,
} from '../filter-schema'

function filterMilestonesPage(
  milestones: MilestoneResponse[],
  search: string,
  rewardTypeFilter: MilestoneRewardTypeFilter,
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
  } = useDraftAppliedFilters<MilestoneFilters>({ emptyFilters: EMPTY_MILESTONE_FILTERS })

  const [drawerOpen, setDrawerOpen] = useState(false)
  const [reorderOpen, setReorderOpen] = useState(false)
  const [editingMilestone, setEditingMilestone] = useState<MilestoneResponse | null>(null)
  const [detailMilestone, setDetailMilestone] = useState<MilestoneResponse | null>(null)
  const [deletingMilestone, setDeletingMilestone] = useState<MilestoneResponse | null>(null)

  const {
    campaignId: campaignFilter,
    status: statusFilter,
    rewardType: rewardTypeFilter,
  } = appliedFilters

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
  const selectedCampaign = useMemo(
    () => campaigns.find((c) => c.id === campaignFilter),
    [campaigns, campaignFilter],
  )

  const filteredContent = useMemo(
    () => filterMilestonesPage(data?.content ?? [], search, rewardTypeFilter),
    [data?.content, search, rewardTypeFilter],
  )

  const activeFilters = collectFilterTags([
    buildSearchFilterTag({ search, onRemove: clearSearch }),
    campaignFilter
      ? buildLabeledFilterTag({
          id: 'campaign',
          label: 'Campaign',
          value: (() => {
            const campaign = campaigns.find((c) => c.id === campaignFilter)
            return campaign ? campaign.displayName || campaign.name : campaignFilter
          })(),
          accent: 'campaign',
          onRemove: () => removeFilter('campaignId', ''),
        })
      : null,
    statusFilter !== 'ALL'
      ? buildLabeledFilterTag({
          id: 'status',
          label: 'Status',
          value: statusFilter,
          accent: 'status',
          onRemove: () => removeFilter('status', 'ALL'),
        })
      : null,
    rewardTypeFilter !== 'ALL'
      ? buildLabeledFilterTag({
          id: 'reward',
          label: 'Reward type',
          value: MILESTONE_REWARD_TYPE_LABELS[rewardTypeFilter],
          accent: 'reward',
          onRemove: () => removeFilter('rewardType', 'ALL'),
        })
      : null,
  ])

  const hasActiveFilters = activeFilters.length > 0
  const summaryText = hasActiveFilters
    ? `Showing ${filteredContent.length} filtered milestone${filteredContent.length === 1 ? '' : 's'}`
    : `Showing ${filteredContent.length} milestone${filteredContent.length === 1 ? '' : 's'}`

  const columns: DataTableColumn<MilestoneResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        ...COL_WIDTH.code,
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      { id: 'name', header: 'Name', ...COL_WIDTH.name, cell: (row) => row.name },
      {
        id: 'campaign',
        header: 'Campaign',
        ...COL_WIDTH.entity,
        cell: (row) => {
          const { label, unknown } = resolveCampaignLabel(row.campaignId, campaigns)
          return (
            <span className={unknown ? 'text-amber-700 text-xs' : 'text-sm'}>
              {label}
            </span>
          )
        },
      },
      {
        id: 'requiredStampCount',
        header: 'Required stamps',
        align: 'right',
        defaultWidth: 130,
        minWidth: 100,
        cell: (row) => `${row.requiredStampCount} stamps`,
      },
      {
        id: 'rewardType',
        header: 'Reward type',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => <StatusBadge status={row.rewardType} />,
      },
      {
        id: 'rewardTitle',
        header: 'Reward title',
        ...COL_WIDTH.title,
        cell: (row) => row.rewardTitle,
      },
      {
        id: 'status',
        header: 'Status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) =>
          row.status ? <StatusBadge status={row.status} /> : <span className="text-muted-foreground">—</span>,
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
        <div className="flex flex-wrap gap-2">
          <Button
            variant="secondary"
            size="md"
            disabled={!campaignFilter}
            title={
              campaignFilter ? undefined : 'Select a campaign filter to reorder milestones'
            }
            onClick={() => setReorderOpen(true)}
          >
            <ArrowUpDown className="h-4 w-4" />
            Reorder
          </Button>
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
      </div>

      <ListFilterToolbar
        searchId="milestone-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by code, name, or reward…"
        activeAdvancedFilterCount={countAppliedAdvancedFilters([
          Boolean(campaignFilter),
          statusFilter !== 'ALL',
          rewardTypeFilter !== 'ALL',
        ])}
        filterSubtitle="Narrow the list with campaign and reward filters."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="milestone-campaign-filter" label="Campaign" accent="campaign">
          <FilterSelect
            id="milestone-campaign-filter"
            value={draftFilters.campaignId}
            onChange={(e) =>
              setDraftFilters((prev) => ({ ...prev, campaignId: e.target.value }))
            }
          >
            <option value="">All campaigns</option>
            {campaigns.map((c) => (
              <option key={c.id} value={c.id}>
                {c.displayName || c.name} ({c.code})
              </option>
            ))}
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="milestone-status-filter" label="Status" accent="status">
          <FilterSelect
            id="milestone-status-filter"
            value={draftFilters.status}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                status: e.target.value as MilestoneStatusFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="ARCHIVED">Archived</option>
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="milestone-reward-type-filter" label="Reward type" accent="reward">
          <FilterSelect
            id="milestone-reward-type-filter"
            value={draftFilters.rewardType}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                rewardType: e.target.value as MilestoneRewardTypeFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="VOUCHER">Voucher</option>
            <option value="DIGITAL_STICKER">Digital sticker</option>
            <option value="BONUS_STAMP">Bonus stamp</option>
          </FilterSelect>
        </FilterGroup>
      </ListFilterToolbar>

      <ActiveFilterTags filters={activeFilters} onClearAll={clearAllFilters} />
      <FilterSummaryText text={summaryText} />

      <DataTable
        tableId="milestones"
        columns={columns}
        data={filteredContent}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Milestones"
        actionsWidth={128}
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
          onSizeChange={setSize}
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

      {campaignFilter ? (
        <MilestonesReorderDrawer
          open={reorderOpen}
          campaignId={campaignFilter}
          campaignLabel={
            selectedCampaign
              ? `${selectedCampaign.code} · ${selectedCampaign.name}`
              : undefined
          }
          onClose={() => setReorderOpen(false)}
        />
      ) : null}

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
