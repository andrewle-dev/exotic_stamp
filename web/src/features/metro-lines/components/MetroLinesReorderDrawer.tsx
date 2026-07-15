import { useMemo } from 'react'
import { ReorderDrawer, type ReorderItem } from '../../../components/ui/ReorderDrawer'
import { useMetroLinesList, useReorderMetroLines } from '../hooks'

interface MetroLinesReorderDrawerProps {
  open: boolean
  onClose: () => void
}

export function MetroLinesReorderDrawer({ open, onClose }: MetroLinesReorderDrawerProps) {
  const { data, isLoading } = useMetroLinesList(
    {
      page: 0,
      size: 200,
      sort: 'sortOrder,asc',
    },
    { enabled: open },
  )
  const reorderMutation = useReorderMetroLines()

  const items: ReorderItem[] = useMemo(
    () =>
      (data?.content ?? []).map((line) => ({
        id: line.id,
        label: line.displayName || line.name,
        secondary: line.code,
        accentColor: line.colorHex,
      })),
    [data?.content],
  )

  const truncated = Boolean(data) && (data?.totalElements ?? 0) > (data?.content?.length ?? 0)

  return (
    <ReorderDrawer
      open={open}
      title="Reorder metro lines"
      description="Drag lines into the display order used across admin and public line lists. Saving renumbers every line."
      items={items}
      isLoading={isLoading}
      isSubmitting={reorderMutation.isPending}
      saveDisabled={truncated}
      error={
        truncated
          ? new Error(
              'Too many lines to reorder in one request. Contact engineering to raise the page size.',
            )
          : reorderMutation.error
      }
      emptyMessage="No metro lines to reorder."
      onClose={() => {
        reorderMutation.reset()
        onClose()
      }}
      onSave={async (orderedIds) => {
        if (truncated) {
          return
        }
        await reorderMutation.mutateAsync({ orderedIds })
        reorderMutation.reset()
        onClose()
      }}
    />
  )
}
