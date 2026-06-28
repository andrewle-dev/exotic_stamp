import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { FormField, Input } from '../../../components/ui/FormField'
import { PublicAssetUploadField } from '../../uploads/components/PublicAssetUploadField'
import type { CampaignResponse } from '../../../types/campaigns'
import type { MilestoneResponse } from '../../../types/milestones'
import {
  buildCampaignOptions,
  shortenId,
} from '../../stamp-designs/utils/resolve-labels'
import {
  defaultMilestoneFormValues,
  milestoneFormSchema,
  parseFormSortOrder,
  type MilestoneFormValues,
} from '../schemas'
import { useCreateMilestone, useUpdateMilestone } from '../hooks'

interface MilestoneFormDrawerProps {
  open: boolean
  milestone?: MilestoneResponse | null
  campaigns: CampaignResponse[]
  onClose: () => void
  onSuccess?: () => void
}

function toCreatePayload(values: MilestoneFormValues) {
  return {
    campaignId: values.campaignId,
    code: values.code.trim(),
    requiredStampCount: Number(values.requiredStampCount),
    name: values.name.trim(),
    description: values.description?.trim() || undefined,
    rewardType: values.rewardType,
    rewardTitle: values.rewardTitle.trim(),
    rewardDescription: values.rewardDescription?.trim() || undefined,
    rewardImageUrl: values.rewardImageUrl?.trim() || undefined,
    status: values.status,
    sortOrder: parseFormSortOrder(values.sortOrder),
  }
}

function toUpdatePayload(values: MilestoneFormValues) {
  return {
    code: values.code.trim(),
    requiredStampCount: Number(values.requiredStampCount),
    name: values.name.trim(),
    description: values.description?.trim() || undefined,
    rewardType: values.rewardType,
    rewardTitle: values.rewardTitle.trim(),
    rewardDescription: values.rewardDescription?.trim() || undefined,
    rewardImageUrl: values.rewardImageUrl?.trim() || undefined,
    status: values.status,
    sortOrder: parseFormSortOrder(values.sortOrder),
  }
}

export function MilestoneFormDrawer({
  open,
  milestone,
  campaigns,
  onClose,
  onSuccess,
}: MilestoneFormDrawerProps) {
  const isEdit = Boolean(milestone)
  const createMutation = useCreateMilestone()
  const updateMutation = useUpdateMilestone()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const campaignOptions = buildCampaignOptions(campaigns)

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isDirty },
  } = useForm<MilestoneFormValues>({
    resolver: zodResolver(milestoneFormSchema),
    defaultValues: defaultMilestoneFormValues,
  })

  useEffect(() => {
    if (!open) {
      return
    }
    if (milestone) {
      reset({
        campaignId: milestone.campaignId,
        code: milestone.code,
        requiredStampCount: String(milestone.requiredStampCount),
        name: milestone.name,
        description: milestone.description ?? '',
        rewardType: milestone.rewardType,
        rewardTitle: milestone.rewardTitle,
        rewardDescription: milestone.rewardDescription ?? '',
        rewardImageUrl: milestone.rewardImageUrl ?? '',
        status: milestone.status ?? 'DRAFT',
        sortOrder: milestone.sortOrder != null ? String(milestone.sortOrder) : '',
      })
    } else {
      reset(defaultMilestoneFormValues)
    }
  }, [open, milestone, reset])

  const onSubmit = handleSubmit(async (values) => {
    if (isEdit && milestone) {
      await updateMutation.mutateAsync({ id: milestone.id, body: toUpdatePayload(values) })
    } else {
      await createMutation.mutateAsync(toCreatePayload(values))
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Milestone' : 'Create Milestone'}
      description={
        isEdit
          ? `Update ${milestone?.name}`
          : 'Milestone triggers reward issuance when a user reaches the stamp count'
      }
      formId="milestone-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create milestone'}
      error={mutation.error}
      onClose={onClose}
      width="lg"
    >
      <form id="milestone-form" className="space-y-6" onSubmit={onSubmit} noValidate>
        <div className="space-y-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Campaign
          </h3>

          <FormField
            label="Campaign"
            htmlFor="campaignId"
            required
            error={errors.campaignId?.message}
          >
            <select
              id="campaignId"
              {...register('campaignId')}
              disabled={isEdit}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm disabled:opacity-60"
            >
              <option value="">Select campaign…</option>
              {campaignOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
              {isEdit &&
              milestone &&
              !campaignOptions.some((o) => o.value === milestone.campaignId) ? (
                <option value={milestone.campaignId}>
                  {shortenId(milestone.campaignId)} (unknown)
                </option>
              ) : null}
            </select>
          </FormField>
        </div>

        <div className="space-y-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Trigger
          </h3>

          <FormField label="Code" htmlFor="code" required error={errors.code?.message}>
            <Input id="code" placeholder="MS3" {...register('code')} />
            <p className="mt-1 text-xs text-muted-foreground">
              Unique milestone code, e.g. MS3, MS7, MS14
            </p>
          </FormField>

          <FormField
            label="Required stamp count"
            htmlFor="requiredStampCount"
            required
            error={errors.requiredStampCount?.message}
          >
            <Input
              id="requiredStampCount"
              type="number"
              min={1}
              step={1}
              {...register('requiredStampCount')}
            />
            <p className="mt-1 text-xs text-muted-foreground">
              User must collect this many stamps to unlock the reward
            </p>
          </FormField>

          <FormField label="Name" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" {...register('name')} />
          </FormField>

          <FormField label="Description" htmlFor="description" error={errors.description?.message}>
            <textarea
              id="description"
              rows={3}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
              {...register('description')}
            />
          </FormField>
        </div>

        <div className="space-y-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Reward
          </h3>

          <FormField
            label="Reward type"
            htmlFor="rewardType"
            required
            error={errors.rewardType?.message}
          >
            <select
              id="rewardType"
              {...register('rewardType')}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            >
              <option value="VOUCHER">Voucher</option>
              <option value="DIGITAL_STICKER">Digital sticker</option>
              <option value="BONUS_STAMP">Bonus stamp</option>
            </select>
          </FormField>

          <FormField
            label="Reward title"
            htmlFor="rewardTitle"
            required
            error={errors.rewardTitle?.message}
          >
            <Input id="rewardTitle" {...register('rewardTitle')} />
          </FormField>

          <FormField
            label="Reward description"
            htmlFor="rewardDescription"
            error={errors.rewardDescription?.message}
          >
            <textarea
              id="rewardDescription"
              rows={2}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
              {...register('rewardDescription')}
            />
          </FormField>

          <Controller
            name="rewardImageUrl"
            control={control}
            render={({ field }) => (
              <PublicAssetUploadField
                id="rewardImageUrl"
                label="Reward image URL"
                value={field.value ?? ''}
                onChange={field.onChange}
                error={errors.rewardImageUrl?.message}
                formDirty={isDirty}
              />
            )}
          />
        </div>

        <div className="space-y-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Publishing
          </h3>

          <div className="grid gap-4 sm:grid-cols-2">
            <FormField label="Sort order" htmlFor="sortOrder" error={errors.sortOrder?.message}>
              <Input id="sortOrder" type="number" min={0} step={1} {...register('sortOrder')} />
            </FormField>

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
          </div>
        </div>
      </form>
    </FormDrawer>
  )
}
