import { useEffect, useMemo } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { FormField } from '../../../components/ui/FormField'
import { formatNumber } from '../../../lib/formatting/number'
import type { RewardResponse } from '../../../types/rewards'
import {
  bulkUploadFormSchema,
  summarizeVoucherCodes,
  type BulkUploadFormValues,
} from '../../vouchers/schemas'
import { useBulkUploadRewardVouchers } from '../hooks'

interface BulkUploadVouchersDrawerProps {
  open: boolean
  reward: RewardResponse | null
  onClose: () => void
}

export function BulkUploadVouchersDrawer({ open, reward, onClose }: BulkUploadVouchersDrawerProps) {
  const mutation = useBulkUploadRewardVouchers()

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isDirty },
  } = useForm<BulkUploadFormValues>({
    resolver: zodResolver(bulkUploadFormSchema),
    defaultValues: { codesText: '' },
  })

  const codesText = useWatch({ control, name: 'codesText' })
  const summary = useMemo(() => summarizeVoucherCodes(codesText ?? ''), [codesText])

  useEffect(() => {
    if (!open) {
      reset({ codesText: '' })
      mutation.reset()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, reset])

  const onSubmit = handleSubmit(async (values) => {
    if (!reward) {
      return
    }
    const codes = summarizeVoucherCodes(values.codesText).codes
    await mutation.mutateAsync({ id: reward.id, body: { codes } })
  })

  const stats = mutation.data

  return (
    <FormDrawer
      open={open}
      title="Upload Voucher Codes"
      description={reward ? `Bulk upload codes for ${reward.name}` : undefined}
      formId="bulk-upload-form"
      isSubmitting={mutation.isPending}
      isDirty={isDirty}
      saveLabel="Upload codes"
      error={mutation.isError ? mutation.error : undefined}
      onClose={onClose}
      width="lg"
    >
      <form id="bulk-upload-form" className="space-y-6" onSubmit={onSubmit} noValidate>
        <FormField
          label="Voucher codes"
          htmlFor="codesText"
          required
          error={errors.codesText?.message}
          hint="One code per line. Blank lines are ignored; duplicates are removed before upload."
        >
          <textarea
            id="codesText"
            rows={10}
            placeholder={'GRAB-XXXX-XXXX\nCOFFEE-1234-5678'}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 font-mono text-sm"
            {...register('codesText')}
          />
        </FormField>

        {codesText?.trim() ? (
          <div className="rounded-lg border border-border bg-secondary/30 p-4 text-sm">
            <p className="font-medium text-foreground">Pre-submit summary</p>
            <ul className="mt-2 space-y-1 text-muted-foreground">
              <li>Total lines: {summary.totalLines}</li>
              <li>Valid non-empty codes: {summary.validCodes}</li>
              <li>Duplicate lines removed: {summary.duplicatesRemoved}</li>
            </ul>
          </div>
        ) : null}

        {stats ? (
          <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm">
            <p className="font-medium text-emerald-800">Upload complete</p>
            <ul className="mt-2 space-y-1 text-emerald-700">
              <li>Available: {formatNumber(stats.availableCount)}</li>
              <li>Redeemed: {formatNumber(stats.redeemedCount)}</li>
            </ul>
          </div>
        ) : null}
      </form>
    </FormDrawer>
  )
}
