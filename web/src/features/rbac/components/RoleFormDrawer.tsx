import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Shield } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input } from '../../../components/ui/FormField'
import type { RoleResponse } from '../../../types/rbac'
import {
  defaultRoleFormValues,
  roleFormSchema,
  type RoleFormValues,
} from '../schemas'
import { useCreateRole, useUpdateRole } from '../hooks'

interface RoleFormDrawerProps {
  open: boolean
  role?: RoleResponse | null
  onClose: () => void
  onSuccess?: () => void
}

function toCreatePayload(values: RoleFormValues) {
  return {
    roleCode: values.roleCode.trim(),
    description: values.description?.trim() || undefined,
  }
}

function toUpdatePayload(values: RoleFormValues) {
  return {
    roleCode: values.roleCode.trim() || undefined,
    description: values.description?.trim() || undefined,
    status: values.status?.trim() || undefined,
  }
}

export function RoleFormDrawer({ open, role, onClose, onSuccess }: RoleFormDrawerProps) {
  const isEdit = Boolean(role)
  const isSystemRole = Boolean(role?.systemRole)
  const createMutation = useCreateRole()
  const updateMutation = useUpdateRole()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<RoleFormValues>({
    resolver: zodResolver(roleFormSchema),
    defaultValues: defaultRoleFormValues,
  })

  useEffect(() => {
    if (!open) {
      return
    }
    if (role) {
      reset({
        roleCode: role.role ?? '',
        description: role.description ?? '',
        status: role.status ?? '',
      })
    } else {
      reset(defaultRoleFormValues)
    }
  }, [open, role, reset])

  const onSubmit = handleSubmit(async (values) => {
    if (isEdit && role) {
      await updateMutation.mutateAsync({ roleId: role.id, body: toUpdatePayload(values) })
    } else {
      await createMutation.mutateAsync(toCreatePayload(values))
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Role' : 'Create Role'}
      description={
        isSystemRole
          ? 'System roles are protected and should not be modified casually.'
          : isEdit
            ? `Update ${role?.role ?? 'role'}`
            : 'Define a new role code and optional description.'
      }
      formId="role-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create role'}
      saveDisabled={isSystemRole}
      error={mutation.error}
      onClose={onClose}
    >
      <form id="role-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        {isSystemRole ? (
          <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
            System roles are protected and should not be modified casually.
          </p>
        ) : null}

        <DrawerSectionCard
          icon={Shield}
          title="Role details"
          description="Role code used for access control assignments."
        >
          <FormField label="Role code" htmlFor="roleCode" required error={errors.roleCode?.message}>
            <Input
              id="roleCode"
              placeholder="MANAGER"
              disabled={isSystemRole}
              {...register('roleCode')}
            />
          </FormField>

          <FormField label="Description" htmlFor="description" error={errors.description?.message}>
            <Input
              id="description"
              placeholder="Optional description"
              disabled={isSystemRole}
              {...register('description')}
            />
          </FormField>

          {isEdit ? (
            <FormField label="Status" htmlFor="status" error={errors.status?.message}>
              <Input
                id="status"
                placeholder="ACTIVE"
                disabled={isSystemRole}
                {...register('status')}
              />
            </FormField>
          ) : null}
        </DrawerSectionCard>
      </form>
    </FormDrawer>
  )
}
