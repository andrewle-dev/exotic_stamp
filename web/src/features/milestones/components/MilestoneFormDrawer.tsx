import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Flag, Gift, MapPin, Send } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input, Select, Textarea } from '../../../components/ui/FormField'
import { ASSET_UPLOAD_HELP } from '../../uploads/assetUploadPurpose'
import { AssetImageFieldCard } from '../../uploads/components/AssetImageFieldCard'
import type { CampaignResponse } from '../../../types/campaigns'
import type { MilestoneResponse } from '../../../types/milestones'
import {
  buildCampaignOptions,
  shortenId,
} from '../../stamp-designs/utils/resolve-labels'
import {
  defaultMilestoneFormValues,
  milestoneFormSchema,
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

function toCreatePayload(values: MilestoneFormValues, existingSortOrder?: number) {
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
    // Ordering is managed via Milestones reorder drawer — preserve on edit, omit on create.
    sortOrder: existingSortOrder,
  }
}

function toUpdatePayload(values: MilestoneFormValues, existingSortOrder?: number) {
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
    sortOrder: existingSortOrder,
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
      })
    } else {
      reset(defaultMilestoneFormValues)
    }
  }, [open, milestone, reset])

  const onSubmit = handleSubmit(async (values) => {
    const existingSortOrder = isEdit ? milestone?.sortOrder : undefined
    if (isEdit && milestone) {
      await updateMutation.mutateAsync({
        id: milestone.id,
        body: toUpdatePayload(values, existingSortOrder),
      })
    } else {
      await createMutation.mutateAsync(toCreatePayload(values, existingSortOrder))
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
      <form id="milestone-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <DrawerSectionCard
          icon={MapPin}
          title="Campaign"
          description="Which campaign this milestone belongs to."
        >
          <FormField
            label="Campaign"
            htmlFor="campaignId"
            required
            error={errors.campaignId?.message}
          >
            <Select
              id="campaignId"
              {...register('campaignId')}
              disabled={isEdit}
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
            </Select>
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Flag}
          title="Trigger"
          description="Stamp threshold and milestone identity."
        >
          <FormField
            label="Code"
            htmlFor="code"
            required
            error={errors.code?.message}
            help="Unique milestone code, e.g. MS3, MS7, MS14."
          >
            <Input id="code" placeholder="MS3" {...register('code')} />
          </FormField>

          <FormField
            label="Required stamp count"
            htmlFor="requiredStampCount"
            required
            error={errors.requiredStampCount?.message}
            help="Number of stamps the collector must gather to unlock this reward."
          >
            <Input
              id="requiredStampCount"
              type="number"
              min={1}
              step={1}
              {...register('requiredStampCount')}
            />
          </FormField>

          <FormField label="Name" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" {...register('name')} />
          </FormField>

          <FormField label="Description" htmlFor="description" error={errors.description?.message}>
            <Textarea id="description" rows={3} {...register('description')} />
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Gift}
          title="Reward"
          description="What the collector receives when this milestone unlocks."
        >
          <FormField
            label="Reward type"
            htmlFor="rewardType"
            required
            error={errors.rewardType?.message}
          >
            <Select id="rewardType" {...register('rewardType')}>
              <option value="VOUCHER">Voucher</option>
              <option value="DIGITAL_STICKER">Digital sticker</option>
              <option value="BONUS_STAMP">Bonus stamp</option>
            </Select>
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
            <Textarea id="rewardDescription" rows={2} {...register('rewardDescription')} />
          </FormField>

          <Controller
            name="rewardImageUrl"
            control={control}
            render={({ field }) => (
              <AssetImageFieldCard
                id="rewardImageUrl"
                title="Reward image"
                value={field.value ?? ''}
                onChange={field.onChange}
                error={errors.rewardImageUrl?.message}
                formDirty={isDirty}
                help={ASSET_UPLOAD_HELP.milestoneReward}
                purpose="MILESTONE_REWARD"
                clearable
              />
            )}
          />
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Send}
          title="Publishing"
          description="Visibility status. List order is managed via Reorder Milestones."
        >
          <FormField label="Status" htmlFor="status" error={errors.status?.message}>
            <Select id="status" {...register('status')}>
              <option value="DRAFT">Draft</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="ARCHIVED">Archived</option>
            </Select>
          </FormField>
        </DrawerSectionCard>
      </form>
    </FormDrawer>
  )
}
