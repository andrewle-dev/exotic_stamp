import { z } from 'zod'

export const importVouchersFormSchema = z
  .object({
    milestoneId: z.string().uuid('Milestone is required'),
    codesText: z.string().min(1, 'At least one voucher code is required'),
    expiresAt: z.string().optional(),
  })
  .refine(
    (data) => {
      if (!data.expiresAt?.trim()) {
        return true
      }
      const date = new Date(data.expiresAt)
      return !Number.isNaN(date.getTime())
    },
    { message: 'Expires at must be a valid date and time', path: ['expiresAt'] },
  )
  .refine(
    (data) => parseVoucherCodes(data.codesText).length > 0,
  { message: 'At least one non-empty voucher code is required', path: ['codesText'] },
  )

export type ImportVouchersFormValues = z.infer<typeof importVouchersFormSchema>

export const defaultImportVouchersFormValues: ImportVouchersFormValues = {
  milestoneId: '',
  codesText: '',
  expiresAt: '',
}

export interface ParsedVoucherCodes {
  codes: string[]
  totalLines: number
  validCodes: number
  duplicatesRemoved: number
}

export function parseVoucherCodes(text: string): string[] {
  const lines = text.split(/\r?\n/)
  const seen = new Set<string>()
  const codes: string[] = []

  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) {
      continue
    }
    const key = trimmed.toLowerCase()
    if (!seen.has(key)) {
      seen.add(key)
      codes.push(trimmed)
    }
  }

  return codes
}

export function summarizeVoucherCodes(text: string): ParsedVoucherCodes {
  const lines = text.split(/\r?\n/)
  const totalLines = lines.length
  const nonEmptyLines = lines.filter((line) => line.trim().length > 0)
  const codes = parseVoucherCodes(text)

  return {
    codes,
    totalLines,
    validCodes: codes.length,
    duplicatesRemoved: nonEmptyLines.length - codes.length,
  }
}

export const bulkUploadFormSchema = z
  .object({
    codesText: z.string().min(1, 'At least one voucher code is required'),
  })
  .refine((data) => parseVoucherCodes(data.codesText).length > 0, {
    message: 'At least one non-empty voucher code is required',
    path: ['codesText'],
  })

export type BulkUploadFormValues = z.infer<typeof bulkUploadFormSchema>
