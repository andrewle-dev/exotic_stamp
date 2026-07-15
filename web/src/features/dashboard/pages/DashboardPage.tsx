import { useMemo } from 'react'
import {
  Gift,
  Handshake,
  MapPin,
  Megaphone,
  Stamp,
  Ticket,
} from 'lucide-react'
import { DataTable, type DataTableColumn } from '../../../components/ui/DataTable'
import { COL_WIDTH } from '../../../components/ui/table/columnWidthPresets'
import { EmptyState } from '../../../components/ui/EmptyState'
import { isForbiddenError } from '../../../lib/api/errors'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import type { CampaignStampCountView } from '../../../types/analytics'
import { AnalyticsPageHeader } from '../../analytics/components/AnalyticsPageHeader'
import { AnalyticsSection } from '../../analytics/components/AnalyticsSection'
import { HorizontalBar } from '../../analytics/components/HorizontalBar'
import { MetricCard } from '../../analytics/components/MetricCard'
import { OperationalWarningsList } from '../../analytics/components/OperationalWarningsList'
import { useOperationalOverviewData } from '../../analytics/hooks/useOperationalOverviewData'
import {
  formatAnalyticsNumber,
  resolveCampaignLabel,
  sortStationsByCollectors,
} from '../../analytics/utils/helpers'

function formatCount(value: number | undefined, unavailable = 'Not available'): string {
  if (value === undefined) {
    return unavailable
  }

  return formatAnalyticsNumber(value)
}

export function DashboardPage() {
  const {
    collectionStats,
    stationStats,
    campaigns,
    stations,
    rewards,
    partners,
    stampDesigns,
    vouchersCount,
    vouchersSample,
    operationalWarnings,
    lastRefreshedAt,
    refetchAll,
    isRefreshing,
  } = useOperationalOverviewData()

  const campaignsList = useMemo(() => campaigns.data?.content ?? [], [campaigns.data?.content])
  const sortedStationStats = useMemo(
    () => sortStationsByCollectors(stationStats.data ?? []),
    [stationStats.data],
  )
  const topStations = sortedStationStats.slice(0, 10)
  const campaignStampRows = useMemo(
    () => collectionStats.data?.stampsPerCampaign ?? [],
    [collectionStats.data?.stampsPerCampaign],
  )
  const maxCampaignStamps = useMemo(() => {
    const counts = campaignStampRows.map((row: CampaignStampCountView) => row.stampCount)
    return counts.length > 0 ? Math.max(...counts) : 0
  }, [campaignStampRows])

  const campaignColumns: DataTableColumn<CampaignStampCountView>[] = useMemo(
    () => [
      {
        id: 'campaign',
        header: 'Campaign',
        ...COL_WIDTH.entity,
        defaultWidth: 240,
        cell: (row) => resolveCampaignLabel(row.campaignId, campaignsList),
      },
      {
        id: 'stamps',
        header: 'Stamps collected',
        align: 'right',
        ...COL_WIDTH.number,
        defaultWidth: 140,
        cell: (row) => formatAnalyticsNumber(row.stampCount),
      },
    ],
    [campaignsList],
  )

  const stationColumns = useMemo(
    () => [
      {
        id: 'station',
        header: 'Station',
        ...COL_WIDTH.entity,
        defaultWidth: 200,
        cell: (row: (typeof topStations)[number]) => row.stationName,
      },
      {
        id: 'line',
        header: 'Line',
        ...COL_WIDTH.entity,
        defaultWidth: 140,
        cell: (row: (typeof topStations)[number]) => row.lineName ?? '—',
      },
      {
        id: 'collectors',
        header: 'Collectors',
        align: 'right' as const,
        ...COL_WIDTH.number,
        cell: (row: (typeof topStations)[number]) => formatAnalyticsNumber(row.collectorCount),
      },
    ],
    [],
  )

  const pageForbidden =
    (collectionStats.error && isForbiddenError(collectionStats.error)) ||
    (stationStats.error && isForbiddenError(stationStats.error)) ||
    (campaigns.error && isForbiddenError(campaigns.error))

  if (pageForbidden) {
    return <PermissionDeniedState title="Dashboard access denied" />
  }

  const voucherSampleNote =
    vouchersSample.data && vouchersSample.data.content.length > 0
      ? 'Voucher warnings are based on the first 100 vouchers loaded (current page sample).'
      : undefined

  return (
    <div className="space-y-6">
      <AnalyticsPageHeader
        title="Dashboard"
        lastRefreshedAt={lastRefreshedAt}
        isRefreshing={isRefreshing}
        onRefresh={() => void refetchAll()}
      />

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
        <MetricCard
          label="Total stamps collected"
          value={
            collectionStats.isLoading
              ? '…'
              : collectionStats.error
                ? 'Not available'
                : formatAnalyticsNumber(collectionStats.data?.totalStampsCollected)
          }
          icon={Stamp}
          accent="success"
        />
        <MetricCard
          label="Total campaigns"
          value={formatCount(campaigns.data?.totalElements)}
          hint={campaigns.error ? 'Could not load campaigns' : undefined}
          icon={Megaphone}
        />
        <MetricCard
          label="Total stations"
          value={formatCount(stations.data?.totalElements)}
          hint={stations.error ? 'Could not load stations' : undefined}
          icon={MapPin}
        />
        <MetricCard
          label="Total rewards"
          value={formatCount(rewards.data?.totalElements)}
          hint={rewards.error ? 'Could not load rewards' : undefined}
          icon={Gift}
        />
        <MetricCard
          label="Total vouchers"
          value={formatCount(vouchersCount.data?.totalElements)}
          hint={vouchersCount.error ? 'Could not load vouchers' : 'From voucher pool totalElements'}
          icon={Ticket}
        />
        <MetricCard
          label="Total partners"
          value={formatCount(partners.data?.totalElements)}
          hint={partners.error ? 'Could not load partners' : undefined}
          icon={Handshake}
        />
      </section>

      <div className="grid gap-6 xl:grid-cols-2">
        <AnalyticsSection
          title="Collection overview"
          description="Aggregate stamp collection from admin collection stats."
          isLoading={collectionStats.isLoading}
          error={collectionStats.error}
          onRetry={() => void collectionStats.refetch()}
        >
          <div className="space-y-4">
            <p className="text-sm text-muted-foreground">
              Total stamps collected:{' '}
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
              <EmptyState
                title="No campaign stamp data"
                description="Collection stats returned no per-campaign breakdown."
              />
            )}
            <DataTable
              tableId="dashboard-campaign-stamps"
              columns={campaignColumns}
              data={campaignStampRows}
              getRowId={(row) => row.campaignId}
              emptyTitle="No campaign stamp counts"
            />
          </div>
        </AnalyticsSection>

        <AnalyticsSection
          title="Station performance"
          description="Top stations by collector count (client-side sort, descending)."
          isLoading={stationStats.isLoading}
          error={stationStats.error}
          onRetry={() => void stationStats.refetch()}
        >
          {topStations.length > 0 ? (
            <DataTable
              tableId="dashboard-top-stations"
              columns={stationColumns}
              data={topStations}
              getRowId={(row) => row.stationId}
              emptyTitle="No station stats"
            />
          ) : (
            <EmptyState
              title="No station statistics"
              description="Station stats endpoint returned an empty list."
            />
          )}
        </AnalyticsSection>
      </div>

      <AnalyticsSection
        title="Operational warnings"
        description="Derived from loaded list endpoints only. No fabricated alerts."
        isLoading={
          campaigns.isLoading ||
          stations.isLoading ||
          rewards.isLoading ||
          partners.isLoading ||
          stampDesigns.isLoading ||
          vouchersSample.isLoading
        }
      >
        <OperationalWarningsList
          warnings={operationalWarnings}
          voucherSampleNote={voucherSampleNote}
        />
      </AnalyticsSection>
    </div>
  )
}
