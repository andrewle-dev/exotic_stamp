import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Gift, Link2, Package } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input, Select, Textarea } from '../../../components/ui/FormField'
import type { MilestoneResponse } from '../../../types/milestones'
import type { PartnerResponse } from '../../../types/partners'
import type { RewardResponse } from '../../../types/rewards'
import {
  defaultRewardFormValues,
  parseOptionalInteger,
  parseOptionalNumber,
  rewardFormSchema,
  type RewardFormValues,
} from '../schemas'
import { buildMilestoneOptions, buildPartnerOptions } from '../utils/resolve-labels'
import { useCreateReward, useUpdateReward } from '../hooks'

interface RewardFormDrawerProps {
  open: boolean
  reward?: RewardResponse | null
  milestones: MilestoneResponse[]
  partners: PartnerResponse[]
  onClose: () => void
  onSuccess?: () => void
}

function toPayload(values: RewardFormValues) {
  return {
    milestoneId: values.milestoneId,
    partnerId: values.partnerId?.trim() || undefined,
    rewardType: values.rewardType,
    name: values.name.trim(),
    description: values.description?.trim() || undefined,
    valueAmount: parseOptionalNumber(values.valueAmount),
    expiryDays: parseOptionalInteger(values.expiryDays),
    totalStock: parseOptionalInteger(values.totalStock),
  }
}

export function RewardFormDrawer({
  open,
  reward,
  milestones,
  partners,
  onClose,
  onSuccess,
}: RewardFormDrawerProps) {
  const isEdit = Boolean(reward)
  const createMutation = useCreateReward()
  const updateMutation = useUpdateReward()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const milestoneOptions = buildMilestoneOptions(milestones)
  const partnerOptions = buildPartnerOptions(partners)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<RewardFormValues>({
    resolver: zodResolver(rewardFormSchema),
    defaultValues: defaultRewardFormValues,
  })

  useEffect(() => {
    if (!open) {
      return
    }
    if (reward) {
      reset({
        milestoneId: reward.milestoneId,
        partnerId: reward.partnerId ?? '',
        rewardType: (reward.rewardType as RewardFormValues['rewardType']) ?? 'VOUCHER',
        name: reward.name,
        description: reward.description ?? '',
        valueAmount: reward.valueAmount !== undefined ? String(reward.valueAmount) : '',
        expiryDays: reward.expiryDays !== undefined ? String(reward.expiryDays) : '',
        totalStock: reward.totalStock !== undefined ? String(reward.totalStock) : '',
      })
    } else {
      reset(defaultRewardFormValues)
    }
  }, [open, reward, reset])

  const onSubmit = handleSubmit(async (values) => {
    const payload = toPayload(values)
    if (isEdit && reward) {
      await updateMutation.mutateAsync({ id: reward.id, body: payload })
    } else {
      await createMutation.mutateAsync(payload)
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Reward' : 'Create Reward'}
      description={
        isEdit
          ? `Update ${reward?.name}`
          : 'Rewards are issued when a user reaches a milestone'
      }
      formId="reward-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create reward'}
      error={mutation.error}
      onClose={onClose}
      width="lg"
    >
      <form id="reward-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <DrawerSectionCard
          icon={Link2}
          title="Assignment"
          description="Link this reward to a milestone and optional partner."
        >
          <FormField
            label="Milestone"
            htmlFor="milestoneId"
            required
            error={errors.milestoneId?.message}
          >
            <Select id="milestoneId" {...register('milestoneId')}>
              <option value="">Select milestone…</option>
              {milestoneOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </Select>
          </FormField>

          <FormField label="Partner" htmlFor="partnerId" error={errors.partnerId?.message}>
            <Select id="partnerId" {...register('partnerId')}>
              {partnerOptions.map((opt) => (
                <option key={opt.value || 'none'} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </Select>
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Gift}
          title="Reward details"
          description="Type, name, and description shown to collectors."
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

          <FormField label="Name" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" placeholder="Coffee Voucher 20k" {...register('name')} />
          </FormField>

          <FormField label="Description" htmlFor="description" error={errors.description?.message}>
            <Textarea
              id="description"
              placeholder="Redeem at Highlands Coffee…"
              rows={3}
              {...register('description')}
            />
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Package}
          title="Stock & expiry"
          description="Monetary value, validity window, and inventory."
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <FormField
              label="Value (₫)"
              htmlFor="valueAmount"
              error={errors.valueAmount?.message}
              help="Use 0 for non-monetary rewards."
            >
              <Input
                id="valueAmount"
                type="number"
                min={0}
                step="any"
                placeholder="20000"
                {...register('valueAmount')}
              />
            </FormField>

            <FormField
              label="Expiry days"
              htmlFor="expiryDays"
              error={errors.expiryDays?.message}
              help="Days from the issue date until the reward expires."
            >
              <Input
                id="expiryDays"
                type="number"
                min={0}
                step={1}
                placeholder="30"
                {...register('expiryDays')}
              />
            </FormField>
          </div>

          <FormField
            label="Total stock"
            htmlFor="totalStock"
            error={errors.totalStock?.message}
            help="Maximum number of vouchers that can be issued from this reward."
          >
            <Input
              id="totalStock"
              type="number"
              min={0}
              step={1}
              placeholder="500"
              {...register('totalStock')}
            />
          </FormField>
        </DrawerSectionCard>
      </form>
    </FormDrawer>
  )
}
