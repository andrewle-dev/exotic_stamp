const STORAGE_PREFIX = 'exotic-stamp-table-widths:v2:'

export function tableWidthsStorageKey(tableId: string): string {
  return `${STORAGE_PREFIX}${tableId}`
}

/** Read persisted column widths. Returns null on missing/invalid data or unavailable storage. */
export function loadTableColumnWidths(tableId: string): Record<string, number> | null {
  try {
    const raw = localStorage.getItem(tableWidthsStorageKey(tableId))
    if (!raw) return null
    const parsed: unknown = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return null
    const result: Record<string, number> = {}
    for (const [key, value] of Object.entries(parsed as Record<string, unknown>)) {
      if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
        result[key] = value
      }
    }
    return Object.keys(result).length > 0 ? result : null
  } catch {
    return null
  }
}

/** Persist column widths. No-ops safely when storage is unavailable. */
export function saveTableColumnWidths(tableId: string, widths: Record<string, number>): void {
  try {
    localStorage.setItem(tableWidthsStorageKey(tableId), JSON.stringify(widths))
  } catch {
    // Quota exceeded, private mode, etc. — fail silently.
  }
}

/** Clear persisted widths for a table so defaults apply on next load. */
export function resetTableColumnWidths(tableId: string): void {
  try {
    localStorage.removeItem(tableWidthsStorageKey(tableId))
  } catch {
    // ignore
  }
}
