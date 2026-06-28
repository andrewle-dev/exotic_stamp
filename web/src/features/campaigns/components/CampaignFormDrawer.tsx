import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { FormField, Input } from '../../../components/ui/FormField'
import {
  fromDatetimeLocalValue,
  toDatetimeLocalValue,
} from '../../../lib/formatting/datetime-local'
import type { CampaignResponse } from '../../../types/campaigns'
import {
  campaignFormSchema,
  defaultCampaignFormValues,
  parseFormPriority,
  type CampaignFormValues,
} from '../schemas'
import { useCreateCampaign, useUpdateCampaign } from '../hooks'

interface CampaignFormDrawerProps {
  open: boolean
  campaign?: CampaignResponse | null
  onClose: () => void
  onSuccess?: () => void
}

function toCreatePayload(values: CampaignFormValues) {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    displayName: values.displayName?.trim() || undefined,
    description: values.description?.trim() || undefined,
    campaignType: values.campaignType,
    startAt: fromDatetimeLocalValue(values.startAt),
    endAt: fromDatetimeLocalValue(values.endAt),
    bannerImageUrl: values.bannerImageUrl?.trim() || undefined,
    thumbnailImageUrl: values.thumbnailImageUrl?.trim() || undefined,
    priority: parseFormPriority(values.priority),
  }
}

function toUpdatePayload(values: CampaignFormValues) {
  return {
    ...toCreatePayload(values),
    status: values.status,
  }
}

export function CampaignFormDrawer({
  open,
  campaign,
  onClose,
  onSuccess,
}: CampaignFormDrawerProps) {
  const isEdit = Boolean(campaign)
  const createMutation = useCreateCampaign()
  const updateMutation = useUpdateCampaign()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<CampaignFormValues>({
    resolver: zodResolver(campaignFormSchema),
    defaultValues: defaultCampaignFormValues,
  })

  useEffect(() => {
    if (!open) {
      return
    }
    if (campaign) {
      reset({
        code: campaign.code,
        name: campaign.name,
        displayName: campaign.displayName ?? '',
        description: campaign.description ?? '',
        campaignType: campaign.campaignType ?? 'STANDARD',
        startAt: toDatetimeLocalValue(campaign.startAt),
        endAt: toDatetimeLocalValue(campaign.endAt),
        bannerImageUrl: campaign.bannerImageUrl ?? '',
        thumbnailImageUrl: campaign.thumbnailImageUrl ?? '',
        priority: campaign.priority != null ? String(campaign.priority) : '',
        status: campaign.status,
      })
    } else {
      reset(defaultCampaignFormValues)
    }
  }, [open, campaign, reset])

  const onSubmit = handleSubmit(async (values) => {
    if (isEdit && campaign) {
      await updateMutation.mutateAsync({ id: campaign.id, body: toUpdatePayload(values) })
    } else {
      await createMutation.mutateAsync(toCreatePayload(values))
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Campaign' : 'Create Campaign'}
      description={isEdit ? `Update ${campaign?.name}` : 'Add a new campaign'}
      formId="campaign-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create campaign'}
      error={mutation.error}
      onClose={onClose}
      width="lg"
    >
      <form id="campaign-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Code" htmlFor="code" required error={errors.code?.message}>
            <Input id="code" {...register('code')} autoComplete="off" />
          </FormField>
          <FormField label="Name" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" {...register('name')} />
          </FormField>
        </div>

        <FormField label="Display name" htmlFor="displayName" error={errors.displayName?.message}>
          <Input id="displayName" {...register('displayName')} />
        </FormField>

        <FormField label="Description" htmlFor="description" error={errors.description?.message}>
          <textarea
            id="description"
            rows={3}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            {...register('description')}
          />
        </FormField>

        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            label="Campaign type"
            htmlFor="campaignType"
            required
            error={errors.campaignType?.message}
          >
            <select
              id="campaignType"
              {...register('campaignType')}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            >
              <option value="STANDARD">Standard</option>
              <option value="SEASONAL">Seasonal</option>
              <option value="EVENT">Event</option>
            </select>
          </FormField>

          {isEdit ? (
            <FormField label="Status" htmlFor="status" error={errors.status?.message}>
              <select
                id="status"
                {...register('status')}
                className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
              >
                <option value="DRAFT">Draft</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
                <option value="ARCHIVED">Archived</option>
              </select>
            </FormField>
          ) : null}
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Start at" htmlFor="startAt" required error={errors.startAt?.message}>
            <Input id="startAt" type="datetime-local" {...register('startAt')} />
          </FormField>
          <FormField label="End at" htmlFor="endAt" required error={errors.endAt?.message}>
            <Input id="endAt" type="datetime-local" {...register('endAt')} />
          </FormField>
        </div>

        <FormField label="Priority" htmlFor="priority" error={errors.priority?.message}>
          <Input
            id="priority"
            type="number"
            step={1}
            {...register('priority')}
          />
        </FormField>

        <FormField
          label="Banner image URL"
          htmlFor="bannerImageUrl"
          error={errors.bannerImageUrl?.message}
        >
          <Input id="bannerImageUrl" type="url" {...register('bannerImageUrl')} />
        </FormField>

        <FormField
          label="Thumbnail image URL"
          htmlFor="thumbnailImageUrl"
          error={errors.thumbnailImageUrl?.message}
        >
          <Input id="thumbnailImageUrl" type="url" {...register('thumbnailImageUrl')} />
        </FormField>
      </form>
    </FormDrawer>
  )
}
