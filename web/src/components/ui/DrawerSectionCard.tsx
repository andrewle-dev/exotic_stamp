import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { cn } from '../../lib/utils/cn'

interface DrawerSectionCardProps {
  icon: LucideIcon
  title: string
  description?: string
  children: ReactNode
  className?: string
}

export function DrawerSectionCard({
  icon: Icon,
  title,
  description,
  children,
  className,
}: DrawerSectionCardProps) {
  return (
    <section
      className={cn(
        'overflow-hidden rounded-xl border border-border bg-card shadow-[0_1px_2px_rgba(29,36,51,0.04)]',
        className,
      )}
    >
      <div className="flex items-start gap-3 border-b border-border/70 bg-secondary/40 px-4 py-3.5">
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
          <Icon className="h-4 w-4" aria-hidden="true" />
        </div>
        <div className="min-w-0 pt-0.5">
          <h3 className="text-sm font-semibold text-foreground">{title}</h3>
          {description ? (
            <p className="mt-0.5 text-xs leading-relaxed text-muted-foreground">{description}</p>
          ) : null}
        </div>
      </div>
      <div className="space-y-4 px-4 py-4">{children}</div>
    </section>
  )
}
