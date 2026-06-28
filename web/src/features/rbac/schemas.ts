import { z } from 'zod'

export const roleFormSchema = z.object({
  roleCode: z.string().trim().min(1, 'Role code is required').max(64, 'Role code must be at most 64 characters'),
  description: z
    .string()
    .max(500, 'Description must be at most 500 characters')
    .optional()
    .or(z.literal('')),
  status: z
    .string()
    .max(20, 'Status must be at most 20 characters')
    .optional()
    .or(z.literal('')),
})

export type RoleFormValues = z.infer<typeof roleFormSchema>

export const defaultRoleFormValues: RoleFormValues = {
  roleCode: '',
  description: '',
  status: '',
}

export const permissionFormSchema = z.object({
  permissionCode: z
    .string()
    .trim()
    .min(1, 'Permission code is required')
    .max(80, 'Permission code must be at most 80 characters'),
  description: z
    .string()
    .max(500, 'Description must be at most 500 characters')
    .optional()
    .or(z.literal('')),
})

export type PermissionFormValues = z.infer<typeof permissionFormSchema>

export const defaultPermissionFormValues: PermissionFormValues = {
  permissionCode: '',
  description: '',
}
