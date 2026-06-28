import { useState } from 'react'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import type { StationDetailResponse } from '../../../types/stations'
import { useRotateStationQr } from '../hooks'

interface RotateQrDialogProps {
  open: boolean
  station: StationDetailResponse | null
  onClose: () => void
  onSuccess?: (message: string) => void
}

export function RotateQrDialog({ open, station, onClose, onSuccess }: RotateQrDialogProps) {
  const rotateMutation = useRotateStationQr()
  const [localError, setLocalError] = useState<unknown>(null)

  async function handleConfirm() {
    if (!station) return
    setLocalError(null)
    try {
      await rotateMutation.mutateAsync(station.id)
      onSuccess?.(`QR code rotated for ${station.name}.`)
      onClose()
    } catch (error) {
      setLocalError(error)
    }
  }

  return (
    <ConfirmDialog
      open={open}
      variant="danger"
      title="Rotate QR code?"
      description={
        station ? (
          <div className="space-y-2">
            <p>
              You are about to rotate the QR code for <strong>{station.name}</strong> (
              <span className="font-mono">{station.code}</span>).
            </p>
            <p className="font-medium text-accent-foreground">
              The old QR code will stop working immediately. Printed materials must be replaced.
            </p>
            {localError ? <ApiErrorAlert error={localError} /> : null}
          </div>
        ) : null
      }
      requireText={station?.code}
      requireTextLabel={
        station ? (
          <>
            Type <span className="font-mono font-semibold text-foreground">{station.code}</span> to
            confirm rotation
          </>
        ) : undefined
      }
      confirmLabel="Rotate QR"
      loading={rotateMutation.isPending}
      onCancel={() => {
        setLocalError(null)
        onClose()
      }}
      onConfirm={() => void handleConfirm()}
    />
  )
}
