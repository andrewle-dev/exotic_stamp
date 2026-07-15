import { useMemo } from 'react'
import { ReorderDrawer, type ReorderItem } from '../../../components/ui/ReorderDrawer'
import { useReorderStampDesigns, useStampDesigns } from '../hooks'

interface StampDesignsReorderDrawerProps {
  open: boolean
  campaignId: string
  campaignLabel?: string
  onClose: () => void
}

export function StampDesignsReorderDrawer({
  open,
  campaignId,
  campaignLabel,
  onClose,
}: StampDesignsReorderDrawerProps) {
  const { data, isLoading } = useStampDesigns(
    { page: 0, size: 500, campaignId },
    { enabled: open && Boolean(campaignId) },
  )
  const reorderMutation = useReorderStampDesigns()

  const items: ReorderItem[] = useMemo(
    () =>
      (data?.content ?? []).map((design) => ({
        id: design.id,
        label: design.name,
        secondary: design.status ?? undefined,
      })),
    [data?.content],
  )

  const truncated = Boolean(data) && (data?.totalElements ?? 0) > (data?.content?.length ?? 0)

  return (
    <ReorderDrawer
      open={open}
      title="Reorder stamp designs"
      description={
        campaignLabel
          ? `Drag stamp designs into catalog order for ${campaignLabel}. Saving renumbers every design in this campaign.`
          : 'Drag stamp designs into catalog order for the selected campaign.'
      }
      items={items}
      isLoading={isLoading}
      isSubmitting={reorderMutation.isPending}
      saveDisabled={truncated}
      error={
        truncated
          ? new Error('Too many stamp designs to reorder in one request.')
          : reorderMutation.error
      }
      emptyMessage="No stamp designs in this campaign to reorder."
      onClose={() => {
        reorderMutation.reset()
        onClose()
      }}
      onSave={async (orderedIds) => {
        if (truncated) return
        await reorderMutation.mutateAsync({ campaignId, orderedIds })
        reorderMutation.reset()
        onClose()
      }}
    />
  )
}
