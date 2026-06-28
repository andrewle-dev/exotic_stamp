import { useMemo, useState } from 'react'
import { cn } from '../../../lib/utils/cn'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { EmptyState } from '../../../components/ui/EmptyState'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { isForbiddenError } from '../../../lib/api/errors'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import type { CampaignStampCountView, StationStatsResponse } from '../../../types/analytics'
import type { RewardResponse } from '../../../types/rewards'
import { AnalyticsPageHeader } from '../components/AnalyticsPageHeader'
import { AnalyticsSection } from '../components/AnalyticsSection'
import { HorizontalBar } from '../components/HorizontalBar'
import { useOperationalOverviewData } from '../hooks/useOperationalOverviewData'
import {
  calculateRemainingStock,
  calculateStockStatus,
  formatAnalyticsNumber,
  resolveCampaignLabel,
  sortStationsByCollectors,
} from '../utils/helpers'

type AnalyticsTab = 'collection' | 'stations' | 'rewards'

const TABS: { id: AnalyticsTab; label: string }[] = [
  { id: 'collection', label: 'Collection Analytics' },
  { id: 'stations', label: 'Station Analytics' },
  { id: 'rewards', label: 'Reward/Voucher Health' },
]

interface RewardStockRow {
  id: string
  name: string
  rewardType: string
  totalStock?: number
  issuedCount?: number
  remaining?: number
  stockStatus: ReturnType<typeof calculateStockStatus>
}

export function AnalyticsPage() {
  const [activeTab, setActiveTab] = useState<AnalyticsTab>('collection')

  const {
    collectionStats,
    stationStats,
    campaigns,
    rewards,
    vouchersCount,
    vouchersSample,
    lastRefreshedAt,
    refetchAll,
    isRefreshing,
  } = useOperationalOverviewData()

  const campaignsList = useMemo(() => campaigns.data?.content ?? [], [campaigns.data?.content])
  const sortedStations = useMemo(
    () => sortStationsByCollectors(stationStats.data ?? []),
    [stationStats.data],
  )
  const topStations = sortedStations.slice(0, 10)
  const bottomStations =
    sortedStations.length > 10 ? sortedStations.slice(-10).reverse() : []

  const campaignStampRows = useMemo(
    () => collectionStats.data?.stampsPerCampaign ?? [],
    [collectionStats.data?.stampsPerCampaign],
  )
  const maxCampaignStamps = useMemo(() => {
    const counts = campaignStampRows.map((row: CampaignStampCountView) => row.stampCount)
    return counts.length > 0 ? Math.max(...counts) : 0
  }, [campaignStampRows])

  const maxCollectorCount = useMemo(() => {
    const counts = sortedStations.map((row) => row.collectorCount)
    return counts.length > 0 ? Math.max(...counts) : 0
  }, [sortedStations])

  const stationsByLine = useMemo(() => {
    const groups = new Map<string, StationStatsResponse[]>()
    for (const station of sortedStations) {
      const lineName = station.lineName ?? 'Unknown line'
      const existing = groups.get(lineName) ?? []
      existing.push(station)
      groups.set(lineName, existing)
    }
    return [...groups.entries()].sort(([left], [right]) => left.localeCompare(right))
  }, [sortedStations])

  const rewardStockRows: RewardStockRow[] = useMemo(
    () =>
      (rewards.data?.content ?? []).map((reward: RewardResponse) => ({
        id: reward.id,
        name: reward.name,
        rewardType: reward.rewardType,
        totalStock: reward.totalStock,
        issuedCount: reward.issuedCount,
        remaining: calculateRemainingStock(reward.totalStock, reward.issuedCount),
        stockStatus: calculateStockStatus(reward.totalStock, reward.issuedCount),
      })),
    [rewards.data?.content],
  )

  const voucherStatusSample = useMemo(() => {
    const counts = new Map<string, number>()
    for (const voucher of vouchersSample.data?.content ?? []) {
      counts.set(voucher.status, (counts.get(voucher.status) ?? 0) + 1)
    }
    return [...counts.entries()].sort(([left], [right]) => left.localeCompare(right))
  }, [vouchersSample.data?.content])

  const campaignColumns: DataTableColumn<CampaignStampCountView>[] = useMemo(
    () => [
      {
        id: 'campaign',
        header: 'Campaign',
        cell: (row) => resolveCampaignLabel(row.campaignId, campaignsList),
      },
      {
        id: 'stamps',
        header: 'Stamps',
        align: 'right',
        cell: (row) => formatAnalyticsNumber(row.stampCount),
      },
      {
        id: 'share',
        header: 'Share of max',
        align: 'right',
        cell: (row) => {
          if (maxCampaignStamps <= 0) {
            return '—'
          }
          const percent = Math.round((row.stampCount / maxCampaignStamps) * 100)
          return `${percent}%`
        },
      },
    ],
    [campaignsList, maxCampaignStamps],
  )

  const stationColumns: DataTableColumn<StationStatsResponse>[] = useMemo(
    () => [
      {
        id: 'station',
        header: 'Station',
        cell: (row) => row.stationName,
      },
      {
        id: 'line',
        header: 'Line',
        cell: (row) => row.lineName ?? '—',
      },
      {
        id: 'collectors',
        header: 'Collectors',
        align: 'right',
        cell: (row) => formatAnalyticsNumber(row.collectorCount),
      },
    ],
    [],
  )

  const rewardColumns: DataTableColumn<RewardStockRow>[] = useMemo(
    () => [
      { id: 'name', header: 'Reward', cell: (row) => row.name },
      {
        id: 'type',
        header: 'Type',
        cell: (row) => <StatusBadge status={row.rewardType} dot={false} />,
      },
      {
        id: 'total',
        header: 'Total stock',
        align: 'right',
        cell: (row) => (row.totalStock !== undefined ? formatAnalyticsNumber(row.totalStock) : '—'),
      },
      {
        id: 'issued',
        header: 'Issued',
        align: 'right',
        cell: (row) => formatAnalyticsNumber(row.issuedCount ?? 0),
      },
      {
        id: 'remaining',
        header: 'Remaining',
        align: 'right',
        cell: (row) =>
          row.remaining !== undefined ? formatAnalyticsNumber(row.remaining) : '—',
      },
      {
        id: 'status',
        header: 'Stock status',
        cell: (row) =>
          row.stockStatus === 'UNKNOWN' ? (
            <span className="text-xs text-muted-foreground">Not available</span>
          ) : (
            <StatusBadge status={row.stockStatus} />
          ),
      },
    ],
    [],
  )

  const pageForbidden =
    (collectionStats.error && isForbiddenError(collectionStats.error)) ||
    (stationStats.error && isForbiddenError(stationStats.error))

  if (pageForbidden) {
    return <PermissionDeniedState title="Analytics access denied" />
  }

  return (
    <div className="space-y-6">
      <AnalyticsPageHeader
        title="Analytics"
        description="Read-only collection, station, and reward health analytics from live admin APIs."
        lastRefreshedAt={lastRefreshedAt}
        isRefreshing={isRefreshing}
        onRefresh={() => void refetchAll()}
      />

      <div className="flex flex-wrap gap-2 border-b border-border pb-1">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id)}
            className={cn(
              'rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
              activeTab === tab.id
                ? 'bg-primary text-primary-foreground'
                : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'collection' ? (
        <AnalyticsSection
          title="Collection analytics"
          description="Stamps per campaign with client-side bar width relative to the highest campaign count."
          isLoading={collectionStats.isLoading || campaigns.isLoading}
          error={collectionStats.error ?? campaigns.error}
          onRetry={() => {
            void collectionStats.refetch()
            void campaigns.refetch()
          }}
        >
          <div className="space-y-4">
            <p className="text-sm">
              <span className="text-muted-foreground">Total stamps collected: </span>
              <span className="font-semibold text-foreground">
                {formatAnalyticsNumber(collectionStats.data?.totalStampsCollected)}
              </span>
            </p>
            {campaignStampRows.length > 0 ? (
              <div className="space-y-3">
                {campaignStampRows.map((row) => (
                  <HorizontalBar
                    key={row.campaignId}
                    label={resolveCampaignLabel(row.campaignId, campaignsList)}
                    value={row.stampCount}
                    max={maxCampaignStamps}
                  />
                ))}
              </div>
            ) : (
              <EmptyState title="No collection breakdown" />
            )}
            <DataTable
              columns={campaignColumns}
              data={campaignStampRows}
              getRowId={(row) => row.campaignId}
              emptyTitle="No campaign stamp counts"
            />
          </div>
        </AnalyticsSection>
      ) : null}

      {activeTab === 'stations' ? (
        <div className="space-y-6">
          <AnalyticsSection
            title="Station collector ranking"
            description="Full station stats table sorted by collector count (descending)."
            isLoading={stationStats.isLoading}
            error={stationStats.error}
            onRetry={() => void stationStats.refetch()}
          >
            {sortedStations.length > 0 ? (
              <div className="space-y-4">
                <div className="space-y-3">
                  {sortedStations.map((station) => (
                    <HorizontalBar
                      key={station.stationId}
                      label={`${station.stationName}${station.lineName ? ` · ${station.lineName}` : ''}`}
                      value={station.collectorCount}
                      max={maxCollectorCount}
                      barClassName="bg-emerald-600"
                    />
                  ))}
                </div>
                <DataTable
                  columns={stationColumns}
                  data={sortedStations}
                  getRowId={(row) => row.stationId}
                  emptyTitle="No station stats"
                />
              </div>
            ) : (
              <EmptyState title="No station statistics" />
            )}
          </AnalyticsSection>

          <div className="grid gap-6 xl:grid-cols-2">
            <AnalyticsSection title="Top 10 stations" description="Highest collector counts.">
              <DataTable
                columns={stationColumns}
                data={topStations}
                getRowId={(row) => row.stationId}
                emptyTitle="Not enough data"
              />
            </AnalyticsSection>

            <AnalyticsSection
              title="Bottom 10 stations"
              description={
                sortedStations.length > 10
                  ? 'Lowest collector counts among loaded stats.'
                  : 'Requires more than 10 stations in stats response.'
              }
            >
              {bottomStations.length > 0 ? (
                <DataTable
                  columns={stationColumns}
                  data={bottomStations}
                  getRowId={(row) => row.stationId}
                  emptyTitle="Not enough data"
                />
              ) : (
                <EmptyState
                  title="Not enough stations"
                  description="Bottom 10 is shown when at least 11 station stats are available."
                />
              )}
            </AnalyticsSection>
          </div>

          <AnalyticsSection
            title="Line grouping"
            description="Stations grouped by lineName when available (client-side)."
          >
            {stationsByLine.length > 0 ? (
              <div className="space-y-4">
                {stationsByLine.map(([lineName, lineStations]) => (
                  <div key={lineName} className="space-y-2">
                    <h4 className="text-sm font-semibold text-foreground">{lineName}</h4>
                    <ul className="space-y-1 text-sm text-muted-foreground">
                      {lineStations.map((station) => (
                        <li key={station.stationId} className="flex justify-between gap-3">
                          <span>{station.stationName}</span>
                          <span className="tabular-nums text-foreground">
                            {formatAnalyticsNumber(station.collectorCount)}
                          </span>
                        </li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState title="No line grouping available" />
            )}
          </AnalyticsSection>
        </div>
      ) : null}

      {activeTab === 'rewards' ? (
        <div className="space-y-6">
          <AnalyticsSection
            title="Reward stock overview"
            description="Stock status derived client-side when totalStock is present on the rewards list."
            isLoading={rewards.isLoading}
            error={rewards.error}
            onRetry={() => void rewards.refetch()}
          >
            <DataTable
              columns={rewardColumns}
              data={rewardStockRows}
              getRowId={(row) => row.id}
              emptyTitle="No rewards loaded"
            />
          </AnalyticsSection>

          <AnalyticsSection
            title="Voucher pool summary"
            description="Totals from voucher list pagination metadata. Status distribution reflects the current loaded page sample only."
            isLoading={vouchersCount.isLoading || vouchersSample.isLoading}
            error={vouchersCount.error ?? vouchersSample.error}
            onRetry={() => {
              void vouchersCount.refetch()
              void vouchersSample.refetch()
            }}
          >
            <div className="space-y-4">
              <p className="text-sm text-muted-foreground">
                Total vouchers (API totalElements):{' '}
                <span className="font-semibold text-foreground">
                  {vouchersCount.data?.totalElements !== undefined
                    ? formatAnalyticsNumber(vouchersCount.data.totalElements)
                    : 'Not available'}
                </span>
              </p>
              <p className="text-xs text-muted-foreground">
                Status distribution below is from the current page sample (
                {vouchersSample.data?.content.length ?? 0} of{' '}
                {vouchersSample.data?.size ?? 100} loaded, page{' '}
                {(vouchersSample.data?.page ?? 0) + 1}).
              </p>
              {voucherStatusSample.length > 0 ? (
                <ul className="space-y-2">
                  {voucherStatusSample.map(([status, count]) => (
                    <li
                      key={status}
                      className="flex items-center justify-between rounded-md border border-border px-3 py-2 text-sm"
                    >
                      <StatusBadge status={status} />
                      <span className="tabular-nums text-muted-foreground">{count}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <EmptyState title="No voucher sample loaded" />
              )}
            </div>
          </AnalyticsSection>
        </div>
      ) : null}
    </div>
  )
}
