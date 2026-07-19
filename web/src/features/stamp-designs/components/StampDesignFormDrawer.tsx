import { useEffect } from 'react'

import { Controller, useForm } from 'react-hook-form'

import { zodResolver } from '@hookform/resolvers/zod'

import { Image, MapPin, FileText, Send } from 'lucide-react'

import { FormDrawer } from '../../../components/ui/FormDrawer'

import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'

import { FormField, Input, Select, Textarea } from '../../../components/ui/FormField'

import { ASSET_UPLOAD_HELP } from '../../uploads/assetUploadPurpose'
import { AssetImageFieldCard } from '../../uploads/components/AssetImageFieldCard'

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



function toCreatePayload(values: StampDesignFormValues, existingSortOrder?: number) {

  return {

    campaignId: values.campaignId,

    stationId: values.stationId,

    name: values.name.trim(),

    description: values.description?.trim() || undefined,

    imageUrl: values.imageUrl.trim(),

    previewImageUrl: values.previewImageUrl?.trim() || undefined,

    rarity: values.rarity,

    status: values.status,

    // Ordering is managed via Stamp Designs reorder drawer — preserve on edit, omit on create.

    sortOrder: existingSortOrder,

  }

}



function toUpdatePayload(values: StampDesignFormValues, existingSortOrder?: number) {

  return toCreatePayload(values, existingSortOrder)

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

      })

    } else {

      reset(defaultStampDesignFormValues)

    }

  }, [open, stampDesign, reset])



  const onSubmit = handleSubmit(async (values) => {

    const existingSortOrder = isEdit ? stampDesign?.sortOrder : undefined

    if (isEdit && stampDesign) {

      await updateMutation.mutateAsync({

        id: stampDesign.id,

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

      title={isEdit ? 'Edit Stamp Design' : 'Create Stamp Design'}

      description={

        isEdit

          ? 'Update campaign-specific collectible artwork for this station.'

          : 'Add campaign-specific collectible artwork for a station.'

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

        <DrawerSectionCard

          icon={MapPin}

          title="Placement"

          description="Link this collectible stamp to a campaign and station."

        >

          <FormField

            label="Campaign"

            htmlFor="campaignId"

            required

            error={errors.campaignId?.message}

          >

            <Select id="campaignId" {...register('campaignId')}>

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

            </Select>

          </FormField>



          <FormField label="Station" htmlFor="stationId" required error={errors.stationId?.message}>

            <Select id="stationId" {...register('stationId')}>

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

            </Select>

          </FormField>

        </DrawerSectionCard>



        <DrawerSectionCard

          icon={FileText}

          title="Details"

          description="Display name and supporting copy shown to collectors."

        >

          <FormField label="Name" htmlFor="name" required error={errors.name?.message}>

            <Input id="name" {...register('name')} />

          </FormField>



          <FormField label="Description" htmlFor="description" error={errors.description?.message}>

            <Textarea id="description" rows={3} {...register('description')} />

          </FormField>

        </DrawerSectionCard>



        <DrawerSectionCard

          icon={Image}

          title="Campaign-specific stamp artwork"

          description="Artwork for the stamp book and collection screens."

        > 

          <Controller

            name="imageUrl"

            control={control}

            render={({ field }) => (

              <AssetImageFieldCard
                id="imageUrl"
                title="Main stamp artwork"
                required
                value={field.value}
                onChange={field.onChange}
                error={errors.imageUrl?.message}
                formDirty={isDirty}
                previewSize="lg"
                objectFit="contain"
                help={ASSET_UPLOAD_HELP.stampArtwork}
                purpose="STAMP_ARTWORK"
                clearable
              />

            )}

          />



          <Controller

            name="previewImageUrl"

            control={control}

            render={({ field }) => (

              <AssetImageFieldCard
                id="previewImageUrl"
                title="Thumbnail / stamp preview"
                value={field.value ?? ''}
                onChange={field.onChange}
                error={errors.previewImageUrl?.message}
                formDirty={isDirty}
                previewSize="lg"
                objectFit="contain"
                help={ASSET_UPLOAD_HELP.stampPreview}
                purpose="STAMP_PREVIEW"
                clearable
              />

            )}

          />

        </DrawerSectionCard>



        <DrawerSectionCard

          icon={Send}

          title="Publishing"

          description="Rarity and visibility. List order is managed via Reorder Stamp Designs."

        >

          <div className="grid gap-4 sm:grid-cols-2">

            <FormField label="Rarity" htmlFor="rarity" error={errors.rarity?.message}>

              <Select id="rarity" {...register('rarity')}>

                <option value="COMMON">Common</option>

                <option value="RARE">Rare</option>

                <option value="EPIC">Epic</option>

                <option value="LEGENDARY">Legendary</option>

              </Select>

            </FormField>



            <FormField label="Status" htmlFor="status" error={errors.status?.message}>

              <Select id="status" {...register('status')}>

                <option value="DRAFT">Draft</option>

                <option value="ACTIVE">Active</option>

                <option value="INACTIVE">Inactive</option>

              </Select>

            </FormField>

          </div>

        </DrawerSectionCard>

      </form>

    </FormDrawer>

  )

}


