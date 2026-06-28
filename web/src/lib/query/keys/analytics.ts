export const analyticsKeys = {
  all: ['analytics'] as const,
  collectionStats: () => [...analyticsKeys.all, 'collection-stats'] as const,
  stationStats: () => [...analyticsKeys.all, 'station-stats'] as const,
}
