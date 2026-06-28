import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { FormField, Input } from '../../../components/ui/FormField'
import type { LineResponse } from '../../../types/metro-lines'
import {
  defaultMetroLineFormValues,
  metroLineFormSchema,
  type MetroLineFormValues,
} from '../schemas'
import { useCreateMetroLine, useUpdateMetroLine } from '../hooks'

interface MetroLineFormDrawerProps {
  open: boolean
  line?: LineResponse | null
  onClose: () => void
  onSuccess?: () => void
}

function toPayload(values: MetroLineFormValues) {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    displayName: values.displayName?.trim() || undefined,
    description: values.description?.trim() || undefined,
    colorHex: values.colorHex?.trim() || undefined,
    sortOrder: values.sortOrder,
    status: values.status,
  }
}

export function MetroLineFormDrawer({ open, line, onClose, onSuccess }: MetroLineFormDrawerProps) {
  const isEdit = Boolean(line)
  const createMutation = useCreateMetroLine()
  const updateMutation = useUpdateMetroLine()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<MetroLineFormValues>({
    resolver: zodResolver(metroLineFormSchema),
    defaultValues: defaultMetroLineFormValues,
  })

  useEffect(() => {
    if (!open) {
      return
    }
    if (line) {
      reset({
        code: line.code,
        name: line.name,
        displayName: line.displayName ?? '',
        description: line.description ?? '',
        colorHex: line.colorHex ?? '',
        sortOrder: line.sortOrder ?? 0,
        status: line.status,
      })
    } else {
      reset(defaultMetroLineFormValues)
    }
  }, [open, line, reset])

  const onSubmit = handleSubmit(async (values) => {
    const payload = toPayload(values)
    if (isEdit && line) {
      await updateMutation.mutateAsync({ id: line.id, body: payload })
    } else {
      await createMutation.mutateAsync(payload)
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Metro Line' : 'Create Metro Line'}
      description={isEdit ? `Update configuration for ${line?.code}` : 'Add a new metro line'}
      formId="metro-line-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create line'}
      error={mutation.error}
      onClose={onClose}
    >
      <form id="metro-line-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <FormField label="Code" htmlFor="code" required error={errors.code?.message}>
          <Input id="code" disabled={isSubmitting} {...register('code')} />
        </FormField>

        <FormField label="Name" htmlFor="name" required error={errors.name?.message}>
          <Input id="name" disabled={isSubmitting} {...register('name')} />
        </FormField>

        <FormField label="Display name" htmlFor="displayName" error={errors.displayName?.message}>
          <Input id="displayName" disabled={isSubmitting} {...register('displayName')} />
        </FormField>

        <FormField label="Description" htmlFor="description" error={errors.description?.message}>
          <textarea
            id="description"
            rows={3}
            disabled={isSubmitting}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            {...register('description')}
          />
        </FormField>

        <FormField label="Color (hex)" htmlFor="colorHex" hint="#RRGGBB" error={errors.colorHex?.message}>
          <Input id="colorHex" placeholder="#01599D" disabled={isSubmitting} {...register('colorHex')} />
        </FormField>

        <FormField label="Sort order" htmlFor="sortOrder" error={errors.sortOrder?.message}>
          <Input
            id="sortOrder"
            type="number"
            disabled={isSubmitting}
            {...register('sortOrder', { valueAsNumber: true })}
          />
        </FormField>

        <FormField label="Status" htmlFor="status" error={errors.status?.message}>
          <select
            id="status"
            disabled={isSubmitting}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            {...register('status')}
          >
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </FormField>
      </form>
    </FormDrawer>
  )
}
