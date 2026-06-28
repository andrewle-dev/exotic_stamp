import type { MapStringInteger } from '../../../types/vouchers'

const KNOWN_IMPORT_RESULT_KEYS: Record<string, string> = {
  inserted: 'Inserted',
  imported: 'Imported',
  duplicate: 'Duplicates',
  duplicates: 'Duplicates',
  invalid: 'Invalid / rejected',
  rejected: 'Invalid / rejected',
}

export function formatImportResultRows(result: MapStringInteger): { label: string; count: number }[] {
  const rows: { label: string; count: number }[] = []
  const usedKeys = new Set<string>()

  for (const [key, label] of Object.entries(KNOWN_IMPORT_RESULT_KEYS)) {
    const matchKey = Object.keys(result).find((k) => k.toLowerCase() === key)
    if (matchKey !== undefined) {
      rows.push({ label, count: result[matchKey] })
      usedKeys.add(matchKey)
    }
  }

  for (const [key, count] of Object.entries(result)) {
    if (!usedKeys.has(key)) {
      rows.push({
        label: key
          .replace(/_/g, ' ')
          .replace(/\b\w/g, (char) => char.toUpperCase()),
        count,
      })
    }
  }

  return rows
}
