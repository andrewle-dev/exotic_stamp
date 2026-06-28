import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { FormField, Input } from '../../../components/ui/FormField'
import { PublicAssetUploadField } from '../../uploads/components/PublicAssetUploadField'
import type { StampDesignResponse } from '../../../types/stamp-designs'
import type { CampaignResponse } from '../../../types/campaigns'
import type { StationResponse } from '../../../types/stations'
import {
  buildCampaignOptions,
  buildStationOptions,
  shortenId,
} from '../utils/resolve-labels'
import {
  defaultStampDesignFormValues,
  parseFormSortOrder,
  stampDesignFormSchema,
  type StampDesignFormValues,
} from '../schemas'
import { useCreateStampDesign, useUpdateStampDesign } from '../hooks'

interface StampDesignFormDrawerProps {
  open: boolean
  stampDesign?: StampDesignResponse | null
  campaigns: CampaignResponse[]
  stations: StationResponse[]
  onClose: () => void
  onSuccess?: () => void
}

function toCreatePayload(values: StampDesignFormValues) {
  return {
    campaignId: values.campaignId,
    stationId: values.stationId,
    name: values.name.trim(),
    description: values.description?.trim() || undefined,
    imageUrl: values.imageUrl.trim(),
    previewImageUrl: values.previewImageUrl?.trim() || undefined,
    rarity: values.rarity,
    status: values.status,
    sortOrder: parseFormSortOrder(values.sortOrder),
  }
}

function toUpdatePayload(values: StampDesignFormValues) {
  return toCreatePayload(values)
}

export function StampDesignFormDrawer({
  open,
  stampDesign,
  campaigns,
  stations,
  onClose,
  onSuccess,
}: StampDesignFormDrawerProps) {
  const isEdit = Boolean(stampDesign)
  const createMutation = useCreateStampDesign()
  const updateMutation = useUpdateStampDesign()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const campaignOptions = buildCampaignOptions(campaigns)
  const stationOptions = buildStationOptions(stations)

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isDirty },
  } = useForm<StampDesignFormValues>({
    resolver: zodResolver(stampDesignFormSchema),
    defaultValues: defaultStampDesignFormValues,
  })

  useEffect(() => {
    if (!open) {
      return
    }
    if (stampDesign) {
      reset({
        campaignId: stampDesign.campaignId,
        stationId: stampDesign.stationId,
        name: stampDesign.name,
        description: stampDesign.description ?? '',
        imageUrl: stampDesign.imageUrl,
        previewImageUrl: stampDesign.previewImageUrl ?? '',
        rarity: stampDesign.rarity ?? 'COMMON',
        status: stampDesign.status ?? 'DRAFT',
        sortOrder: stampDesign.sortOrder != null ? String(stampDesign.sortOrder) : '',
      })
    } else {
      reset(defaultStampDesignFormValues)
    }
  }, [open, stampDesign, reset])

  const onSubmit = handleSubmit(async (values) => {
    if (isEdit && stampDesign) {
      await updateMutation.mutateAsync({ id: stampDesign.id, body: toUpdatePayload(values) })
    } else {
      await createMutation.mutateAsync(toCreatePayload(values))
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Stamp Design' : 'Create Stamp Design'}
      description={
        isEdit ? `Update ${stampDesign?.name}` : 'Configure stamp artwork for a campaign station'
      }
      formId="stamp-design-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create stamp design'}
      error={mutation.error}
      onClose={onClose}
      width="lg"
    >
      <form id="stamp-design-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <FormField
          label="Campaign"
          htmlFor="campaignId"
          required
          error={errors.campaignId?.message}
        >
          <select
            id="campaignId"
            {...register('campaignId')}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
          >
            <option value="">Select campaign…</option>
            {campaignOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
            {isEdit &&
            stampDesign &&
            !campaignOptions.some((o) => o.value === stampDesign.campaignId) ? (
              <option value={stampDesign.campaignId}>
                {shortenId(stampDesign.campaignId)} (unknown)
              </option>
            ) : null}
          </select>
        </FormField>

        <FormField label="Station" htmlFor="stationId" required error={errors.stationId?.message}>
          <select
            id="stationId"
            {...register('stationId')}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
          >
            <option value="">Select station…</option>
            {stationOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
            {isEdit &&
            stampDesign &&
            !stationOptions.some((o) => o.value === stampDesign.stationId) ? (
              <option value={stampDesign.stationId}>
                {shortenId(stampDesign.stationId)} (unknown)
              </option>
            ) : null}
          </select>
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

        <Controller
          name="imageUrl"
          control={control}
          render={({ field }) => (
            <PublicAssetUploadField
              id="imageUrl"
              label="Image URL"
              required
              value={field.value}
              onChange={field.onChange}
              error={errors.imageUrl?.message}
              formDirty={isDirty}
            />
          )}
        />

        <Controller
          name="previewImageUrl"
          control={control}
          render={({ field }) => (
            <PublicAssetUploadField
              id="previewImageUrl"
              label="Preview image URL"
              value={field.value ?? ''}
              onChange={field.onChange}
              error={errors.previewImageUrl?.message}
              formDirty={isDirty}
            />
          )}
        />

        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Rarity" htmlFor="rarity" error={errors.rarity?.message}>
            <select
              id="rarity"
              {...register('rarity')}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            >
              <option value="COMMON">Common</option>
              <option value="RARE">Rare</option>
              <option value="EPIC">Epic</option>
              <option value="LEGENDARY">Legendary</option>
            </select>
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
            </select>
          </FormField>
        </div>

        <FormField label="Sort order" htmlFor="sortOrder" error={errors.sortOrder?.message}>
          <Input id="sortOrder" type="number" step={1} {...register('sortOrder')} />
        </FormField>
      </form>
    </FormDrawer>
  )
}
