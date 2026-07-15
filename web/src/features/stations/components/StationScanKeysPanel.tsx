import { useState } from 'react'
import { createPortal } from 'react-dom'
import { Check, Copy, Plus, RefreshCw } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Button } from '../../../components/ui/Button'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/Card'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { FormField, Input } from '../../../components/ui/FormField'
import { LoadingState } from '../../../components/ui/LoadingState'
import { StatusBadge } from '../../../components/ui/StatusBadge'
import { ApiErrorAlert } from '../../../components/feedback/ApiErrorAlert'
import { formatDateTime } from '../../../lib/formatting/date'
import { useCopyToClipboard } from '../../../lib/utils/useCopyToClipboard'
import type { StationScanKeyCreatedResponse, StationScanKeyResponse } from '../../../types/stations'
import {
  useActivateStationScanKey,
  useCreateStationScanKey,
  useMarkLostStationScanKey,
  useRevokeStationScanKey,
  useStationScanKeys,
} from '../hooks'
import {
  createStationScanKeySchema,
  type CreateStationScanKeyFormValues,
} from '../schemas'

interface StationScanKeysPanelProps {
  stationId: string
}

function MetaRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex justify-between gap-3 text-xs">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-right font-medium text-foreground">{value ?? '—'}</span>
    </div>
  )
}

function ScanKeyCard({
  scanKey,
  stationId,
  busy,
}: {
  scanKey: StationScanKeyResponse
  stationId: string
  busy: boolean
}) {
  const activateMutation = useActivateStationScanKey(stationId)
  const revokeMutation = useRevokeStationScanKey(stationId)
  const markLostMutation = useMarkLostStationScanKey(stationId)
  const [revokeOpen, setRevokeOpen] = useState(false)
  const [lostOpen, setLostOpen] = useState(false)
  const [revokeReason, setRevokeReason] = useState('')

  const canActivate = scanKey.status === 'DRAFT' || scanKey.status === 'INACTIVE'
  const canRevokeOrLose =
    scanKey.status === 'DRAFT' ||
    scanKey.status === 'ACTIVE' ||
    scanKey.status === 'INACTIVE'

  return (
    <div className="rounded-md border border-border bg-card p-3 space-y-2">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex flex-wrap items-center gap-2">
          <StatusBadge status={scanKey.scanType} />
          <StatusBadge status={scanKey.status} />
          <span className="font-mono text-xs text-muted-foreground">{scanKey.keyPrefix}…</span>
        </div>
        <div className="flex flex-wrap gap-1">
          {canActivate ? (
            <Button
              size="sm"
              variant="secondary"
              disabled={busy || activateMutation.isPending}
              onClick={() => void activateMutation.mutateAsync(scanKey.id)}
            >
              Activate
            </Button>
          ) : null}
          {canRevokeOrLose ? (
            <>
              <Button
                size="sm"
                variant="outline"
                disabled={busy || revokeMutation.isPending}
                onClick={() => setRevokeOpen(true)}
              >
                Revoke
              </Button>
              <Button
                size="sm"
                variant="danger"
                disabled={busy || markLostMutation.isPending}
                onClick={() => setLostOpen(true)}
              >
                Mark lost
              </Button>
            </>
          ) : null}
        </div>
      </div>

      <div className="grid gap-1 sm:grid-cols-2">
        <MetaRow label="Label" value={scanKey.label ?? '—'} />
        <MetaRow label="Placement" value={scanKey.placementNote ?? '—'} />
        <MetaRow label="Activated" value={formatDateTime(scanKey.activatedAt)} />
        <MetaRow label="Revoked" value={formatDateTime(scanKey.revokedAt)} />
        <MetaRow label="Last seen" value={formatDateTime(scanKey.lastSeenAt)} />
        <MetaRow label="Install verified" value={formatDateTime(scanKey.lastInstallVerifiedAt)} />
        <MetaRow
          label="Installed GPS"
          value={
            scanKey.installedLatitude != null && scanKey.installedLongitude != null
              ? `${scanKey.installedLatitude}, ${scanKey.installedLongitude}`
              : '—'
          }
        />
        <MetaRow
          label="Install accuracy"
          value={
            scanKey.installedAccuracyMeters != null
              ? `${scanKey.installedAccuracyMeters}m`
              : '—'
          }
        />
        <MetaRow label="Install device" value={scanKey.installedDevicePlatform ?? '—'} />
        <MetaRow label="Install app" value={scanKey.installedAppVersion ?? '—'} />
      </div>

      {activateMutation.isError ? <ApiErrorAlert error={activateMutation.error} /> : null}
      {revokeMutation.isError ? <ApiErrorAlert error={revokeMutation.error} /> : null}
      {markLostMutation.isError ? <ApiErrorAlert error={markLostMutation.error} /> : null}

      <ConfirmDialog
        open={revokeOpen}
        variant="warning"
        title="Revoke scan key?"
        description={
          <div className="space-y-2">
            <p>
              Key <span className="font-mono">{scanKey.keyPrefix}…</span> will stop accepting
              scans.
            </p>
            <FormField label="Reason (optional)" htmlFor={`revoke-reason-${scanKey.id}`}>
              <Input
                id={`revoke-reason-${scanKey.id}`}
                value={revokeReason}
                onChange={(e) => setRevokeReason(e.target.value)}
                maxLength={255}
                placeholder="lost / damaged / rotated"
              />
            </FormField>
          </div>
        }
        confirmLabel="Revoke"
        loading={revokeMutation.isPending}
        onCancel={() => {
          setRevokeOpen(false)
          setRevokeReason('')
        }}
        onConfirm={async () => {
          await revokeMutation.mutateAsync({
            id: scanKey.id,
            body: { reason: revokeReason.trim() || undefined },
          })
          setRevokeOpen(false)
          setRevokeReason('')
        }}
      />

      <ConfirmDialog
        open={lostOpen}
        variant="danger"
        title="Mark scan key as lost?"
        description={
          <>
            Key <span className="font-mono">{scanKey.keyPrefix}…</span> will be marked LOST and
            cannot be used for collection.
          </>
        }
        confirmLabel="Mark lost"
        loading={markLostMutation.isPending}
        onCancel={() => setLostOpen(false)}
        onConfirm={async () => {
          await markLostMutation.mutateAsync(scanKey.id)
          setLostOpen(false)
        }}
      />
    </div>
  )
}

function OneTimePayloadModal({
  created,
  onClose,
}: {
  created: StationScanKeyCreatedResponse
  onClose: () => void
}) {
  const { copied, copy } = useCopyToClipboard()

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="payload-once-title"
        className="w-full max-w-lg rounded-lg border border-border bg-card p-5 shadow-lg"
      >
        <h3 id="payload-once-title" className="text-lg font-semibold text-foreground">
          Payload to write
        </h3>
        <p className="mt-2 text-sm text-amber-800 bg-amber-50 border border-amber-200 rounded-md px-3 py-2">
          This payload is shown once. Copy or write it now. It will not be shown again after you
          close this dialog.
        </p>
        <div className="mt-4 rounded-md border border-border bg-secondary p-3">
          <p className="break-all font-mono text-sm text-foreground">{created.payloadToWrite}</p>
        </div>
        <div className="mt-4 flex flex-wrap justify-end gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => void copy(created.payloadToWrite)}
          >
            {copied ? <Check className="h-4 w-4 text-emerald-600" /> : <Copy className="h-4 w-4" />}
            {copied ? 'Copied' : 'Copy payload'}
          </Button>
          <Button size="sm" onClick={onClose}>
            Done
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  )
}

export function StationScanKeysPanel({ stationId }: StationScanKeysPanelProps) {
  const { data: keys, isLoading, error, refetch, isFetching } = useStationScanKeys(stationId)
  const createMutation = useCreateStationScanKey(stationId)
  const [generateOpen, setGenerateOpen] = useState(false)
  const [oneTimePayload, setOneTimePayload] = useState<StationScanKeyCreatedResponse | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateStationScanKeyFormValues>({
    resolver: zodResolver(createStationScanKeySchema),
    defaultValues: { scanType: 'NFC', label: '', placementNote: '' },
  })

  const onGenerate = handleSubmit(async (values) => {
    const created = await createMutation.mutateAsync({
      scanType: values.scanType,
      label: values.label?.trim() || undefined,
      placementNote: values.placementNote?.trim() || undefined,
    })
    setGenerateOpen(false)
    reset({ scanType: 'NFC', label: '', placementNote: '' })
    setOneTimePayload(created)
  })

  return (
    <Card className="lg:col-span-2">
      <CardHeader className="flex flex-row items-center justify-between gap-2 space-y-0">
        <CardTitle>NFC / QR Scan Keys</CardTitle>
        <div className="flex flex-wrap gap-2">
          <Button
            size="sm"
            variant="outline"
            disabled={isFetching}
            onClick={() => void refetch()}
          >
            <RefreshCw className={`h-4 w-4 ${isFetching ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Button size="sm" onClick={() => setGenerateOpen(true)}>
            <Plus className="h-4 w-4" />
            Generate NFC Key
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-3">
        <p className="text-xs text-muted-foreground">
          Production keys store only a hash. The write payload is shown once at generation. Multiple
          ACTIVE NFC tags per station are allowed.
        </p>

        {isLoading ? <LoadingState message="Loading scan keys…" /> : null}
        {error ? <ApiErrorAlert error={error} /> : null}

        {!isLoading && !error && (keys?.length ?? 0) === 0 ? (
          <p className="text-sm text-muted-foreground">No scan keys yet. Generate an NFC key to start.</p>
        ) : null}

        <div className="space-y-3">
          {keys?.map((scanKey) => (
            <ScanKeyCard
              key={scanKey.id}
              scanKey={scanKey}
              stationId={stationId}
              busy={createMutation.isPending}
            />
          ))}
        </div>
      </CardContent>

      <ConfirmDialog
        open={generateOpen}
        title="Generate NFC scan key"
        description={
          <form id="generate-scan-key-form" className="space-y-3" onSubmit={(e) => void onGenerate(e)}>
            <FormField label="Scan type" htmlFor="scanType" error={errors.scanType?.message}>
              <select
                id="scanType"
                className="flex h-9 w-full rounded-md border border-border bg-background px-3 text-sm"
                disabled={createMutation.isPending}
                {...register('scanType')}
              >
                <option value="NFC">NFC</option>
                <option value="QR_STATIC">QR_STATIC</option>
                <option value="QR_DYNAMIC_PLACEHOLDER">QR_DYNAMIC_PLACEHOLDER</option>
              </select>
            </FormField>
            <FormField label="Label" htmlFor="label" error={errors.label?.message}>
              <Input
                id="label"
                maxLength={100}
                placeholder="Gate A - Pillar 02"
                disabled={createMutation.isPending}
                {...register('label')}
              />
            </FormField>
            <FormField
              label="Placement note"
              htmlFor="placementNote"
              error={errors.placementNote?.message}
            >
              <Input
                id="placementNote"
                maxLength={255}
                placeholder="Near ticket gate"
                disabled={createMutation.isPending}
                {...register('placementNote')}
              />
            </FormField>
            {createMutation.isError ? <ApiErrorAlert error={createMutation.error} /> : null}
          </form>
        }
        confirmLabel="Generate"
        loading={createMutation.isPending}
        onCancel={() => {
          setGenerateOpen(false)
          reset({ scanType: 'NFC', label: '', placementNote: '' })
          createMutation.reset()
        }}
        onConfirm={() => {
          void onGenerate()
        }}
      />

      {oneTimePayload ? (
        <OneTimePayloadModal
          created={oneTimePayload}
          onClose={() => setOneTimePayload(null)}
        />
      ) : null}
    </Card>
  )
}
