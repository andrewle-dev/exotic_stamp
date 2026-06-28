import { z } from 'zod'

const metroStatusSchema = z.enum(['DRAFT', 'ACTIVE', 'INACTIVE'])

function optionalNumericField(min: number, max: number, integer = false) {
  return z
    .string()
    .optional()
    .refine(
      (val) => {
        if (val === undefined || val.trim() === '') {
          return true
        }
        const num = Number(val)
        if (Number.isNaN(num)) {
          return false
        }
        if (integer && !Number.isInteger(num)) {
          return false
        }
        return num >= min && num <= max
      },
      { message: `Must be a valid number between ${min} and ${max}` },
    )
}

export const stationFormSchema = z.object({
  lineId: z.string().uuid('Line is required'),
  code: z.string().trim().min(1, 'Code is required').max(20),
  name: z.string().trim().min(1, 'Name is required').max(100),
  displayName: z.string().max(100).optional(),
  description: z.string().max(500).optional(),
  address: z.string().max(255).optional(),
  sortOrder: z.number().int().min(0),
  latitude: optionalNumericField(-90, 90),
  longitude: optionalNumericField(-180, 180),
  zoneRadiusMeters: optionalNumericField(20, 1000, true),
  imageUrl: z.string().optional(),
  stampPreviewUrl: z.string().optional(),
  status: metroStatusSchema,
})

export type StationFormValues = z.infer<typeof stationFormSchema>

export const defaultStationFormValues: StationFormValues = {
  lineId: '',
  code: '',
  name: '',
  displayName: '',
  description: '',
  address: '',
  sortOrder: 0,
  latitude: '',
  longitude: '',
  zoneRadiusMeters: '',
  imageUrl: '',
  stampPreviewUrl: '',
  status: 'DRAFT',
}

const scanKeyStatusSchema = z.enum(['ACTIVE', 'INACTIVE'])

export const scanKeysFormSchema = z.object({
  nfcTagId: z.string().optional(),
  qrCodeValue: z.string().optional(),
  scanKeyStatus: scanKeyStatusSchema,
})

export type ScanKeysFormValues = z.infer<typeof scanKeysFormSchema>

export function parseOptionalNumber(value: string | undefined): number | undefined {
  if (value === undefined || value.trim() === '') {
    return undefined
  }
  return Number(value)
}
