import type { ReactNode } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/Card'
import { ErrorState } from '../../../components/ui/ErrorState'
import { LoadingSkeleton } from '../../../components/ui/LoadingSkeleton'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { Button } from '../../../components/ui/Button'
import { isForbiddenError } from '../../../lib/api/errors'

interface AnalyticsSectionProps {
  title: string
  description?: string
  isLoading?: boolean
  error?: unknown
  onRetry?: () => void
  children: ReactNode
}

export function AnalyticsSection({
  title,
  description,
  isLoading,
  error,
  onRetry,
  children,
}: AnalyticsSectionProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        {description ? <p className="mt-1 text-sm text-muted-foreground">{description}</p> : null}
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="space-y-3" aria-busy="true">
            <LoadingSkeleton className="h-4 w-full" />
            <LoadingSkeleton className="h-4 w-5/6" />
            <LoadingSkeleton className="h-4 w-2/3" />
          </div>
        ) : error ? (
          isForbiddenError(error) ? (
            <PermissionDeniedState
              title="Access denied"
              description="You do not have permission to view this section."
            />
          ) : (
            <ErrorState
              title="Failed to load section"
              error={error}
              action={
                onRetry ? (
                  <Button variant="secondary" size="sm" onClick={() => void onRetry()}>
                    Retry
                  </Button>
                ) : undefined
              }
            />
          )
        ) : (
          children
        )}
      </CardContent>
    </Card>
  )
}
