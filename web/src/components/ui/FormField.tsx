import { cn } from '../../lib/utils/cn'
import type { InputHTMLAttributes, ReactNode } from 'react'
import { InlineFieldError } from './InlineFieldError'

interface FormFieldProps {
  label: string
  htmlFor: string
  error?: string
  required?: boolean
  hint?: string
  children: ReactNode
  className?: string
}

export function FormField({
  label,
  htmlFor,
  error,
  required,
  hint,
  children,
  className,
}: FormFieldProps) {
  return (
    <div className={cn('space-y-1.5', className)}>
      <label htmlFor={htmlFor} className="flex items-center gap-1 text-sm font-medium text-foreground">
        {label}
        {required ? <span className="text-destructive">*</span> : null}
      </label>
      {children}
      {hint && !error ? <p className="text-xs text-muted-foreground">{hint}</p> : null}
      <InlineFieldError id={`${htmlFor}-error`} message={error} />
    </div>
  )
}

export function Input({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={cn(
        'w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm text-foreground outline-none transition-colors placeholder:text-muted-foreground focus:border-primary focus:ring-2 focus:ring-ring/20',
        className,
      )}
      {...props}
    />
  )
}
