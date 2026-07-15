import type { SelectHTMLAttributes } from 'react'
import { Select } from '../ui/FormField'
import { cn } from '../../lib/utils/cn'

type FilterSelectProps = SelectHTMLAttributes<HTMLSelectElement>

/** Consistent select control for filter groups. */
export function FilterSelect({ className, ...props }: FilterSelectProps) {
  return <Select className={cn('bg-card', className)} {...props} />
}
