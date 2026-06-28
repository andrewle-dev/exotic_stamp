import { useMemo, useState } from 'react'
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { Input } from '../../../components/ui/FormField'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { formatDateTime } from '../../../lib/formatting/date'
import { isForbiddenError } from '../../../lib/api/errors'
import type {
  StampDesignResponse,
  StampDesignStatus,
  StampRarity,
} from '../../../types/stamp-designs'
import { useCampaigns } from '../../campaigns/hooks'
import { useStationsList } from '../../stations/hooks'
import { useDeleteStampDesign, useStampDesigns } from '../hooks'
import { StampDesignFormDrawer } from '../components/StampDesignFormDrawer'
import { StampDesignDetailDrawer } from '../components/StampDesignDetailDrawer'
import { resolveCampaignLabel, resolveStationLabel } from '../utils/resolve-labels'

type RarityFilter = StampRarity | 'ALL'
type StatusFilter = StampDesignStatus | 'ALL'

/**
 * Search, campaign, station, rarity, and status filters are applied client-side to the
 * current API page only. The list endpoint supports page/size query params only.
 */
function filterStampDesignsPage(
  designs: StampDesignResponse[],
  search: string,
  campaignFilter: string,
  stationFilter: string,
  rarityFilter: RarityFilter,
  statusFilter: StatusFilter,
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
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [campaignFilter, setCampaignFilter] = useState('')
  const [stationFilter, setStationFilter] = useState('')
  const [rarityFilter, setRarityFilter] = useState<RarityFilter>('ALL')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingDesign, setEditingDesign] = useState<StampDesignResponse | null>(null)
  const [deletingDesign, setDeletingDesign] = useState<StampDesignResponse | null>(null)
  const [detailDesign, setDetailDesign] = useState<StampDesignResponse | null>(null)

  const listParams = useMemo(() => ({ page, size }), [page, size])
  const { data, isLoading, error, refetch } = useStampDesigns(listParams)
  const { data: campaignsPage } = useCampaigns({ page: 0, size: 200 })
  const { data: stationsPage } = useStationsList({ page: 0, size: 500 })
  const deleteMutation = useDeleteStampDesign()

  const campaigns = useMemo(() => campaignsPage?.content ?? [], [campaignsPage?.content])
  const stations = useMemo(() => stationsPage?.content ?? [], [stationsPage?.content])

  const filteredContent = useMemo(
    () =>
      filterStampDesignsPage(
        data?.content ?? [],
        search,
        campaignFilter,
        stationFilter,
        rarityFilter,
        statusFilter,
      ),
    [data?.content, search, campaignFilter, stationFilter, rarityFilter, statusFilter],
  )

  const columns: DataTableColumn<StampDesignResponse>[] = useMemo(
    () => [
      {
        id: 'preview',
        header: 'Preview',
        cell: (row) => (
          <ImageWithFallback
            src={row.previewImageUrl || row.imageUrl}
            alt={row.name}
            className="h-10 w-10"
            fallbackClassName="h-10 w-10"
          />
        ),
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
        id: 'station',
        header: 'Station',
        cell: (row) => {
          const { label, unknown } = resolveStationLabel(row.stationId, stations)
          return (
            <span className={unknown ? 'text-amber-700 text-xs' : 'text-sm'} title={row.stationId}>
              {label}
            </span>
          )
        },
      },
      {
        id: 'rarity',
        header: 'Rarity',
        cell: (row) =>
          row.rarity ? <StatusBadge status={row.rarity} /> : <span className="text-muted-foreground">—</span>,
      },
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
      {
        id: 'updatedAt',
        header: 'Updated at',
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
            Configure stamp artwork per campaign and station with rarity and status controls.
          </p>
        </div>
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

      <p className="text-xs text-muted-foreground">
        Search and filters apply to the current page only. Pagination uses the server list endpoint.
      </p>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:flex-wrap lg:items-end">
        <div className="min-w-[200px] flex-1 space-y-1">
          <label htmlFor="stamp-design-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="stamp-design-search"
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
          <label htmlFor="stamp-campaign-filter" className="text-xs font-medium text-muted-foreground">
            Campaign
          </label>
          <select
            id="stamp-campaign-filter"
            value={campaignFilter}
            onChange={(e) => setCampaignFilter(e.target.value)}
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
          <label htmlFor="stamp-station-filter" className="text-xs font-medium text-muted-foreground">
            Station
          </label>
          <select
            id="stamp-station-filter"
            value={stationFilter}
            onChange={(e) => setStationFilter(e.target.value)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-48"
          >
            <option value="">All stations</option>
            {stations.map((s) => (
              <option key={s.id} value={s.id}>
                {s.code} — {s.name}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1">
          <label htmlFor="stamp-rarity-filter" className="text-xs font-medium text-muted-foreground">
            Rarity
          </label>
          <select
            id="stamp-rarity-filter"
            value={rarityFilter}
            onChange={(e) => setRarityFilter(e.target.value as RarityFilter)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-36"
          >
            <option value="ALL">All</option>
            <option value="COMMON">Common</option>
            <option value="RARE">Rare</option>
            <option value="EPIC">Epic</option>
            <option value="LEGENDARY">Legendary</option>
          </select>
        </div>

        <div className="space-y-1">
          <label htmlFor="stamp-status-filter" className="text-xs font-medium text-muted-foreground">
            Status
          </label>
          <select
            id="stamp-status-filter"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-36"
          >
            <option value="ALL">All</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
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
        caption="Stamp designs"
        emptyTitle="No stamp designs found"
        emptyDescription="Create a stamp design or adjust filters on this page."
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
          onSizeChange={(next) => {
            setSize(next)
            setPage(0)
          }}
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
