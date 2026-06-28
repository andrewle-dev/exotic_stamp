/** Mask sensitive scan keys and voucher codes for table/list display. */
export function maskSecret(value: string | null | undefined, visibleTail = 4): string {
  if (!value) {
    return '—'
  }

  if (value.length <= visibleTail) {
    return '••••'
  }

  return `${'•'.repeat(Math.min(8, value.length - visibleTail))}${value.slice(-visibleTail)}`
}

export function maskVoucherCode(code: string | null | undefined): string {
  if (!code) {
    return '—'
  }

  const parts = code.split('-')
  if (parts.length > 1) {
    return `${parts[0]}-••••-••••`
  }

  return maskSecret(code, 4)
}
