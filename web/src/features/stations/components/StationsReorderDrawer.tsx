import { useMemo } from 'react'
import { ReorderDrawer, type ReorderItem } from '../../../components/ui/ReorderDrawer'
import { useReorderStations, useStationsList } from '../hooks'

interface StationsReorderDrawerProps {
  open: boolean
  lineId: string
  lineLabel?: string
  onClose: () => void
}

export function StationsReorderDrawer({
  open,
  lineId,
  lineLabel,
  onClose,
}: StationsReorderDrawerProps) {
  const { data, isLoading } = useStationsList(
    {
      page: 0,
      size: 500,
      lineId,
      sort: 'sortOrder,asc',
    },
    { enabled: open && Boolean(lineId) },
  )
  const reorderMutation = useReorderStations()

  const items: ReorderItem[] = useMemo(
    () =>
      (data?.content ?? []).map((station) => ({
        id: station.id,
        label: station.displayName || station.name,
        secondary: station.code,
      })),
    [data?.content],
  )

  const truncated =
    Boolean(data) && (data?.totalElements ?? 0) > (data?.content?.length ?? 0)

  return (
    <ReorderDrawer
      open={open}
      title="Reorder stations"
      description={
        lineLabel
          ? `Drag stations into order for ${lineLabel}. Saving renumbers every station on this line (including draft and inactive).`
          : 'Drag stations into order for the selected line. Saving renumbers every station on this line.'
      }
      items={items}
      isLoading={isLoading}
      isSubmitting={reorderMutation.isPending}
      saveDisabled={truncated}
      error={
        truncated
          ? new Error(
              'Too many stations to reorder in one request. Reduce the line size or contact engineering.',
            )
          : reorderMutation.error
      }
      emptyMessage="No stations on this line to reorder."
      onClose={() => {
        reorderMutation.reset()
        onClose()
      }}
      onSave={async (orderedIds) => {
        if (truncated) {
          return
        }
        await reorderMutation.mutateAsync({ lineId, orderedIds })
        reorderMutation.reset()
        onClose()
      }}
    />
  )
}
