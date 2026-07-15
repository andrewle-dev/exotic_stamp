import type { QueryClient, QueryKey } from '@tanstack/react-query'

/**
 * Awaited invalidation so mutateAsync resolves only after active queries
 * have been marked stale and refetch has been kicked off / completed.
 * Prefer this over fire-and-forget `void invalidateQueries(...)`.
 */
export async function invalidateKeys(
  queryClient: QueryClient,
  keys: readonly QueryKey[],
): Promise<void> {
  await Promise.all(keys.map((queryKey) => queryClient.invalidateQueries({ queryKey })))
}
