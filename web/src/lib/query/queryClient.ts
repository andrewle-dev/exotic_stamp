import { QueryClient } from '@tanstack/react-query'
import { isApiError } from '../api/errors'

export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        retry: (failureCount, error) => {
          const status = isApiError(error) ? error.status : (error as { status?: number }).status
          if (status === 401 || status === 403 || status === 404 || status === 422) {
            return false
          }
          if (status === 429) {
            return failureCount < 1
          }
          if (status === 503) {
            return failureCount < 2
          }
          return failureCount < 2
        },
      },
      mutations: {
        retry: false,
      },
    },
  })
}

/** Singleton used by AppProviders (kept out of component modules for Fast Refresh). */
export const appQueryClient = createQueryClient()

