/**
 * Shared default width presets (px) for admin DataTables.
 * Use these as starting points; pages may override per column.
 */
export const COL_WIDTH = {
  /** Short codes / IDs */
  code: { defaultWidth: 100, minWidth: 72 },
  /** UUID / long mono identifiers */
  monoId: { defaultWidth: 160, minWidth: 100 },
  /** Primary name / title */
  name: { defaultWidth: 180, minWidth: 120 },
  /** Display name, reward title, etc. */
  title: { defaultWidth: 180, minWidth: 120 },
  /** Longer descriptive text */
  description: { defaultWidth: 260, minWidth: 160 },
  /** Address / location lines */
  address: { defaultWidth: 240, minWidth: 140 },
  /** Campaign / partner / station labels */
  entity: { defaultWidth: 180, minWidth: 120 },
  /** Status / rarity / type badges (fits “Active Contract”, “Expiring Soon”, etc.) */
  badge: { defaultWidth: 152, minWidth: 112 },
  /** Narrow badge / flag columns */
  badgeSm: { defaultWidth: 112, minWidth: 88 },
  /** Dates and datetimes */
  date: { defaultWidth: 150, minWidth: 120 },
  /** Date ranges */
  dateRange: { defaultWidth: 200, minWidth: 140 },
  /** Numeric counts */
  number: { defaultWidth: 100, minWidth: 72 },
  /** Thumbnail / logo preview — fixed */
  thumbnail: {
    defaultWidth: 72,
    minWidth: 64,
    maxWidth: 96,
    resizable: false as const,
  },
  /** Color swatch + hex */
  color: { defaultWidth: 120, minWidth: 96 },
  /** Email */
  email: { defaultWidth: 200, minWidth: 140 },
  /** Compact metric (share %, priority) */
  metric: { defaultWidth: 110, minWidth: 80 },
} as const

/** Default actions column width when not overridden (≈ 2–3 icon buttons). */
export const DEFAULT_ACTIONS_WIDTH = 128

/** Icon view/edit + text Activate/Deactivate action cluster. */
export const ACTIONS_WIDTH_WITH_LABEL = 220

export type ColumnWidthPreset = {
  defaultWidth: number
  minWidth?: number
  maxWidth?: number
  resizable?: boolean
}
