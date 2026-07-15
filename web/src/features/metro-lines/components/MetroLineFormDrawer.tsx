import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Palette, FileText, Send } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input, Select, Textarea } from '../../../components/ui/FormField'
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

function toPayload(values: MetroLineFormValues, existingSortOrder?: number) {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    displayName: values.displayName?.trim() || undefined,
    description: values.description?.trim() || undefined,
    colorHex: values.colorHex?.trim() || undefined,
    // Ordering is managed via Metro Lines reorder drawer — preserve on edit, default on create.
    sortOrder: existingSortOrder ?? 0,
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
        status: line.status,
      })
    } else {
      reset(defaultMetroLineFormValues)
    }
  }, [open, line, reset])

  const onSubmit = handleSubmit(async (values) => {
    const payload = toPayload(values, isEdit ? line?.sortOrder : undefined)
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
        <DrawerSectionCard
          icon={FileText}
          title="Details"
          description="Identifiers and display labels for this line."
        >
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
            <Textarea
              id="description"
              rows={3}
              disabled={isSubmitting}
              {...register('description')}
            />
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Palette}
          title="Appearance"
          description="Brand color used in maps and station lists."
        >
          <FormField
            label="Color (hex)"
            htmlFor="colorHex"
            hint="#RRGGBB"
            error={errors.colorHex?.message}
          >
            <Input
              id="colorHex"
              placeholder="#01599D"
              disabled={isSubmitting}
              {...register('colorHex')}
            />
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Send}
          title="Publishing"
          description="Visibility status. List order is managed via Reorder Metro Lines."
        >
          <FormField label="Status" htmlFor="status" error={errors.status?.message}>
            <Select id="status" disabled={isSubmitting} {...register('status')}>
              <option value="DRAFT">Draft</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </Select>
          </FormField>
        </DrawerSectionCard>
      </form>
    </FormDrawer>
  )
}
