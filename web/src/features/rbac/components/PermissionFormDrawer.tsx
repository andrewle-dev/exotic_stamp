import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { KeyRound } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input } from '../../../components/ui/FormField'
import {
  defaultPermissionFormValues,
  permissionFormSchema,
  type PermissionFormValues,
} from '../schemas'
import { useCreatePermission } from '../hooks'

interface PermissionFormDrawerProps {
  open: boolean
  onClose: () => void
  onSuccess?: () => void
}

function toPayload(values: PermissionFormValues) {
  return {
    permissionCode: values.permissionCode.trim(),
    description: values.description?.trim() || undefined,
  }
}

export function PermissionFormDrawer({ open, onClose, onSuccess }: PermissionFormDrawerProps) {
  const createMutation = useCreatePermission()
  const isSubmitting = createMutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<PermissionFormValues>({
    resolver: zodResolver(permissionFormSchema),
    defaultValues: defaultPermissionFormValues,
  })

  useEffect(() => {
    if (open) {
      reset(defaultPermissionFormValues)
    }
  }, [open, reset])

  const onSubmit = handleSubmit(async (values) => {
    await createMutation.mutateAsync(toPayload(values))
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title="Create Permission"
      description="Define a permission code used for fine-grained access control."
      formId="permission-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel="Create permission"
      error={createMutation.error}
      onClose={onClose}
    >
      <form id="permission-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <DrawerSectionCard
          icon={KeyRound}
          title="Permission details"
          description="Codes should be stable and descriptive."
        >
          <FormField
            label="Permission code"
            htmlFor="permissionCode"
            required
            error={errors.permissionCode?.message}
          >
            <Input
              id="permissionCode"
              placeholder="STAMP_DESIGN_MANAGE"
              {...register('permissionCode')}
            />
          </FormField>

          <FormField label="Description" htmlFor="description" error={errors.description?.message}>
            <Input
              id="description"
              placeholder="Optional description"
              {...register('description')}
            />
          </FormField>
        </DrawerSectionCard>
      </form>
    </FormDrawer>
  )
}
