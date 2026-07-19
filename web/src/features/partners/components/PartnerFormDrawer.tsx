import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Building2, Calendar, Image } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input } from '../../../components/ui/FormField'
import { ASSET_UPLOAD_HELP } from '../../uploads/assetUploadPurpose'
import { AssetImageFieldCard } from '../../uploads/components/AssetImageFieldCard'
import type { PartnerResponse } from '../../../types/partners'
import {
  defaultPartnerFormValues,
  partnerFormSchema,
  type PartnerFormValues,
} from '../schemas'
import { useCreatePartner, useUpdatePartner } from '../hooks'

interface PartnerFormDrawerProps {
  open: boolean
  partner?: PartnerResponse | null
  onClose: () => void
  onSuccess?: () => void
}

function toPayload(values: PartnerFormValues) {
  return {
    name: values.name.trim(),
    logoUrl: values.logoUrl?.trim() || undefined,
    bannerImageUrl: values.bannerImageUrl?.trim() || undefined,
    contactEmail: values.contactEmail?.trim() || undefined,
    contractStartDate: values.contractStartDate?.trim() || undefined,
    contractEndDate: values.contractEndDate?.trim() || undefined,
  }
}

export function PartnerFormDrawer({
  open,
  partner,
  onClose,
  onSuccess,
}: PartnerFormDrawerProps) {
  const isEdit = Boolean(partner)
  const createMutation = useCreatePartner()
  const updateMutation = useUpdatePartner()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isDirty },
  } = useForm<PartnerFormValues>({
    resolver: zodResolver(partnerFormSchema),
    defaultValues: defaultPartnerFormValues,
  })

  useEffect(() => {
    if (!open) {
      return
    }
    if (partner) {
      reset({
        name: partner.name,
        logoUrl: partner.logoUrl ?? '',
        bannerImageUrl: partner.bannerImageUrl ?? '',
        contactEmail: partner.contactEmail ?? '',
        contractStartDate: partner.contractStartDate ?? '',
        contractEndDate: partner.contractEndDate ?? '',
      })
    } else {
      reset(defaultPartnerFormValues)
    }
  }, [open, partner, reset])

  const onSubmit = handleSubmit(async (values) => {
    if (isEdit && partner) {
      await updateMutation.mutateAsync({ id: partner.id, body: toPayload(values) })
    } else {
      await createMutation.mutateAsync(toPayload(values))
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Partner' : 'Add Partner'}
      description={
        isEdit
          ? `Update ${partner?.name}`
          : 'Partner data is used in reward configuration'
      }
      formId="partner-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create partner'}
      error={mutation.error}
      onClose={onClose}
      width="lg"
    >
      <form id="partner-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <DrawerSectionCard
          icon={Building2}
          title="Identity"
          description="Partner name and primary contact."
        >
          <FormField label="Partner name" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" placeholder="Grab Vietnam" {...register('name')} />
          </FormField>

          <FormField
            label="Contact email"
            htmlFor="contactEmail"
            error={errors.contactEmail?.message}
          >
            <Input
              id="contactEmail"
              type="email"
              placeholder="partner@example.com"
              {...register('contactEmail')}
            />
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Image}
          title="Brand assets"
          description="Logo and banner used across reward and Home surfaces."
        >
          <div className="space-y-6">
            <Controller
              name="logoUrl"
              control={control}
              render={({ field }) => (
                <AssetImageFieldCard
                  id="logoUrl"
                  title="Partner logo"
                  help={ASSET_UPLOAD_HELP.partnerLogo}
                  purpose="PARTNER_LOGO"
                  value={field.value ?? ''}
                  onChange={field.onChange}
                  error={errors.logoUrl?.message}
                  formDirty={isDirty}
                  previewAspect="square"
                  clearable
                />
              )}
            />

            <div className="border-t border-border pt-6">
              <Controller
                name="bannerImageUrl"
                control={control}
                render={({ field }) => (
                  <AssetImageFieldCard
                    id="bannerImageUrl"
                    title="Partner banner"
                    help={ASSET_UPLOAD_HELP.partnerBanner}
                    purpose="PARTNER_BANNER"
                    value={field.value ?? ''}
                    onChange={field.onChange}
                    error={errors.bannerImageUrl?.message}
                    formDirty={isDirty}
                    previewAspect="wide"
                    clearable
                  />
                )}
              />
            </div>
          </div>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Calendar}
          title="Contract"
          description="Optional partnership window dates."
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <FormField
              label="Contract start"
              htmlFor="contractStartDate"
              error={errors.contractStartDate?.message}
            >
              <Input id="contractStartDate" type="date" {...register('contractStartDate')} />
            </FormField>

            <FormField
              label="Contract end"
              htmlFor="contractEndDate"
              error={errors.contractEndDate?.message}
            >
              <Input id="contractEndDate" type="date" {...register('contractEndDate')} />
            </FormField>
          </div>
        </DrawerSectionCard>
      </form>
    </FormDrawer>
  )
}
