import { z } from 'zod'

function parseDateOnly(value: string | undefined): Date | null {
  const trimmed = value?.trim()
  if (!trimmed) {
    return null
  }
  const date = new Date(`${trimmed}T00:00:00`)
  return Number.isNaN(date.getTime()) ? null : date
}

export const partnerFormSchema = z
  .object({
    name: z.string().trim().min(1, 'Name is required'),
    logoUrl: z.string().optional(),
    bannerImageUrl: z.string().optional(),
    contactEmail: z
      .string()
      .optional()
      .refine((val) => !val?.trim() || z.string().email().safeParse(val.trim()).success, {
        message: 'Contact email must be valid',
      }),
    contractStartDate: z.string().optional(),
    contractEndDate: z.string().optional(),
  })
  .refine(
    (data) => {
      if (!data.contractStartDate?.trim()) {
        return true
      }
      return parseDateOnly(data.contractStartDate) !== null
    },
    { message: 'Contract start must be a valid date', path: ['contractStartDate'] },
  )
  .refine(
    (data) => {
      if (!data.contractEndDate?.trim()) {
        return true
      }
      return parseDateOnly(data.contractEndDate) !== null
    },
    { message: 'Contract end must be a valid date', path: ['contractEndDate'] },
  )
  .refine(
    (data) => {
      const start = parseDateOnly(data.contractStartDate)
      const end = parseDateOnly(data.contractEndDate)
      if (!start || !end) {
        return true
      }
      return start.getTime() <= end.getTime()
    },
    {
      message: 'Contract start must be on or before contract end',
      path: ['contractEndDate'],
    },
  )

export type PartnerFormValues = z.infer<typeof partnerFormSchema>

export const defaultPartnerFormValues: PartnerFormValues = {
  name: '',
  logoUrl: '',
  bannerImageUrl: '',
  contactEmail: '',
  contractStartDate: '',
  contractEndDate: '',
}
