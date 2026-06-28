import { z } from 'zod'

const stampRaritySchema = z.enum(['COMMON', 'RARE', 'EPIC', 'LEGENDARY'])
const stampDesignStatusSchema = z.enum(['DRAFT', 'ACTIVE', 'INACTIVE'])

export const stampDesignFormSchema = z
  .object({
    campaignId: z.string().uuid('Campaign is required'),
    stationId: z.string().uuid('Station is required'),
    name: z.string().trim().min(1, 'Name is required'),
    description: z.string().optional(),
    imageUrl: z.string().trim().min(1, 'Image URL is required'),
    previewImageUrl: z.string().optional(),
    rarity: stampRaritySchema.optional(),
    status: stampDesignStatusSchema.optional(),
    sortOrder: z.string().optional(),
  })
  .refine(
    (data) => {
      if (!data.sortOrder?.trim()) {
        return true
      }
      const num = Number(data.sortOrder)
      return Number.isInteger(num)
    },
    { message: 'Sort order must be an integer', path: ['sortOrder'] },
  )

export type StampDesignFormValues = z.infer<typeof stampDesignFormSchema>

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
  sortOrder: '',
}
