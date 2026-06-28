const SENSITIVE_PATTERNS = ['MANAGE', 'ADMIN', 'DELETE', 'RBAC', 'SCAN_KEY', 'VOUCHER'] as const

export function isSensitivePermissionCode(permissionCode: string): boolean {
  const upper = permissionCode.trim().toUpperCase()
  return SENSITIVE_PATTERNS.some((pattern) => upper.includes(pattern))
}
