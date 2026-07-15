import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { AlertTriangle, KeyRound, Shield } from 'lucide-react'
import { FormDrawer } from '../../../components/ui/FormDrawer'
import { DrawerSectionCard } from '../../../components/ui/DrawerSectionCard'
import { FormField, Input, Select } from '../../../components/ui/FormField'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { SecretField } from '../../../components/ui/SecretField'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { LoadingState } from '../../../components/ui/LoadingState'
import { ErrorState } from '../../../components/ui/ErrorState'
import { formatDateTime } from '../../../lib/formatting/date'
import { scanKeyReadinessStatus } from '../../../lib/metro/readiness'
import { isForbiddenError, isNotFoundError } from '../../../lib/api/errors'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { scanKeysFormSchema, type ScanKeysFormValues } from '../schemas'
import { useStationDetail, useUpdateScanKeys } from '../hooks'

interface ScanKeyDrawerProps {
  open: boolean
  stationId: string | null
  onClose: () => void
}

export function ScanKeyDrawer({ open, stationId, onClose }: ScanKeyDrawerProps) {
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [pendingValues, setPendingValues] = useState<ScanKeysFormValues | null>(null)

  const { data: station, isLoading, error } = useStationDetail(open ? stationId ?? undefined : undefined)
  const updateMutation = useUpdateScanKeys()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ScanKeysFormValues>({
    resolver: zodResolver(scanKeysFormSchema),
    defaultValues: { nfcTagId: '', qrCodeValue: '', scanKeyStatus: 'ACTIVE' },
  })

  useEffect(() => {
    if (station) {
      reset({
        nfcTagId: station.nfcTagId ?? '',
        qrCodeValue: station.qrCodeValue ?? '',
        scanKeyStatus: station.scanKeyStatus ?? 'ACTIVE',
      })
    }
  }, [station, reset])

  const onSubmit = handleSubmit((values) => {
    setPendingValues(values)
    setConfirmOpen(true)
  })

  async function confirmSave() {
    if (!stationId || !pendingValues) return
    await updateMutation.mutateAsync({
      id: stationId,
      body: {
        nfcTagId: pendingValues.nfcTagId?.trim() || undefined,
        qrCodeValue: pendingValues.qrCodeValue?.trim() || undefined,
        scanKeyStatus: pendingValues.scanKeyStatus,
      },
    })
    setConfirmOpen(false)
    setPendingValues(null)
    onClose()
  }

  function renderContent() {
    if (isLoading) {
      return <LoadingState message="Loading scan keys…" />
    }
    if (error) {
      if (isForbiddenError(error)) {
        return <PermissionDeniedState title="Scan key access denied" />
      }
      if (isNotFoundError(error)) {
        return <ErrorState title="Station not found" error={error} />
      }
      return <ErrorState error={error} />
    }
    if (!station) {
      return null
    }

    const readiness = scanKeyReadinessStatus(station)

    return (
      <form id="scan-keys-form" className="space-y-4" onSubmit={onSubmit} noValidate>
        <div className="flex items-center gap-2 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
          <AlertTriangle className="h-4 w-4 shrink-0" />
          Scan keys are sensitive. Do not expose them in screenshots or shared logs.
        </div>

        <DrawerSectionCard
          icon={Shield}
          title="Current keys"
          description="Masked values currently assigned to this station."
        >
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Scan key status:</span>
            <StatusBadge status={readiness} />
          </div>

          <div className="space-y-2">
            <p className="text-sm font-medium text-foreground">NFC Tag ID</p>
            <SecretField value={station.nfcTagId} ariaLabel="NFC tag ID" />
          </div>

          <div className="space-y-2">
            <p className="text-sm font-medium text-foreground">QR Code Value</p>
            <SecretField value={station.qrCodeValue} ariaLabel="QR code value" />
          </div>

          <div className="grid grid-cols-2 gap-3 text-xs text-muted-foreground">
            <div>
              <p className="font-medium uppercase tracking-wide">Last QR rotated</p>
              <p className="mt-0.5 text-foreground">{formatDateTime(station.lastQrRotatedAt)}</p>
            </div>
            <div>
              <p className="font-medium uppercase tracking-wide">Last scan key update</p>
              <p className="mt-0.5 text-foreground">{formatDateTime(station.lastScanKeyUpdatedAt)}</p>
            </div>
          </div>
        </DrawerSectionCard>

        <DrawerSectionCard
          icon={KeyRound}
          title="Update scan keys"
          description="Changes affect live station collection."
        >
          <FormField label="NFC Tag ID" htmlFor="nfcTagId" error={errors.nfcTagId?.message}>
            <Input id="nfcTagId" disabled={updateMutation.isPending} {...register('nfcTagId')} />
          </FormField>

          <FormField label="QR Code Value" htmlFor="qrCodeValue" error={errors.qrCodeValue?.message}>
            <Input id="qrCodeValue" disabled={updateMutation.isPending} {...register('qrCodeValue')} />
          </FormField>

          <FormField label="Scan key status" htmlFor="scanKeyStatus" error={errors.scanKeyStatus?.message}>
            <Select
              id="scanKeyStatus"
              disabled={updateMutation.isPending}
              {...register('scanKeyStatus')}
            >
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </Select>
          </FormField>
        </DrawerSectionCard>
      </form>
    )
  }

  return (
    <>
      <FormDrawer
        open={open}
        title="Scan Key Management"
        description={station ? `${station.name} (${station.code})` : 'Station scan keys'}
        formId="scan-keys-form"
        isSubmitting={updateMutation.isPending}
        isDirty={isDirty}
        saveLabel="Update scan keys"
        saveDisabled={isLoading || Boolean(error)}
        error={updateMutation.error}
        onClose={onClose}
      >
        {renderContent()}
      </FormDrawer>

      <ConfirmDialog
        open={confirmOpen}
        variant="warning"
        title="Update scan keys?"
        description="Changing scan keys affects live station collection. Ensure physical tags and QR codes are updated accordingly."
        confirmLabel="Save scan keys"
        loading={updateMutation.isPending}
        onCancel={() => {
          setConfirmOpen(false)
          setPendingValues(null)
        }}
        onConfirm={() => void confirmSave()}
      />
    </>
  )
}
