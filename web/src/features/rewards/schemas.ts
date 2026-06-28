import { z } from 'zod'

const rewardTypeSchema = z.enum(['VOUCHER', 'DIGITAL_STICKER', 'BONUS_STAMP'])

export const rewardFormSchema = z
  .object({
    milestoneId: z.string().uuid('Milestone is required'),
    partnerId: z.string().optional(),
    rewardType: rewardTypeSchema,
    name: z.string().trim().min(1, 'Name is required').max(100, 'Name must be at most 100 characters'),
    description: z
      .string()
      .max(255, 'Description must be at most 255 characters')
      .optional(),
    valueAmount: z.string().optional(),
    expiryDays: z.string().optional(),
    totalStock: z.string().optional(),
  })
  .refine(
    (data) => {
      if (!data.valueAmount?.trim()) {
        return true
      }
      const num = Number(data.valueAmount)
      return !Number.isNaN(num) && num >= 0
    },
    { message: 'Value amount must be a number of 0 or greater', path: ['valueAmount'] },
  )
  .refine(
    (data) => {
      if (!data.expiryDays?.trim()) {
        return true
      }
      const num = Number(data.expiryDays)
      return Number.isInteger(num) && num >= 0
    },
    { message: 'Expiry days must be an integer of 0 or greater', path: ['expiryDays'] },
  )
  .refine(
    (data) => {
      if (!data.totalStock?.trim()) {
        return true
      }
      const num = Number(data.totalStock)
      return Number.isInteger(num) && num >= 0
    },
    { message: 'Total stock must be an integer of 0 or greater', path: ['totalStock'] },
  )

export type RewardFormValues = z.infer<typeof rewardFormSchema>

export const defaultRewardFormValues: RewardFormValues = {
  milestoneId: '',
  partnerId: '',
  rewardType: 'VOUCHER',
  name: '',
  description: '',
  valueAmount: '',
  expiryDays: '',
  totalStock: '',
}

export function parseOptionalNumber(value: string | undefined): number | undefined {
  const trimmed = value?.trim()
  if (!trimmed) {
    return undefined
  }
  return Number(trimmed)
}

export function parseOptionalInteger(value: string | undefined): number | undefined {
  const trimmed = value?.trim()
  if (!trimmed) {
    return undefined
  }
  return Number(trimmed)
}
