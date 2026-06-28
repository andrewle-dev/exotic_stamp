import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { FormField, Input } from '../../../components/ui/FormField'
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

function toPayload(values: StationFormValues) {
  return {
    lineId: values.lineId,
    code: values.code.trim(),
    name: values.name.trim(),
    displayName: values.displayName?.trim() || undefined,
    description: values.description?.trim() || undefined,
    address: values.address?.trim() || undefined,
    sortOrder: values.sortOrder,
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
        sortOrder: station.sortOrder ?? 0,
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
    const payload = toPayload(values)
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
      description={isEdit ? `Update ${station?.name}` : 'Add a new metro station'}
      formId="station-form"
      isSubmitting={isSubmitting}
      isDirty={isDirty}
      saveLabel={isEdit ? 'Save changes' : 'Create station'}
      error={mutation.error}
      onClose={onClose}
    >
      <form id="station-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        {!isEdit ? (
          <FormField label="Metro line" htmlFor="lineId" required error={errors.lineId?.message}>
            <select
              id="lineId"
              disabled={isSubmitting}
              className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
              {...register('lineId')}
            >
              <option value="">Select line…</option>
              {lines.map((line) => (
                <option key={line.id} value={line.id}>
                  {line.code} — {line.name}
                </option>
              ))}
            </select>
          </FormField>
        ) : null}

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
          <textarea
            id="description"
            rows={2}
            disabled={isSubmitting}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            {...register('description')}
          />
        </FormField>

        <div className="grid grid-cols-2 gap-4">
          <FormField label="Latitude" htmlFor="latitude" error={errors.latitude?.message}>
            <Input id="latitude" type="number" step="any" disabled={isSubmitting} {...register('latitude')} />
          </FormField>
          <FormField label="Longitude" htmlFor="longitude" error={errors.longitude?.message}>
            <Input id="longitude" type="number" step="any" disabled={isSubmitting} {...register('longitude')} />
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

        <FormField label="Sort order" htmlFor="sortOrder" error={errors.sortOrder?.message}>
          <Input
            id="sortOrder"
            type="number"
            disabled={isSubmitting}
            {...register('sortOrder', { valueAsNumber: true })}
          />
        </FormField>

        <FormField label="Image URL" htmlFor="imageUrl" error={errors.imageUrl?.message}>
          <Input id="imageUrl" disabled={isSubmitting} {...register('imageUrl')} />
        </FormField>

        <FormField label="Stamp preview URL" htmlFor="stampPreviewUrl" error={errors.stampPreviewUrl?.message}>
          <Input id="stampPreviewUrl" disabled={isSubmitting} {...register('stampPreviewUrl')} />
        </FormField>

        <FormField label="Status" htmlFor="status" error={errors.status?.message}>
          <select
            id="status"
            disabled={isSubmitting}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm"
            {...register('status')}
          >
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </FormField>
      </form>
    </FormDrawer>
  )
}
