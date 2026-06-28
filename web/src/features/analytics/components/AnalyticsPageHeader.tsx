import { RefreshCw } from 'lucide-react'
import { Button } from '../../../components/ui/Button'
import { formatDateTime } from '../../../lib/formatting/date'

interface AnalyticsPageHeaderProps {
  title: string
  description: string
  lastRefreshedAt: number
  isRefreshing: boolean
  onRefresh: () => void
}

export function AnalyticsPageHeader({
  title,
  description,
  lastRefreshedAt,
  isRefreshing,
  onRefresh,
}: AnalyticsPageHeaderProps) {
  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        <p className="text-sm text-muted-foreground">{description}</p>
        {lastRefreshedAt > 0 ? (
          <p className="mt-1 text-xs text-muted-foreground">
            Last refreshed at {formatDateTime(new Date(lastRefreshedAt).toISOString())}
          </p>
        ) : null}
      </div>
      <Button
        variant="secondary"
        size="sm"
        onClick={() => void onRefresh()}
        disabled={isRefreshing}
        className="shrink-0"
      >
        <RefreshCw className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
        Refresh
      </Button>
    </div>
  )
}
