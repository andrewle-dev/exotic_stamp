export function formatNumber(
  value: number | null | undefined,
  options?: Intl.NumberFormatOptions,
): string {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '—'
  }

  return new Intl.NumberFormat(undefined, options).format(value)
}

export function formatCompactNumber(value: number | null | undefined): string {
  return formatNumber(value, { notation: 'compact', maximumFractionDigits: 1 })
}
