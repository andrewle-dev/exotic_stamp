import { useMemo, useState } from 'react'
import { Eye, Pencil, Plus } from 'lucide-react'
import {
  ACTIVE_STATE_FILTER_LABELS,
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
import { COL_WIDTH, ACTIONS_WIDTH_WITH_LABEL } from '../../../components/ui/table/columnWidthPresets'
import { Pagination } from '../../../components/ui/Pagination'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { ImageWithFallback } from '../../../components/ui/ImageWithFallback'
import { formatDate } from '../../../lib/formatting/date'
import { isForbiddenError } from '../../../lib/api/errors'
import { cn } from '../../../lib/utils/cn'
import type { PartnerResponse } from '../../../types/partners'
import {
  useActivatePartner,
  useDeactivatePartner,
  usePartners,
} from '../hooks'
import { PartnerFormDrawer } from '../components/PartnerFormDrawer'
import { PartnerDetailDrawer } from '../components/PartnerDetailDrawer'
import { deriveContractStatus } from '../utils/contract-status'
import {
  EMPTY_PARTNER_FILTERS,
  type PartnerActiveFilter,
  type PartnerFilters,
} from '../filter-schema'

function filterPartnersPage(
  partners: PartnerResponse[],
  search: string,
  activeFilter: PartnerActiveFilter,
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
  } = useDraftAppliedFilters<PartnerFilters>({ emptyFilters: EMPTY_PARTNER_FILTERS })

  const [drawerOpen, setDrawerOpen] = useState(false)
  const [editingPartner, setEditingPartner] = useState<PartnerResponse | null>(null)
  const [detailPartner, setDetailPartner] = useState<PartnerResponse | null>(null)
  const [togglingPartner, setTogglingPartner] = useState<PartnerResponse | null>(null)

  const { active: activeFilter } = appliedFilters

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
  ])

  const hasActiveFilters = activeFilters.length > 0
  const summaryText = hasActiveFilters
    ? `Showing ${filteredContent.length} filtered partner${filteredContent.length === 1 ? '' : 's'}`
    : `Showing ${filteredContent.length} partner${filteredContent.length === 1 ? '' : 's'}`

  const columns: DataTableColumn<PartnerResponse>[] = useMemo(
    () => [
      {
        id: 'logo',
        header: 'Logo',
        ...COL_WIDTH.thumbnail,
        truncate: false,
        cell: (row) => (
          <ImageWithFallback
            src={row.logoUrl}
            alt={row.name}
            className="h-10 w-10 rounded"
            fallbackClassName="h-10 w-10 rounded"
          />
        ),
      },
      { id: 'name', header: 'Name', ...COL_WIDTH.name, cell: (row) => row.name },
      {
        id: 'contactEmail',
        header: 'Contact email',
        ...COL_WIDTH.email,
        cell: (row) => row.contactEmail ?? '—',
      },
      {
        id: 'contractStart',
        header: 'Contract start',
        ...COL_WIDTH.date,
        cell: (row) => formatDate(row.contractStartDate),
      },
      {
        id: 'contractEnd',
        header: 'Contract end',
        ...COL_WIDTH.date,
        cell: (row) => formatDate(row.contractEndDate),
      },
      {
        id: 'contractStatus',
        header: 'Contract status',
        ...COL_WIDTH.badge,
        truncate: false,
        cell: (row) => (
          <StatusBadge
            status={deriveContractStatus(row.contractStartDate, row.contractEndDate)}
          />
        ),
      },
      {
        id: 'active',
        header: 'Active',
        ...COL_WIDTH.badgeSm,
        truncate: false,
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

      <ListFilterToolbar
        searchId="partner-search"
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        onSearchSubmit={() => applySearch()}
        searchPlaceholder="Search by name or email…"
        activeAdvancedFilterCount={countAppliedAdvancedFilters([activeFilter !== 'ALL'])}
        filterSubtitle="Narrow the list by partner activation status."
        onApplyFilters={applyFilters}
        onClearFilters={resetFilters}
      >
        <FilterGroup id="partner-active-filter" label="Active" accent="status">
          <FilterSelect
            id="partner-active-filter"
            value={draftFilters.active}
            onChange={(e) =>
              setDraftFilters((prev) => ({
                ...prev,
                active: e.target.value as PartnerActiveFilter,
              }))
            }
          >
            <option value="ALL">All</option>
            <option value="ACTIVE_ONLY">Active only</option>
            <option value="INACTIVE_ONLY">Inactive only</option>
          </FilterSelect>
        </FilterGroup>
      </ListFilterToolbar>

      <ActiveFilterTags filters={activeFilters} onClearAll={clearAllFilters} />
      <FilterSummaryText text={summaryText} />

      <DataTable
        tableId="partners"
        columns={columns}
        data={filteredContent}
        getRowId={(row) => row.id}
        isLoading={isLoading}
        error={error}
        onRetry={() => void refetch()}
        caption="Partners"
        actionsWidth={ACTIONS_WIDTH_WITH_LABEL}
        emptyTitle="No partners found"
        emptyDescription="Create a partner or adjust filters on this page."
        rowActions={(row) => (
          <>
            <Button
              variant="ghost"
              size="sm"
              className="px-2"
              onClick={() => setDetailPartner(row)}
              aria-label="View partner"
            >
              <Eye className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="px-2"
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
              className={cn(
                'px-2',
                row.active ? 'text-destructive' : 'text-emerald-700',
              )}
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
          onSizeChange={setSize}
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
