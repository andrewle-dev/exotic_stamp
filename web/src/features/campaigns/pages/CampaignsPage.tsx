import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { Input } from '../../../components/ui/FormField'
import { formatDateTime } from '../../../lib/formatting/date'
import { formatDateRange } from '../../../lib/campaigns/schedule'
import { isForbiddenError } from '../../../lib/api/errors'
import { ROUTES } from '../../../lib/constants/routes'
import type { CampaignResponse, CampaignStatus, CampaignType } from '../../../types/campaigns'
import { useCampaigns, useDeleteCampaign } from '../hooks'
import { CampaignFormDrawer } from '../components/CampaignFormDrawer'

type TypeFilter = CampaignType | 'ALL'
type StatusFilter = CampaignStatus | 'ALL'

/**
 * Search, type, and status filters are applied client-side to the current API page only.
 * The list endpoint supports page/size query params only (no server-side filtering).
 */
function filterCampaignsPage(
  campaigns: CampaignResponse[],
  search: string,
  typeFilter: TypeFilter,
  statusFilter: StatusFilter,
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
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingCampaign, setEditingCampaign] = useState<CampaignResponse | null>(null)
  const [deletingCampaign, setDeletingCampaign] = useState<CampaignResponse | null>(null)

  const listParams = useMemo(() => ({ page, size }), [page, size])
  const { data, isLoading, error, refetch } = useCampaigns(listParams)
  const deleteMutation = useDeleteCampaign()

  const filteredContent = useMemo(
    () => filterCampaignsPage(data?.content ?? [], search, typeFilter, statusFilter),
    [data?.content, search, typeFilter, statusFilter],
  )

  const columns: DataTableColumn<CampaignResponse>[] = useMemo(
    () => [
      {
        id: 'code',
        header: 'Code',
        cell: (row) => <span className="font-mono text-xs">{row.code}</span>,
      },
      { id: 'name', header: 'Name', cell: (row) => row.name },
      {
        id: 'displayName',
        header: 'Display name',
        cell: (row) => row.displayName ?? '—',
      },
      {
        id: 'campaignType',
        header: 'Type',
        cell: (row) => (
          <StatusBadge status={row.campaignType ?? 'STANDARD'} label={undefined} />
        ),
      },
      {
        id: 'status',
        header: 'Status',
        cell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'dateRange',
        header: 'Date range',
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
        cell: (row) => row.priority ?? '—',
      },
      {
        id: 'updatedAt',
        header: 'Updated at',
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

      <p className="text-xs text-muted-foreground">
        Search and filters apply to the current page only. Pagination uses the server list endpoint.
      </p>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:items-end">
        <div className="flex-1 space-y-1">
          <label htmlFor="campaign-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="campaign-search"
            placeholder="Search by code or name…"
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
          <label htmlFor="campaign-type" className="text-xs font-medium text-muted-foreground">
            Type
          </label>
          <select
            id="campaign-type"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value as TypeFilter)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-40"
          >
            <option value="ALL">All</option>
            <option value="STANDARD">Standard</option>
            <option value="SEASONAL">Seasonal</option>
            <option value="EVENT">Event</option>
          </select>
        </div>
        <div className="space-y-1">
          <label htmlFor="campaign-status" className="text-xs font-medium text-muted-foreground">
            Status
          </label>
          <select
            id="campaign-status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm lg:w-40"
          >
            <option value="ALL">All</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="ARCHIVED">Archived</option>
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
        caption="Campaigns"
        emptyTitle="No campaigns found"
        emptyDescription="Create a campaign or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate(ROUTES.campaignDetail(row.id))}
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
          onSizeChange={(next) => {
            setSize(next)
            setPage(0)
          }}
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
