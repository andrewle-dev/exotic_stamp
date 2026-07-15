import { z } from 'zod'

const stampRaritySchema = z.enum(['COMMON', 'RARE', 'EPIC', 'LEGENDARY'])
const stampDesignStatusSchema = z.enum(['DRAFT', 'ACTIVE', 'INACTIVE'])

export const stampDesignFormSchema = z.object({
  campaignId: z.string().uuid('Campaign is required'),
  stationId: z.string().uuid('Station is required'),
  name: z.string().trim().min(1, 'Name is required'),
  description: z.string().optional(),
  imageUrl: z.string().trim().min(1, 'Main stamp artwork is required'),
  previewImageUrl: z.string().optional(),
  rarity: stampRaritySchema.optional(),
  status: stampDesignStatusSchema.optional(),
})

export type StampDesignFormValues = z.infer<typeof stampDesignFormSchema>

/** @deprecated Sort order is no longer edited in the form UI; kept for any residual callers. */
export function parseFormSortOrder(value: string | undefined): number | undefined {
  const trimmed = value?.trim()
  if (!trimmed) {
    return undefined
  }
  return Number(trimmed)
}

export const defaultStampDesignFormValues: StampDesignFormValues = {
  campaignId: '',
  stationId: '',
  name: '',
  description: '',
  imageUrl: '',
  previewImageUrl: '',
  rarity: 'COMMON',
  status: 'DRAFT',
}
