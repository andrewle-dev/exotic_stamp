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
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { formatDateTime } from '../../../lib/formatting/date'
import { isForbiddenError } from '../../../lib/api/errors'
import type { StampDesignResponse } from '../../../types/stamp-designs'
import { useCampaigns } from '../../campaigns/hooks'
import { useStationsList } from '../../stations/hooks'
import { useDeleteStampDesign, useStampDesigns } from '../hooks'
import { StampDesignFormDrawer } from '../components/StampDesignFormDrawer'
import { StampDesignDetailDrawer } from '../components/StampDesignDetailDrawer'
import { StampDesignsReorderDrawer } from '../components/StampDesignsReorderDrawer'
import { resolveCampaignLabel, resolveStationLabel } from '../utils/resolve-labels'
import {
  EMPTY_STAMP_FILTERS,
  STAMP_RARITY_LABELS,
  type StampDesignFilters,
  type StampRarityFilter,
  type StampStatusFilter,
} from '../filter-schema'

/**
 * Station, rarity, status, and search filters are applied client-side to the current API page.
 * When a campaign is selected, the list is loaded server-side for that campaign (full scope for reorder).
 */
function filterStampDesignsPage(
  designs: StampDesignResponse[],
  search: string,
  campaignFilter: string,
  stationFilter: string,
  rarityFilter: StampRarityFilter,
  statusFilter: StampStatusFilter,
): StampDesignResponse[] {
  const needle = search.trim().toLowerCase()

  return designs.filter((design) => {
    if (campaignFilter && design.campaignId !== campaignFilter) {
      return false
    }
    if (stationFilter && design.stationId !== stationFilter) {
      return false
    }
    if (rarityFilter !== 'ALL' && design.rarity !== rarityFilter) {
      return false
    }
    if (statusFilter !== 'ALL' && design.status !== statusFilter) {
      return false
    }
    if (!needle) {
      return true
    }
    return (
      design.name.toLowerCase().includes(needle) ||
      (design.description?.toLowerCase().includes(needle) ?? false)
    )
  })
}

export function StampDesignsPage() {
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
  } = useDraftAppliedFilters<StampDesignFilters>({ emptyFilters: EMPTY_STAMP_FILTERS })

  const [drawerOpen, setDrawerOpen] = useState(false)
  const [reorderOpen, setReorderOpen] = useState(false)
  const [editingDesign, setEditingDesign] = useState<StampDesignResponse | null>(null)
  const [deletingDesign, setDeletingDesign] = useState<StampDesignResponse | null>(null)
  const [detailDesign, setDetailDesign] = useState<StampDesignResponse | null>(null)

  const {
    campaignId: campaignFilter,
    stationId: stationFilter,
    rarity: rarityFilter,
    status: statusFilter,
  } = appliedFilters

  const listParams = useMemo(
    () => ({
      page,
      size,
      ...(campaignFilter ? { campaignId: campaignFilter } : {}),
    }),
    [page, size, campaignFilter],
  )
  const { data, isLoading, error, refetch } = useStampDesigns(listParams)
  const { data: campaignsPage } = useCampaigns({ page: 0, size: 200 })
  const { data: stationsPage } = useStationsList({ page: 0, size: 500 })
  const deleteMutation = useDeleteStampDesign()

  const campaigns = useMemo(() => campaignsPage?.content ?? [], [campaignsPage?.content])
  const stations = useMemo(() => stationsPage?.content ?? [], [stationsPage?.content])
  const selectedCampaign = useMemo(
    () => campaigns.find((c) => c.id === campaignFilter),
    [campaigns, campaignFilter],
  )

  const filteredContent = useMemo(
    () =>
      filterStampDesignsPage(
        data?.content ?? [],
        search,
        // Campaign already applied server-side when selected.
        campaignFilter ? '' : campaignFilter,
        stationFilter,
        rarityFilter,
        statusFilter,
      ),
    [data?.content, search, campaignFilter, stationFilter, rarityFilter, statusFilter],
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
    stationFilter
      ? buildLabeledFilterTag({
          id: 'station',
          label: 'Station',
          value: (() => {
            const station = stations.find((s) => s.id === stationFilter)
            return station ? `${station.code} — ${station.name}` : stationFilter
          })(),
          accent: 'station',
          onRemove: () => removeFilter('stationId', ''),
        })
      : null,
    rarityFilter !== 'ALL'
      ? buildLabeledFilterTag({
          id: 'rarity',
          label: 'Rarity',
          value: STAMP_RARITY_LABELS[rarityFilter],
          accent: 'rarity',
          onRemove: () => removeFilter('rarity', 'ALL'),
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
    ? `Showing ${filteredContent.length} filtered stamp design${filteredContent.length === 1 ? '' : 's'}`
    : `Showing ${filteredContent.length} stamp design${filteredContent.length === 1 ? '' : 's'}`

  const columns: DataTableColumn<StampDesignResponse>[] = useMemo(
    () => [
      {
        id: 'preview',
        header: 'Preview',
        ...COL_WIDTH.thumbnail,
        truncate: false,
        cell: (row) => (
          <ImageWithFallback
            src={row.previewImageUrl || row.imageUrl}
            alt={row.name}
            className="h-10 w-10"
            fallbackClassName="h-10 w-10"
          />
        ),
      },
      {
        id: 'name',
        header: 'Name',
        ...COL_WIDTH.name,
        cell: (row) => row.name,
      },
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
        id: 'station',
        header: 'Station',
        ...COL_WIDTH.entity,
        defaultWidth: 160,
        cell: (row) => {
          const { label, unknown } = resolveStationLabel(row.stationId, stations)
          return (
            <span className={unknown ? 'text-amber-700 text-xs' : 'text-sm'}>
              {label}
            </span>
          )
        },
      },
      {
        id: 'rarity',
        header: 'Rarity',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) =>
          row.rarity ? <StatusBadge status={row.rarity} /> : <span className="text-muted-foreground">—</span>,
      },
      {
        id: 'status',
        header: 'Status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) =>
          row.status ? <StatusBadge status={row.status} /> : <span className="text-muted-foreground">—</span>,
      },
      {
        id: 'updatedAt',
        header: 'Updated at',
        ...COL_WIDTH.date,
        cell: (row) => (
          <span className="text-xs text-muted-foreground">
            {row.updatedAt ? formatDateTime(row.updatedAt) : '—'}
          </span>
        ),
      },
    ],
    [campaigns, stations],
  )

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Stamp designs access denied" />
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">Stamp Designs</h2>
          <p className="text-sm text-muted-foreground">
            Manage campaign-specific collectible stamp artwork per station, with rarity and
            status controls.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="secondary"
            size="md"
            disabled={!campaignFilter}
            title={
              campaignFilter
                ? undefined
                : 'Select a campaign filter to reorder stamp designs'
            }
            onClick={() => setReorderOpen(true)}
          >
            <ArrowUpDown className="h-4 w-4" />
            Reorder
          </Button>
          <Button
            size="md"
            onClick={() => {
              setEditingDesign(null)
              setDrawerOpen(true)
            }}
          >
            <Plus className="h-4 w-4" />
            Create stamp design
          </Button>
        </div>
      </div>

      <ListFilterToolbar
        searchId="stamp-design-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by name or description…"
        activeAdvancedFilterCount={countAppliedAdvancedFilters([
          Boolean(campaignFilter),
          Boolean(stationFilter),
          rarityFilter !== 'ALL',
          statusFilter !== 'ALL',
        ])}
        filterSubtitle="Narrow the list with campaign and metadata filters."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="stamp-campaign-filter" label="Campaign" accent="campaign">
          <FilterSelect
            id="stamp-campaign-filter"
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

        <FilterGroup id="stamp-station-filter" label="Station" accent="station">
          <FilterSelect
            id="stamp-station-filter"
            value={draftFilters.stationId}
            onChange={(e) =>
              setDraftFilters((prev) => ({ ...prev, stationId: e.target.value }))
            }
          >
            <option value="">All stations</option>
            {stations.map((s) => (
              <option key={s.id} value={s.id}>
                {s.code} — {s.name}
              </option>
            ))}
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="stamp-rarity-filter" label="Rarity" accent="rarity">
          <FilterSelect
            id="stamp-rarity-filter"
            value={draftFilters.rarity}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                rarity: e.target.value as StampRarityFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="COMMON">Common</option>
            <option value="RARE">Rare</option>
            <option value="EPIC">Epic</option>
            <option value="LEGENDARY">Legendary</option>
          </FilterSelect>
        </FilterGroup>

        <FilterGroup id="stamp-status-filter" label="Status" accent="status">
          <FilterSelect
            id="stamp-status-filter"
            value={draftFilters.status}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                status: e.target.value as StampStatusFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </FilterSelect>
        </FilterGroup>
      </ListFilterToolbar>

      <ActiveFilterTags filters={activeFilters} onClearAll={clearAllFilters} />
      <FilterSummaryText text={summaryText} />

      <DataTable
        tableId="stamp-designs"
        columns={columns}
        data={filteredContent}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Stamp designs"
        actionsWidth={128}
        emptyTitle="No stamp designs found"
        emptyDescription="Create a campaign-specific stamp design or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDetailDesign(row)}
              aria-label="View stamp design"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setEditingDesign(row)
                setDrawerOpen(true)
              }}
              aria-label="Edit stamp design"
            >
              <Pencil className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDeletingDesign(row)}
              aria-label="Soft delete stamp design"
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

      <StampDesignFormDrawer
        open={drawerOpen}
        stampDesign={editingDesign}
        campaigns={campaigns}
        stations={stations}
        onClose={() => {
          setDrawerOpen(false)
          setEditingDesign(null)
        }}
      />

      {campaignFilter ? (
        <StampDesignsReorderDrawer
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

      <StampDesignDetailDrawer
        open={Boolean(detailDesign)}
        stampDesignId={detailDesign?.id ?? null}
        campaigns={campaigns}
        stations={stations}
        fallback={detailDesign}
        onClose={() => setDetailDesign(null)}
        onEdit={(design) => {
          setDetailDesign(null)
          setEditingDesign(design)
          setDrawerOpen(true)
        }}
      />

      <ConfirmDialog
        open={Boolean(deletingDesign)}
        variant="danger"
        title="Soft delete stamp design?"
        description={
          deletingDesign ? (
            <>
              This will soft-delete <strong>{deletingDesign.name}</strong>. The stamp design is
              disabled, not permanently removed.
            </>
          ) : null
        }
        confirmLabel="Soft delete stamp design"
        loading={deleteMutation.isPending}
        onCancel={() => setDeletingDesign(null)}
        onConfirm={async () => {
          if (!deletingDesign) {
            return
          }
          await deleteMutation.mutateAsync(deletingDesign.id)
          setDeletingDesign(null)
        }}
      />
    </div>
  )
}
