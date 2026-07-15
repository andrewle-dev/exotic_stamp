import type { LucideIcon } from 'lucide-react'
import {
  Building2,
  Calendar,
  CircleDot,
  Flag,
  Layers,
  MapPin,
  Sparkles,
  Tag,
} from 'lucide-react'

export type FilterAccent =
  | 'campaign'
  | 'station'
  | 'rarity'
  | 'status'
  | 'default'
  | 'line'
  | 'type'
  | 'reward'
  | 'milestone'
  | 'partner'
  | 'date'
  | 'search'

export interface FilterThemeStyles {
  badge: string
  label: string
  /** @deprecated Active filter tags use UNIFIED_FILTER_TAG_STYLE instead. */
  chip: string
  icon: LucideIcon
}

/**
 * Single blue system for applied-filter tags (primary #01599D).
 * Category accents are reserved for FilterGroup labels inside the panel.
 */
export const UNIFIED_FILTER_TAG_STYLE =
  'border-primary/20 bg-primary/10 text-primary'

export const FILTER_THEMES: Record<FilterAccent, FilterThemeStyles> = {
  campaign: {
    badge: 'bg-sky-50 text-sky-700',
    label: 'text-sky-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Flag,
  },
  station: {
    badge: 'bg-teal-50 text-teal-700',
    label: 'text-teal-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: MapPin,
  },
  rarity: {
    badge: 'bg-violet-50 text-violet-700',
    label: 'text-violet-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Sparkles,
  },
  status: {
    badge: 'bg-emerald-50 text-emerald-700',
    label: 'text-emerald-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: CircleDot,
  },
  line: {
    badge: 'bg-teal-50 text-teal-700',
    label: 'text-teal-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: MapPin,
  },
  type: {
    badge: 'bg-sky-50 text-sky-700',
    label: 'text-sky-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Layers,
  },
  reward: {
    badge: 'bg-fuchsia-50 text-fuchsia-700',
    label: 'text-fuchsia-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Tag,
  },
  milestone: {
    badge: 'bg-sky-50 text-sky-700',
    label: 'text-sky-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Flag,
  },
  partner: {
    badge: 'bg-indigo-50 text-indigo-700',
    label: 'text-indigo-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Building2,
  },
  date: {
    badge: 'bg-amber-50 text-amber-700',
    label: 'text-amber-700',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Calendar,
  },
  search: {
    badge: 'bg-slate-100 text-slate-600',
    label: 'text-slate-600',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Tag,
  },
  default: {
    badge: 'bg-slate-100 text-slate-600',
    label: 'text-slate-600',
    chip: UNIFIED_FILTER_TAG_STYLE,
    icon: Tag,
  },
}
