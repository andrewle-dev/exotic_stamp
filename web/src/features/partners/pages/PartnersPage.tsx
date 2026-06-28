import { useMemo, useState } from 'react'
import { Eye, Pencil, Plus } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { Input } from '../../../components/ui/FormField'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { formatDate } from '../../../lib/formatting/date'
import { isForbiddenError } from '../../../lib/api/errors'
import type { PartnerResponse } from '../../../types/partners'
import {
  useActivatePartner,
  useDeactivatePartner,
  usePartners,
} from '../hooks'
import { PartnerFormDrawer } from '../components/PartnerFormDrawer'
import { PartnerDetailDrawer } from '../components/PartnerDetailDrawer'
import { deriveContractStatus } from '../utils/contract-status'

type ActiveFilter = 'ALL' | 'ACTIVE_ONLY' | 'INACTIVE_ONLY'

function filterPartnersPage(
  partners: PartnerResponse[],
  search: string,
  activeFilter: ActiveFilter,
): PartnerResponse[] {
  const needle = search.trim().toLowerCase()

  return partners.filter((partner) => {
    if (activeFilter === 'INACTIVE_ONLY' && partner.active) {
      return false
    }
    if (!needle) {
      return true
    }
    return (
      partner.name.toLowerCase().includes(needle) ||
      (partner.contactEmail?.toLowerCase().includes(needle) ?? false)
    )
  })
}

export function PartnersPage() {
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ALL')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingPartner, setEditingPartner] = useState<PartnerResponse | null>(null)
  const [detailPartner, setDetailPartner] = useState<PartnerResponse | null>(null)
  const [togglingPartner, setTogglingPartner] = useState<PartnerResponse | null>(null)

  const listParams = useMemo(
    () => ({
      page,
      size,
      activeOnly: activeFilter === 'ACTIVE_ONLY' ? true : undefined,
    }),
    [page, size, activeFilter],
  )

  const { data, isLoading, error, refetch } = usePartners(listParams)
  const activateMutation = useActivatePartner()
  const deactivateMutation = useDeactivatePartner()
  const toggleMutation = togglingPartner?.active ? deactivateMutation : activateMutation

  const filteredContent = useMemo(
    () => filterPartnersPage(data?.content ?? [], search, activeFilter),
    [data?.content, search, activeFilter],
  )

  const columns: DataTableColumn<PartnerResponse>[] = useMemo(
    () => [
      {
        id: 'logo',
        header: 'Logo',
        cell: (row) => (
          <ImageWithFallback
            src={row.logoUrl}
            alt={row.name}
            className="h-10 w-10 rounded"
            fallbackClassName="h-10 w-10 rounded"
          />
        ),
      },
      { id: 'name', header: 'Name', cell: (row) => row.name },
      {
        id: 'contactEmail',
        header: 'Contact email',
        cell: (row) => row.contactEmail ?? '—',
      },
      {
        id: 'contractStart',
        header: 'Contract start',
        cell: (row) => formatDate(row.contractStartDate),
      },
      {
        id: 'contractEnd',
        header: 'Contract end',
        cell: (row) => formatDate(row.contractEndDate),
      },
      {
        id: 'contractStatus',
        header: 'Contract status',
        cell: (row) => (
          <StatusBadge
            status={deriveContractStatus(row.contractStartDate, row.contractEndDate)}
          />
        ),
      },
      {
        id: 'active',
        header: 'Active',
        cell: (row) => <StatusBadge status={row.active ? 'ACTIVE' : 'INACTIVE'} />,
      },
    ],
    [],
  )

  if (!isLoading && error && isForbiddenError(error)) {
    return <PermissionDeniedState title="Partners access denied" />
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">Partners</h2>
          <p className="text-sm text-muted-foreground">
            Manage reward partner accounts, contracts, and activation status.
          </p>
        </div>
        <Button
          size="md"
          onClick={() => {
            setEditingPartner(null)
            setDrawerOpen(true)
          }}
        >
          <Plus className="h-4 w-4" />
          Add partner
        </Button>
      </div>

      <p className="text-xs text-muted-foreground">
        Search and inactive filter apply to the current page only. Active-only filter uses the
        server list endpoint.
      </p>

      <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 lg:flex-row lg:flex-wrap lg:items-end">
        <div className="min-w-[200px] flex-1 space-y-1">
          <label htmlFor="partner-search" className="text-xs font-medium text-muted-foreground">
            Search
          </label>
          <Input
            id="partner-search"
            placeholder="Search by name or email…"
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
          <label htmlFor="partner-active-filter" className="text-xs font-medium text-muted-foreground">
            Active
          </label>
          <select
            id="partner-active-filter"
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
        caption="Partners"
        emptyTitle="No partners found"
        emptyDescription="Create a partner or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDetailPartner(row)}
              aria-label="View partner"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setEditingPartner(row)
                setDrawerOpen(true)
              }}
              aria-label="Edit partner"
            >
              <Pencil className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className={row.active ? 'text-destructive' : 'text-emerald-700'}
              onClick={() => setTogglingPartner(row)}
              aria-label={row.active ? 'Deactivate partner' : 'Activate partner'}
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

      <PartnerFormDrawer
        open={drawerOpen}
        partner={editingPartner}
        onClose={() => {
          setDrawerOpen(false)
          setEditingPartner(null)
        }}
      />

      <PartnerDetailDrawer
        open={Boolean(detailPartner)}
        partnerId={detailPartner?.id ?? null}
        fallback={detailPartner}
        onClose={() => setDetailPartner(null)}
        onEdit={(partner) => {
          setDetailPartner(null)
          setEditingPartner(partner)
          setDrawerOpen(true)
        }}
      />

      <ConfirmDialog
        open={Boolean(togglingPartner)}
        variant={togglingPartner?.active ? 'danger' : 'default'}
        title={togglingPartner?.active ? 'Deactivate partner?' : 'Activate partner?'}
        description={
          togglingPartner ? (
            <>
              {togglingPartner.active ? (
                <>
                  This will deactivate <strong>{togglingPartner.name}</strong>. The partner will no
                  longer be available for new reward configuration.
                </>
              ) : (
                <>
                  This will activate <strong>{togglingPartner.name}</strong>. The partner will be
                  available for reward configuration.
                </>
              )}
            </>
          ) : null
        }
        confirmLabel={togglingPartner?.active ? 'Deactivate partner' : 'Activate partner'}
        loading={toggleMutation.isPending}
        onCancel={() => setTogglingPartner(null)}
        onConfirm={async () => {
          if (!togglingPartner) {
            return
          }
          if (togglingPartner.active) {
            await deactivateMutation.mutateAsync(togglingPartner.id)
          } else {
            await activateMutation.mutateAsync(togglingPartner.id)
          }
          setTogglingPartner(null)
        }}
      />
    </div>
  )
}
