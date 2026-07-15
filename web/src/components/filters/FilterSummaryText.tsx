import { cn } from '../../lib/utils/cn'

interface FilterSummaryTextProps {
  text: string
  className?: string
}

export function FilterSummaryText({ text, className }: FilterSummaryTextProps) {
  return <p className={cn('text-sm text-muted-foreground', className)}>{text}</p>
}
