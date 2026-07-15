import { cn } from '../../lib/utils/cn'
import type {
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from 'react'
import { FieldHelpTooltip } from './InfoTooltip'
import { InlineFieldError } from './InlineFieldError'
import { controlClass } from './control-styles'

interface FormFieldProps {
  label: string
  htmlFor: string
  error?: string
  required?: boolean
  /** Short always-visible helper. Prefer `help` for detailed rules. */
  hint?: string
  /** Detailed guidance shown via info tooltip next to the label. */
  help?: string
  children: ReactNode
  className?: string
}

export function FormField({
  label,
  htmlFor,
  error,
  required,
  hint,
  help,
  children,
  className,
}: FormFieldProps) {
  return (
    <div className={cn('space-y-1.5', className)}>
      <label htmlFor={htmlFor} className="flex items-center gap-1.5 text-sm font-medium text-foreground">
        <span className="inline-flex items-center gap-1">
          {label}
          {required ? <span className="text-destructive">*</span> : null}
        </span>
        {help ? <FieldHelpTooltip content={help} label={`Help for ${label}`} /> : null}
      </label>
      {children}
      {hint && !error ? <p className="text-xs leading-relaxed text-muted-foreground">{hint}</p> : null}
      <InlineFieldError id={`${htmlFor}-error`} message={error} />
    </div>
  )
}

export function Input({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return <input className={controlClass(className)} {...props} />
}

export function Select({ className, ...props }: SelectHTMLAttributes<HTMLSelectElement>) {
  return <select className={controlClass(className)} {...props} />
}

export function Textarea({ className, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea className={controlClass(className)} {...props} />
}
