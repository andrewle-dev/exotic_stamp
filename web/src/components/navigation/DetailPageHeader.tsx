import type { ReactNode } from 'react'
import { ArrowLeft } from 'lucide-react'
import { Button } from '../ui/Button'
import { useSafeBackNavigation } from '../../lib/navigation/useSafeBackNavigation'

export interface DetailPageHeaderProps {
  backLabel: string
  backTo: string
  title: ReactNode
  subtitle?: ReactNode
  badges?: ReactNode
  actions?: ReactNode
}

export function DetailPageHeader({
  backLabel,
  backTo,
  title,
  subtitle,
  badges,
  actions,
}: DetailPageHeaderProps) {
  const goBack = useSafeBackNavigation(backTo)

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" onClick={goBack} className="-ml-2">
        <ArrowLeft className="h-4 w-4" />
        {backLabel}
      </Button>

      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
            {badges}
          </div>
          {subtitle ? (
            <p className="font-mono text-sm text-muted-foreground">{subtitle}</p>
          ) : null}
        </div>

        {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
      </div>
    </div>
  )
}
