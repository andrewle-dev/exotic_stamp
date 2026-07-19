import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { FileText, Image, MapPin, Send } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input, Select, Textarea } from '../../../components/ui/FormField'
import { ASSET_UPLOAD_HELP } from '../../uploads/assetUploadPurpose'
import { AssetImageFieldCard } from '../../uploads/components/AssetImageFieldCard'
import type { StationResponse } from '../../../types/stations'
import type { LineResponse } from '../../../types/metro-lines'
import {
  defaultStationFormValues,
  parseOptionalNumber,
  stationFormSchema,
  type StationFormValues,
} from '../schemas'
import { useCreateStation, useUpdateStation } from '../hooks'

interface StationFormDrawerProps {
  open: boolean
  station?: StationResponse | null
  lines: LineResponse[]
  onClose: () => void
  onSuccess?: () => void
}

function toPayload(values: StationFormValues, existingSortOrder?: number) {
  return {
    lineId: values.lineId,
    code: values.code.trim(),
    name: values.name.trim(),
    displayName: values.displayName?.trim() || undefined,
    description: values.description?.trim() || undefined,
    address: values.address?.trim() || undefined,
    // Ordering is managed via Stations reorder drawer — preserve on edit, default on create.
    sortOrder: existingSortOrder ?? 0,
    latitude: parseOptionalNumber(values.latitude),
    longitude: parseOptionalNumber(values.longitude),
    zoneRadiusMeters: parseOptionalNumber(values.zoneRadiusMeters),
    imageUrl: values.imageUrl?.trim() || undefined,
    stampPreviewUrl: values.stampPreviewUrl?.trim() || undefined,
    status: values.status,
  }
}

export function StationFormDrawer({
  open,
  station,
  lines,
  onClose,
  onSuccess,
}: StationFormDrawerProps) {
  const isEdit = Boolean(station)
  const createMutation = useCreateStation()
  const updateMutation = useUpdateStation()
  const mutation = isEdit ? updateMutation : createMutation
  const isSubmitting = mutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isDirty },
  } = useForm<StationFormValues>({
    resolver: zodResolver(stationFormSchema),
    defaultValues: defaultStationFormValues,
  })

  useEffect(() => {
    if (!open) return
    if (station) {
      reset({
        lineId: station.lineId,
        code: station.code,
        name: station.name,
        displayName: station.displayName ?? '',
        description: station.description ?? '',
        address: station.address ?? '',
        latitude: station.latitude != null ? String(station.latitude) : '',
        longitude: station.longitude != null ? String(station.longitude) : '',
        zoneRadiusMeters:
          station.zoneRadiusMeters != null ? String(station.zoneRadiusMeters) : '',
        imageUrl: station.imageUrl ?? '',
        stampPreviewUrl: station.stampPreviewUrl ?? '',
        status: station.status,
      })
    } else {
      reset({
        ...defaultStationFormValues,
        lineId: lines[0]?.id ?? '',
      })
    }
  }, [open, station, lines, reset])

  const onSubmit = handleSubmit(async (values) => {
    const payload = toPayload(values, isEdit ? station?.sortOrder : undefined)
    if (isEdit && station) {
      const { lineId, ...updateBody } = payload
      void lineId
      await updateMutation.mutateAsync({ id: station.id, body: updateBody })
    } else {
      await createMutation.mutateAsync(payload)
    }
    onSuccess?.()
    onClose()
  })

  return (
    <FormDrawer
      open={open}
      title={isEdit ? 'Edit Station' : 'Create Station'}
      description={
        isEdit
          ? 'Update station profile, location metadata, and discovery media.'
          : 'Add a metro station profile with location metadata and discovery media.'
      }
      formId="station-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create station'}
      error={mutation.error}
      onClose={onClose}
      width="lg"
    >
      <form id="station-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <DrawerSectionCard
          icon={MapPin}
          title="Placement"
          description="Line assignment and location coordinates."
        >
          {!isEdit ? (
            <FormField label="Metro line" htmlFor="lineId" required error={errors.lineId?.message}>
              <Select id="lineId" disabled={isSubmitting} {...register('lineId')}>
                <option value="">Select line…</option>
                {lines.map((line) => (
                  <option key={line.id} value={line.id}>
                    {line.code} — {line.name}
                  </option>
                ))}
              </Select>
            </FormField>
          ) : null}

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Latitude" htmlFor="latitude" error={errors.latitude?.message}>
              <Input
                id="latitude"
                type="number"
                step="any"
                disabled={isSubmitting}
                {...register('latitude')}
              />
            </FormField>
            <FormField label="Longitude" htmlFor="longitude" error={errors.longitude?.message}>
              <Input
                id="longitude"
                type="number"
                step="any"
                disabled={isSubmitting}
                {...register('longitude')}
              />
            </FormField>
          </div>

          <FormField
            label="Zone radius (meters)"
            htmlFor="zoneRadiusMeters"
            error={errors.zoneRadiusMeters?.message}
          >
            <Input
              id="zoneRadiusMeters"
              type="number"
              disabled={isSubmitting}
              {...register('zoneRadiusMeters')}
            />
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={FileText}
          title="Details"
          description="Station identity and address information."
        >
          <FormField label="Code" htmlFor="code" required error={errors.code?.message}>
            <Input id="code" disabled={isSubmitting} {...register('code')} />
          </FormField>

          <FormField label="Name" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" disabled={isSubmitting} {...register('name')} />
          </FormField>

          <FormField label="Display name" htmlFor="displayName" error={errors.displayName?.message}>
            <Input id="displayName" disabled={isSubmitting} {...register('displayName')} />
          </FormField>

          <FormField label="Address" htmlFor="address" error={errors.address?.message}>
            <Input id="address" disabled={isSubmitting} {...register('address')} />
          </FormField>

          <FormField label="Description" htmlFor="description" error={errors.description?.message}>
            <Textarea
              id="description"
              rows={2}
              disabled={isSubmitting}
              {...register('description')}
            />
          </FormField>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Image}
          title="Discovery media"
        >

          <Controller
            name="imageUrl"
            control={control}
            render={({ field }) => (
              <AssetImageFieldCard
                id="imageUrl"
                title="Station cover image"
                value={field.value ?? ''}
                onChange={field.onChange}
                error={errors.imageUrl?.message}
                formDirty={isDirty}
                previewSize="lg"
                objectFit="cover"
                help={ASSET_UPLOAD_HELP.stationCover}
                purpose="STATION_COVER"
                clearable
              />
            )}
          />

          <Controller
            name="stampPreviewUrl"
            control={control}
            render={({ field }) => (
              <AssetImageFieldCard
                id="stampPreviewUrl"
                title="Station card preview"
                value={field.value ?? ''}
                onChange={field.onChange}
                error={errors.stampPreviewUrl?.message}
                formDirty={isDirty}
                previewSize="lg"
                objectFit="cover"
                help={ASSET_UPLOAD_HELP.stationCard}
                purpose="STATION_CARD"
                clearable
              />
            )}
          />
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={Send}
          title="Publishing"
          description="Visibility status. List order is managed via Reorder Stations."
        >
          <FormField label="Status" htmlFor="status" error={errors.status?.message}>
            <Select id="status" disabled={isSubmitting} {...register('status')}>
              <option value="DRAFT">Draft</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </Select>
          </FormField>
        </DrawerSectionCard>
      </form>
    </FormDrawer>
  )
}
