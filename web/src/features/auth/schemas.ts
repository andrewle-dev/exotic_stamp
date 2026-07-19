import { z } from 'zod'

export const loginSchema = z.object({
  identifier: z
    .string()
    .min(1, 'Email or username is required')
    .trim(),
  password: z
    .string()
    .min(1, 'Password is required'),
})

export const forgotPasswordSchema = z.object({
  email: z
    .string()
    .trim()
    .email('Please enter a valid email'),
})

export const resetPasswordSchema = z
  .object({
    email: z
      .string()
      .trim()
      .email('Please enter a valid email'),
    otp: z
      .string()
      .trim()
      .regex(/^\d{6}$/, 'OTP must be 6 digits'),
    newPassword: z
      .string()
      .min(6, 'Password must be at least 6 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  })

export type LoginFormValues = z.infer<typeof loginSchema>
export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>
export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>
