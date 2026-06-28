import { useMemo, useState } from 'react'
import { cn } from '../../../lib/utils/cn'
import { PermissionDeniedState } from '../../../components/ui/PermissionDeniedState'
import { isForbiddenError } from '../../../lib/api/errors'
import { useMilestones } from '../../milestones/hooks'
import { usePartners } from '../../partners/hooks'
import { RewardsTab } from '../components/RewardsTab'
import { VoucherPoolTab } from '../../vouchers/components/VoucherPoolTab'
import { ImportVouchersTab } from '../../vouchers/components/ImportVouchersTab'
import { useRewards } from '../hooks'
import { useVouchers } from '../../vouchers/hooks'

type RewardsPageTab = 'rewards' | 'voucher-pool' | 'import'

const TABS: { id: RewardsPageTab; label: string }[] = [
  { id: 'rewards', label: 'Rewards' },
  { id: 'voucher-pool', label: 'Voucher Pool' },
  { id: 'import', label: 'Import Vouchers' },
]

export function RewardsPage() {
  const [activeTab, setActiveTab] = useState<RewardsPageTab>('rewards')

  const { data: milestonesPage, error: milestonesError } = useMilestones({ page: 0, size: 500 })
  const { data: partnersPage, error: partnersError } = usePartners({ page: 0, size: 500 })

  const { error: rewardsError } = useRewards({ page: 0, size: 1 })
  const { error: vouchersError } = useVouchers({ page: 0, size: 1 })

  const milestones = useMemo(() => milestonesPage?.content ?? [], [milestonesPage?.content])
  const partners = useMemo(() => partnersPage?.content ?? [], [partnersPage?.content])

  const forbidden =
    (rewardsError && isForbiddenError(rewardsError)) ||
    (vouchersError && isForbiddenError(vouchersError)) ||
    (milestonesError && isForbiddenError(milestonesError)) ||
    (partnersError && isForbiddenError(partnersError))

  if (forbidden) {
    return <PermissionDeniedState title="Rewards & voucher pool access denied" />
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-semibold text-foreground">Rewards &amp; Voucher Pool</h2>
        <p className="text-sm text-muted-foreground">
          Manage rewards, voucher inventory, bulk imports, and disable flows with masked voucher
          codes.
        </p>
      </div>

      <div className="border-b border-border">
        <nav className="-mb-px flex gap-1" aria-label="Rewards sections">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={cn(
                'border-b-2 px-4 py-2.5 text-xs font-semibold uppercase tracking-wide transition-colors',
                activeTab === tab.id
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:border-border hover:text-foreground',
              )}
              aria-current={activeTab === tab.id ? 'page' : undefined}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {activeTab === 'rewards' ? (
        <RewardsTab milestones={milestones} partners={partners} />
      ) : null}

      {activeTab === 'voucher-pool' ? <VoucherPoolTab milestones={milestones} /> : null}

      {activeTab === 'import' ? <ImportVouchersTab milestones={milestones} /> : null}
    </div>
  )
}
