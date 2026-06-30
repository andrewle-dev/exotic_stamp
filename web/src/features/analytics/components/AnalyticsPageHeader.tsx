import { RefreshCw } from 'lucide-react'
import { Button } from '../../../components/ui/button'

type AnalyticsPageHeaderProps = {
  title: string
  description?: string
  lastRefreshedAt?: Date | string | number | null
  isRefreshing?: boolean
  onRefresh?: () => void
}

const refreshTimestampFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function parseRefreshDate(
  value: AnalyticsPageHeaderProps['lastRefreshedAt'],
): Date | null {
  if (value === null || value === undefined) {
    return null
  }

  const date = value instanceof Date ? value : new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

export function AnalyticsPageHeader({
  title,
  description,
  lastRefreshedAt,
  isRefreshing = false,
  onRefresh,
}: AnalyticsPageHeaderProps) {
  const hasRefreshValue = lastRefreshedAt !== null && lastRefreshedAt !== undefined
  const parsedLastRefreshedAt = parseRefreshDate(lastRefreshedAt)
  const lastRefreshedLabel = !hasRefreshValue
    ? 'Not refreshed yet'
    : parsedLastRefreshedAt
      ? `Last refreshed at ${refreshTimestampFormatter.format(parsedLastRefreshedAt)}`
      : 'Refresh time unavailable'
  const statusDotClassName = parsedLastRefreshedAt ? 'bg-emerald-500' : 'bg-muted-foreground/50'
  const hasDescription = description?.trim()

  return (
    <header className="flex flex-col gap-3 px-0 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0">
        <h1 className="text-2xl font-semibold tracking-tight text-foreground">
          {title}
        </h1>

        {hasDescription ? (
          <p className="mt-1 max-w-3xl text-sm text-muted-foreground">
            {description}
          </p>
        ) : null}
      </div>

      <div className="flex shrink-0 flex-wrap items-center gap-3 sm:justify-end">
        <div className="flex min-w-0 items-center gap-2 text-sm text-muted-foreground">
          <span
            className={`h-2 w-2 rounded-full ${statusDotClassName}`}
            aria-hidden="true"
          />
          <span className="min-w-0 leading-none sm:truncate">{lastRefreshedLabel}</span>
        </div>

        {onRefresh ? (
          <Button
            type="button"
            variant="outline"
            onClick={onRefresh}
            disabled={isRefreshing}
            className="h-9 gap-2 px-3"
          >
            <RefreshCw
              className={isRefreshing ? 'h-4 w-4 animate-spin' : 'h-4 w-4'}
              aria-hidden="true"
            />
            Refresh
          </Button>
        ) : null}
      </div>
    </header>
  )
}
