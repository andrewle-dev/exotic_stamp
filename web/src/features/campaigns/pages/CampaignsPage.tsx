import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react'
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
import { formatDateTime } from '../../../lib/formatting/date'
import { formatDateRange } from '../../../lib/campaigns/schedule'
import { isForbiddenError } from '../../../lib/api/errors'
import { ROUTES } from '../../../lib/constants/routes'
import { detailFromListState } from '../../../lib/navigation/useSafeBackNavigation'
import type { CampaignResponse } from '../../../types/campaigns'
import { useCampaigns, useDeleteCampaign } from '../hooks'
import { CampaignFormDrawer } from '../components/CampaignFormDrawer'
import {
  CAMPAIGN_TYPE_LABELS,
  EMPTY_CAMPAIGN_FILTERS,
  type CampaignFilters,
  type CampaignStatusFilter,
  type CampaignTypeFilter,
} from '../filter-schema'

/**
 * Search, type, and status filters are applied client-side to the current API page only.
 * The list endpoint supports page/size query params only (no server-side filtering).
 */
function filterCampaignsPage(
  campaigns: CampaignResponse[],
  search: string,
  typeFilter: CampaignTypeFilter,
  statusFilter: CampaignStatusFilter,
): CampaignResponse[] {
  const needle = search.trim().toLowerCase()

  return campaigns.filter((campaign) => {
    if (typeFilter !== 'ALL' && campaign.campaignType !== typeFilter) {
      return false
    }
    if (statusFilter !== 'ALL' && campaign.status !== statusFilter) {
      return false
    }
    if (!needle) {
      return true
    }
    return (
      campaign.code.toLowerCase().includes(needle) ||
      campaign.name.toLowerCase().includes(needle) ||
      (campaign.displayName?.toLowerCase().includes(needle) ?? false)
    )
  })
}

export function CampaignsPage() {
  const navigate = useNavigate()
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
  } = useDraftAppliedFilters<CampaignFilters>({ emptyFilters: EMPTY_CAMPAIGN_FILTERS })

  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingCampaign, setEditingCampaign] = useState<CampaignResponse | null>(null)
  const [deletingCampaign, setDeletingCampaign] = useState<CampaignResponse | null>(null)

  const { type: typeFilter, status: statusFilter } = appliedFilters

  const listParams = useMemo(() => ({ page, size }), [page, size])
  const { data, isLoading, error, refetch } = useCampaigns(listParams)
  const deleteMutation = useDeleteCampaign()

  const filteredContent = useMemo(
    () => filterCampaignsPage(data?.content ?? [], search, typeFilter, statusFilter),
    [data?.content, search, typeFilter, statusFilter],
  )

  const activeFilters = collectFilterTags([
    buildSearchFilterTag({ search, onRemove: clearSearch }),
    typeFilter !== 'ALL'
      ? buildLabeledFilterTag({
          id: 'type',
          label: 'Type',
          value: CAMPAIGN_TYPE_LABELS[typeFilter],
          accent: 'type',
          onRemove: () => removeFilter('type', 'ALL'),
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
  ])

  const hasActiveFilters = activeFilters.length > 0
  const summaryText = hasActiveFilters
    ? `Showing ${filteredContent.length} filtered campaign${filteredContent.length === 1 ? '' : 's'}`
    : `Showing ${filteredContent.length} campaign${filteredContent.length === 1 ? '' : 's'}`

  const columns: DataTableColumn<CampaignResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        ...COL_WIDTH.code,
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      { id: 'name', header: 'Name', ...COL_WIDTH.name, cell: (row) => row.name },
      {
        id: 'displayName',
        header: 'Display name',
        ...COL_WIDTH.title,
        cell: (row) => row.displayName ?? '—',
      },
      {
        id: 'campaignType',
        header: 'Type',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => (
          <StatusBadge status={row.campaignType ?? 'STANDARD'} label={undefined} />
        ),
      },
      {
        id: 'status',
        header: 'Status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'dateRange',
        header: 'Date range',
        ...COL_WIDTH.dateRange,
        cell: (row) => (
          <span className="text-xs text-muted-foreground">
            {formatDateRange(row.startAt, row.endAt)}
          </span>
        ),
      },
      {
        id: 'priority',
        header: 'Priority',
        align: 'right',
        ...COL_WIDTH.metric,
        cell: (row) => row.priority ?? '—',
      },
      {
        id: 'updatedAt',
        header: 'Updated at',
        ...COL_WIDTH.date,
        cell: (row) => (
          <span className="text-xs text-muted-foreground">{formatDateTime(row.updatedAt)}</span>
        ),
      },
    ],
    [],
  )

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Campaigns access denied" />
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">Campaigns</h2>
          <p className="text-sm text-muted-foreground">
            Create and manage campaigns, activation rules, and station assignments.
          </p>
        </div>
        <Button
          size="md"
          onClick={() => {
            setEditingCampaign(null)
            setDrawerOpen(true)
          }}
        >
          <Plus className="h-4 w-4" />
          Create campaign
        </Button>
      </div>

      <ListFilterToolbar
        searchId="campaign-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by code or name…"
        activeAdvancedFilterCount={countAppliedAdvancedFilters([
          typeFilter !== 'ALL',
          statusFilter !== 'ALL',
        ])}
        filterSubtitle="Narrow the list by campaign type and status."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="campaign-type-filter" label="Type" accent="type">
          <FilterSelect
            id="campaign-type-filter"
            value={draftFilters.type}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                type: e.target.value as CampaignTypeFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="STANDARD">Standard</option>
            <option value="SEASONAL">Seasonal</option>
            <option value="EVENT">Event</option>
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="campaign-status-filter" label="Status" accent="status">
          <FilterSelect
            id="campaign-status-filter"
            value={draftFilters.status}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                status: e.target.value as CampaignStatusFilter,
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
      </ListFilterToolbar>

      <ActiveFilterTags filters={activeFilters} onClearAll={clearAllFilters} />
      <FilterSummaryText text={summaryText} />

      <DataTable
        tableId="campaigns"
        columns={columns}
        data={filteredContent}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Campaigns"
        actionsWidth={128}
        emptyTitle="No campaigns found"
        emptyDescription="Create a campaign or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() =>
                navigate(ROUTES.campaignDetail(row.id), {
                  state: detailFromListState(ROUTES.campaigns),
                })
              }
              aria-label="View campaign"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setEditingCampaign(row)
                setDrawerOpen(true)
              }}
              aria-label="Edit campaign"
            >
              <Pencil className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDeletingCampaign(row)}
              aria-label="Archive campaign"
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

      <CampaignFormDrawer
        open={drawerOpen}
        campaign={editingCampaign}
        onClose={() => {
          setDrawerOpen(false)
          setEditingCampaign(null)
        }}
      />

      <ConfirmDialog
        open={Boolean(deletingCampaign)}
        variant="danger"
        title="Archive campaign?"
        description={
          deletingCampaign ? (
            <>
              This will soft-delete <strong>{deletingCampaign.name}</strong> ({deletingCampaign.code}
              ). The campaign is archived, not permanently removed.
            </>
          ) : null
        }
        confirmLabel="Archive campaign"
        loading={deleteMutation.isPending}
        onCancel={() => setDeletingCampaign(null)}
        onConfirm={async () => {
          if (!deletingCampaign) {
            return
          }
          await deleteMutation.mutateAsync(deletingCampaign.id)
          setDeletingCampaign(null)
        }}
      />
    </div>
  )
}
