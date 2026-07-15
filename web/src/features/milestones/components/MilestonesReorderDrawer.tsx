import { useMemo } from 'react'
import { ReorderDrawer, type ReorderItem } from '../../../components/ui/ReorderDrawer'
import { useMilestones, useReorderMilestones } from '../hooks'

interface MilestonesReorderDrawerProps {
  open: boolean
  campaignId: string
  campaignLabel?: string
  onClose: () => void
}

export function MilestonesReorderDrawer({
  open,
  campaignId,
  campaignLabel,
  onClose,
}: MilestonesReorderDrawerProps) {
  // Full campaign scope (no status filter) so reorder matches backend membership.
  const { data, isLoading } = useMilestones(
    { page: 0, size: 500, campaignId },
    { enabled: open && Boolean(campaignId) },
  )
  const reorderMutation = useReorderMilestones()

  const items: ReorderItem[] = useMemo(
    () =>
      (data?.content ?? []).map((milestone) => ({
        id: milestone.id,
        label: milestone.name,
        secondary: milestone.code,
      })),
    [data?.content],
  )

  const truncated = Boolean(data) && (data?.totalElements ?? 0) > (data?.content?.length ?? 0)

  return (
    <ReorderDrawer
      open={open}
      title="Reorder milestones"
      description={
        campaignLabel
          ? `Drag milestones into display order for ${campaignLabel}. Saving renumbers every milestone in this campaign.`
          : 'Drag milestones into display order for the selected campaign.'
      }
      items={items}
      isLoading={isLoading}
      isSubmitting={reorderMutation.isPending}
      saveDisabled={truncated}
      error={
        truncated
          ? new Error('Too many milestones to reorder in one request.')
          : reorderMutation.error
      }
      emptyMessage="No milestones in this campaign to reorder."
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
