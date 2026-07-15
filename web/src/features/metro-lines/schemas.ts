import { z } from 'zod'

const metroStatusSchema = z.enum(['DRAFT', 'ACTIVE', 'INACTIVE'])

export const metroLineFormSchema = z.object({
  code: z.string().trim().min(1, 'Code is required').max(10),
  name: z.string().trim().min(1, 'Name is required').max(100),
  displayName: z.string().max(100).optional(),
  description: z.string().max(500).optional(),
  colorHex: z
    .string()
    .optional()
    .refine((val) => !val || /^#[0-9A-Fa-f]{6}$/.test(val), {
      message: 'Color must be in #RRGGBB format',
    }),
  status: metroStatusSchema,
})

export type MetroLineFormValues = z.infer<typeof metroLineFormSchema>

export const defaultMetroLineFormValues: MetroLineFormValues = {
  code: '',
  name: '',
  displayName: '',
  description: '',
  colorHex: '',
  status: 'DRAFT',
}
