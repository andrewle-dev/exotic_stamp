import { QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { AuthProvider } from '../features/auth/AuthProvider'
import { appQueryClient } from '../lib/query/queryClient'

export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider client={appQueryClient}>
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  )
}
