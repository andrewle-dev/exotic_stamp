import { useMemo } from 'react'
import { useQueries } from '@tanstack/react-query'
import { listCampaignStations } from '../../../lib/api/campaigns.api'
import { campaignKeys } from '../../../lib/query/keys/campaigns'
import { useCampaigns } from '../../campaigns/hooks'
import { usePartners } from '../../partners/hooks'
import { useRewards } from '../../rewards/hooks'
import { useStampDesigns } from '../../stamp-designs/hooks'
import { useStationsList } from '../../stations/hooks'
import { useVouchers } from '../../vouchers/hooks'
import { useCollectionStats, useStationStats } from '../hooks'
import { deriveOperationalWarnings } from '../utils/operational-warnings'
import type { CampaignStationResponse } from '../../../types/campaigns'

const LIST_PAGE = { page: 0, size: 500 } as const
const COUNT_PAGE = { page: 0, size: 1 } as const
const VOUCHER_SAMPLE_PAGE = { page: 0, size: 100 } as const

export function useOperationalOverviewData() {
  const collectionStats = useCollectionStats()
  const stationStats = useStationStats()
  const campaigns = useCampaigns(LIST_PAGE)
  const stations = useStationsList(LIST_PAGE)
  const rewards = useRewards(LIST_PAGE)
  const partners = usePartners(LIST_PAGE)
  const stampDesigns = useStampDesigns(LIST_PAGE)
  const vouchersCount = useVouchers(COUNT_PAGE)
  const vouchersSample = useVouchers(VOUCHER_SAMPLE_PAGE)

  const activeCampaigns = useMemo(
    () => campaigns.data?.content.filter((campaign) => campaign.status === 'ACTIVE') ?? [],
    [campaigns.data?.content],
  )

  const campaignStationQueries = useQueries({
    queries: activeCampaigns.map((campaign) => ({
      queryKey: campaignKeys.stations(campaign.id),
      queryFn: () => listCampaignStations(campaign.id),
      enabled: Boolean(campaigns.data),
    })),
  })

  const campaignStationsByCampaignId = useMemo(() => {
    const map = new Map<string, CampaignStationResponse[]>()
    activeCampaigns.forEach((campaign, index) => {
      const query = campaignStationQueries[index]
      if (query?.data) {
        map.set(campaign.id, query.data)
      }
    })
    return map
  }, [activeCampaigns, campaignStationQueries])

  const operationalWarnings = useMemo(
    () =>
      deriveOperationalWarnings({
        campaigns: campaigns.data?.content ?? [],
        campaignStationsByCampaignId,
        stations: stations.data?.content ?? [],
        stampDesigns: stampDesigns.data?.content ?? [],
        rewards: rewards.data?.content ?? [],
        vouchers: vouchersSample.data?.content ?? [],
        partners: partners.data?.content ?? [],
      }),
    [
      campaignStationsByCampaignId,
      campaigns.data?.content,
      partners.data?.content,
      rewards.data?.content,
      stampDesigns.data?.content,
      stations.data?.content,
      vouchersSample.data?.content,
    ],
  )

  const lastRefreshedAt = useMemo(() => {
    const timestamps = [
      collectionStats.dataUpdatedAt,
      stationStats.dataUpdatedAt,
      campaigns.dataUpdatedAt,
      stations.dataUpdatedAt,
      rewards.dataUpdatedAt,
      partners.dataUpdatedAt,
      stampDesigns.dataUpdatedAt,
      vouchersCount.dataUpdatedAt,
      vouchersSample.dataUpdatedAt,
      ...campaignStationQueries.map((query) => query.dataUpdatedAt),
    ].filter((value) => value > 0)

    return timestamps.length > 0 ? Math.max(...timestamps) : 0
  }, [
    campaignStationQueries,
    campaigns.dataUpdatedAt,
    collectionStats.dataUpdatedAt,
    partners.dataUpdatedAt,
    rewards.dataUpdatedAt,
    stampDesigns.dataUpdatedAt,
    stationStats.dataUpdatedAt,
    stations.dataUpdatedAt,
    vouchersCount.dataUpdatedAt,
    vouchersSample.dataUpdatedAt,
  ])

  const refetchAll = async () => {
    await Promise.all([
      collectionStats.refetch(),
      stationStats.refetch(),
      campaigns.refetch(),
      stations.refetch(),
      rewards.refetch(),
      partners.refetch(),
      stampDesigns.refetch(),
      vouchersCount.refetch(),
      vouchersSample.refetch(),
      ...campaignStationQueries.map((query) => query.refetch()),
    ])
  }

  const isRefreshing =
    collectionStats.isFetching ||
    stationStats.isFetching ||
    campaigns.isFetching ||
    stations.isFetching ||
    rewards.isFetching ||
    partners.isFetching ||
    stampDesigns.isFetching ||
    vouchersCount.isFetching ||
    vouchersSample.isFetching ||
    campaignStationQueries.some((query) => query.isFetching)

  return {
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
  }
}
