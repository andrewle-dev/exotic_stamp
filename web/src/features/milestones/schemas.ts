import { z } from 'zod'

const milestoneRewardTypeSchema = z.enum(['VOUCHER', 'DIGITAL_STICKER', 'BONUS_STAMP'])
const milestoneStatusSchema = z.enum(['DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'])

export const milestoneFormSchema = z
  .object({
    campaignId: z.string().uuid('Campaign is required'),
    code: z.string().trim().min(1, 'Code is required'),
    requiredStampCount: z.string().min(1, 'Required stamp count is required'),
    name: z.string().trim().min(1, 'Name is required'),
    description: z.string().optional(),
    rewardType: milestoneRewardTypeSchema,
    rewardTitle: z.string().trim().min(1, 'Reward title is required'),
    rewardDescription: z.string().optional(),
    rewardImageUrl: z.string().optional(),
    status: milestoneStatusSchema.optional(),
    sortOrder: z.string().optional(),
  })
  .refine(
    (data) => {
      const num = Number(data.requiredStampCount)
      return Number.isInteger(num) && num >= 1
    },
    { message: 'Required stamp count must be at least 1', path: ['requiredStampCount'] },
  )
  .refine(
    (data) => {
      if (!data.sortOrder?.trim()) {
        return true
      }
      const num = Number(data.sortOrder)
      return Number.isInteger(num) && num >= 0
    },
    { message: 'Sort order must be an integer of 0 or greater', path: ['sortOrder'] },
  )

export type MilestoneFormValues = z.infer<typeof milestoneFormSchema>

export function parseFormSortOrder(value: string | undefined): number | undefined {
  const trimmed = value?.trim()
  if (!trimmed) {
    return undefined
  }
  return Number(trimmed)
}

export const defaultMilestoneFormValues: MilestoneFormValues = {
  campaignId: '',
  code: '',
  requiredStampCount: '1',
  name: '',
  description: '',
  rewardType: 'VOUCHER',
  rewardTitle: '',
  rewardDescription: '',
  rewardImageUrl: '',
  status: 'DRAFT',
  sortOrder: '',
}
