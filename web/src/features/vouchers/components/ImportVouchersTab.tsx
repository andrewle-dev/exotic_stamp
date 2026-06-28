import { useMemo } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Button } from '../../../components/ui/Button'
import { FormField, Input } from '../../../components/ui/FormField'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import { formatNumber } from '../../../lib/formatting/number'
import { fromDatetimeLocalValue } from '../../../lib/formatting/datetime-local'
import type { MilestoneResponse } from '../../../types/milestones'
import {
  defaultImportVouchersFormValues,
  importVouchersFormSchema,
  summarizeVoucherCodes,
  type ImportVouchersFormValues,
} from '../schemas'
import { buildMilestoneOptions } from '../../rewards/utils/resolve-labels'
import { useImportVouchers } from '../hooks'
import { formatImportResultRows } from '../utils/import-result'

interface ImportVouchersTabProps {
  milestones: MilestoneResponse[]
}

export function ImportVouchersTab({ milestones }: ImportVouchersTabProps) {
  const importMutation = useImportVouchers()
  const milestoneOptions = buildMilestoneOptions(milestones)

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors },
  } = useForm<ImportVouchersFormValues>({
    resolver: zodResolver(importVouchersFormSchema),
    defaultValues: defaultImportVouchersFormValues,
  })

  const codesText = useWatch({ control, name: 'codesText' })
  const summary = useMemo(() => summarizeVoucherCodes(codesText ?? ''), [codesText])

  const resultRows = importMutation.data ? formatImportResultRows(importMutation.data) : null

  const onSubmit = handleSubmit(async (values) => {
    const codes = summarizeVoucherCodes(values.codesText).codes
    await importMutation.mutateAsync({
      milestoneId: values.milestoneId,
      codes,
      expiresAt: values.expiresAt?.trim()
        ? fromDatetimeLocalValue(values.expiresAt)
        : undefined,
    })
  })

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm text-muted-foreground">
          Import voucher codes into the pool for a milestone. Codes are deduplicated client-side
          before submission.
        </p>
        <p className="mt-1 text-xs text-muted-foreground">
          This uses the milestone-level import endpoint, not reward-specific bulk upload.
        </p>
      </div>

      <form
        className="max-w-2xl space-y-6 rounded-lg border border-border bg-card p-6"
        onSubmit={onSubmit}
        noValidate
      >
        <FormField
          label="Milestone"
          htmlFor="import-milestoneId"
          required
          error={errors.milestoneId?.message}
        >
          <select
            id="import-milestoneId"
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            {...register('milestoneId')}
          >
            <option value="">Select milestone…</option>
            {milestoneOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </FormField>

        <FormField
          label="Voucher codes"
          htmlFor="import-codesText"
          required
          error={errors.codesText?.message}
          hint="One code per line. Blank lines are ignored; duplicates are removed before import."
        >
          <textarea
            id="import-codesText"
            rows={12}
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

        <FormField
          label="Expires at"
          htmlFor="import-expiresAt"
          error={errors.expiresAt?.message}
          hint="Optional. When set, imported vouchers expire at this date and time."
        >
          <Input id="import-expiresAt" type="datetime-local" {...register('expiresAt')} />
        </FormField>

        {importMutation.isError ? <ApiErrorAlert error={importMutation.error} /> : null}

        {resultRows && resultRows.length > 0 ? (
          <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm">
            <p className="font-medium text-emerald-800">Import result</p>
            <ul className="mt-2 space-y-1 text-emerald-700">
              {resultRows.map((row) => (
                <li key={row.label}>
                  {row.label}: {formatNumber(row.count)}
                </li>
              ))}
            </ul>
          </div>
        ) : null}

        <div className="flex gap-2">
          <Button type="submit" size="md" disabled={importMutation.isPending}>
            {importMutation.isPending ? 'Importing…' : 'Import vouchers'}
          </Button>
          <Button
            type="button"
            variant="secondary"
            size="md"
            disabled={importMutation.isPending}
            onClick={() => {
              reset(defaultImportVouchersFormValues)
              importMutation.reset()
            }}
          >
            Clear form
          </Button>
        </div>
      </form>
    </div>
  )
}
