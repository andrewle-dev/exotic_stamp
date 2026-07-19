import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_empty_state.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_page_scaffold.dart';
import '../../../../shared/widgets/app_version_footer.dart';
import '../../domain/entities/rewards_overview.dart';
import '../../domain/entities/user_reward.dart';
import '../../domain/usecases/get_rewards_overview_usecase.dart';
import '../cubit/rewards_cubit.dart';
import '../cubit/rewards_state.dart';
import '../widgets/milestone_timeline_item.dart';
import '../widgets/milestones_bottom_sheet.dart';
import '../widgets/reward_voucher_card.dart';
import '../widgets/rewards_progress_card.dart';
import '../widgets/rewards_refresh_listener.dart';
import '../widgets/rewards_screen_header.dart';

class RewardsScreen extends StatefulWidget {
  const RewardsScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final RewardsCubit? cubit;

  @override
  State<RewardsScreen> createState() => _RewardsScreenState();
}

class _RewardsScreenState extends State<RewardsScreen> {
  bool _showHistory = false;

  @override
  Widget build(BuildContext context) {
    final body = _RewardsBody(
      showHistory: _showHistory,
      onHistoryToggle: () => setState(() => _showHistory = !_showHistory),
    );

    if (widget.cubit != null) {
      return BlocProvider<RewardsCubit>.value(
        value: widget.cubit!,
        child: body,
      );
    }

    return BlocProvider(
      create: (_) => RewardsCubit(
        getRewardsOverviewUseCase: GetRewardsOverviewUseCase(
          Injection.instance.rewardsRepository,
        ),
      ),
      child: RewardsRefreshListener(child: body),
    );
  }
}

class _RewardsBody extends StatelessWidget {
  const _RewardsBody({
    required this.showHistory,
    required this.onHistoryToggle,
  });

  final bool showHistory;
  final VoidCallback onHistoryToggle;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: BlocBuilder<RewardsCubit, RewardsState>(
          builder: (context, state) {
            switch (state.status) {
              case RewardsStatus.initial:
              case RewardsStatus.loading:
                return const AppLoadingView(message: 'Loading rewards...');
              case RewardsStatus.failure:
                return AppErrorView(
                  message:
                      state.failure?.message ?? 'Unable to load rewards.',
                  onRetry: () => context.read<RewardsCubit>().load(),
                );
              case RewardsStatus.loaded:
              case RewardsStatus.noRewardsYet:
              case RewardsStatus.rewardPending:
                final overview = state.overview;
                if (overview == null) {
                  return const AppLoadingView();
                }
                return _RewardsContent(
                  overview: overview,
                  showNoRewardsBanner:
                      state.status == RewardsStatus.noRewardsYet,
                  showPendingBanner:
                      state.status == RewardsStatus.rewardPending,
                  showHistory: showHistory,
                  onHistoryToggle: onHistoryToggle,
                );
            }
          },
        ),
      ),
    );
  }
}

class _RewardsContent extends StatelessWidget {
  const _RewardsContent({
    required this.overview,
    required this.showNoRewardsBanner,
    required this.showPendingBanner,
    required this.showHistory,
    required this.onHistoryToggle,
  });

  final RewardsOverview overview;
  final bool showNoRewardsBanner;
  final bool showPendingBanner;
  final bool showHistory;
  final VoidCallback onHistoryToggle;

  UserReward? _rewardForMilestone(String milestoneId) {
    for (final reward in overview.rewards) {
      if (reward.milestoneId == milestoneId &&
          reward.status == UserRewardStatus.available) {
        return reward;
      }
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    final milestones = overview.milestones;
    final availableVouchers = overview.availableVouchers;
    final historyVouchers = overview.historyVouchers;
    final goalTotal = overview.progress?.total ?? 14;

    return RefreshIndicator(
      color: AppColors.primaryBlue,
      onRefresh: () => context.read<RewardsCubit>().refresh(),
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.xl,
          AppSpacing.md,
          AppSpacing.xl,
          AppPageScaffold.shellBottomInset,
        ),
        children: [
          const RewardsScreenHeader(),
          const SizedBox(height: AppSpacing.xl),
          if (overview.partialErrors.isNotEmpty)
            _InfoBanner(
              message: overview.partialErrors.first,
              color: AppColors.redTint,
              textColor: AppColors.accentRed,
            ),
          if (showPendingBanner)
            const _InfoBanner(
              message:
                  'Your reward is almost ready. Pull to refresh.',
              color: AppColors.blueTint,
              textColor: AppColors.primaryBlue,
            ),
          if (overview.progress != null) ...[
            RewardsProgressCard(overview: overview),
            const SizedBox(height: AppSpacing.xxl),
          ],
          if (milestones.isNotEmpty) ...[
            RewardsSectionHeader(
              title: 'Road to $goalTotal',
              actionLabel: 'View Milestones',
              onActionTap: () => showMilestonesBottomSheet(
                context: context,
                milestones: milestones,
                rewardForMilestone: _rewardForMilestone,
                onOpenReward: (reward) => context.push(
                  RouteNames.voucherDetail(reward.id),
                ),
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            ...List.generate(milestones.length, (index) {
              final milestone = milestones[index];
              final linkedReward = _rewardForMilestone(milestone.id);
              return MilestoneTimelineItem(
                milestone: milestone,
                isFirst: index == 0,
                isLast: index == milestones.length - 1,
                onClaimTap: linkedReward == null
                    ? null
                    : () => context.push(
                          RouteNames.voucherDetail(linkedReward.id),
                        ),
              );
            }),
            const SizedBox(height: AppSpacing.xxl),
          ],
          RewardsSectionHeader(
            title: 'Available Vouchers',
            actionLabel: showHistory ? 'Hide History' : 'History >',
            onActionTap: onHistoryToggle,
          ),
          const SizedBox(height: AppSpacing.lg),
          if (availableVouchers.isEmpty)
            AppEmptyState(
              title: 'No vouchers yet',
              message: showNoRewardsBanner
                  ? 'Keep collecting stamps to unlock rewards.'
                  : 'Pull down to refresh your rewards.',
              icon: Icons.card_giftcard_outlined,
            )
          else
            ...availableVouchers.map(
              (reward) => Padding(
                padding: const EdgeInsets.only(bottom: AppSpacing.lg),
                child: RewardVoucherCard(
                  reward: reward,
                  onRedeemTap: () =>
                      context.push(RouteNames.voucherDetail(reward.id)),
                ),
              ),
            ),
          if (showHistory && historyVouchers.isNotEmpty) ...[
            const SizedBox(height: AppSpacing.lg),
            Text(
              'History',
              style: AppTextStyles.titleMedium.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            ...historyVouchers.map(
              (reward) => RewardHistoryTile(reward: reward),
            ),
          ],
          const AppVersionFooter(),
        ],
      ),
    );
  }
}

class _InfoBanner extends StatelessWidget {
  const _InfoBanner({
    required this.message,
    required this.color,
    required this.textColor,
  });

  final String message;
  final Color color;
  final Color textColor;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.lg),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(AppSpacing.lg),
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: AppColors.border),
        ),
        child: Text(
          message,
          style: AppTextStyles.bodyMedium.copyWith(color: textColor),
        ),
      ),
    );
  }
}
