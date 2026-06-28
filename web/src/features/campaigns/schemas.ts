import { z } from 'zod'

const campaignTypeSchema = z.enum(['STANDARD', 'SEASONAL', 'EVENT'])
const campaignStatusSchema = z.enum(['DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'])

export const campaignFormSchema = z
  .object({
    code: z.string().trim().min(1, 'Code is required'),
    name: z.string().trim().min(1, 'Name is required'),
    displayName: z.string().optional(),
    description: z.string().optional(),
    campaignType: campaignTypeSchema,
    startAt: z.string().min(1, 'Start date is required'),
    endAt: z.string().min(1, 'End date is required'),
    bannerImageUrl: z.string().optional(),
    thumbnailImageUrl: z.string().optional(),
    priority: z.string().optional(),
    status: campaignStatusSchema.optional(),
  })
  .refine((data) => new Date(data.startAt).getTime() < new Date(data.endAt).getTime(), {
    message: 'Start date must be before end date',
    path: ['endAt'],
  })
  .refine(
    (data) => {
      if (!data.priority?.trim()) {
        return true
      }
      const num = Number(data.priority)
      return Number.isInteger(num)
    },
    { message: 'Priority must be an integer', path: ['priority'] },
  )

export type CampaignFormValues = z.infer<typeof campaignFormSchema>

export function parseFormPriority(value: string | undefined): number | undefined {
  const trimmed = value?.trim()
  if (!trimmed) {
    return undefined
  }
  return Number(trimmed)
}

export const defaultCampaignFormValues: CampaignFormValues = {
  code: '',
  name: '',
  displayName: '',
  description: '',
  campaignType: 'STANDARD',
  startAt: '',
  endAt: '',
  bannerImageUrl: '',
  thumbnailImageUrl: '',
  priority: '',
  status: 'DRAFT',
}
