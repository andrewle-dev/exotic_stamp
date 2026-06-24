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
import '../../domain/entities/rewards_overview.dart';
import '../../domain/usecases/get_rewards_overview_usecase.dart';
import '../cubit/rewards_cubit.dart';
import '../cubit/rewards_state.dart';
import '../widgets/milestone_timeline_item.dart';
import '../widgets/reward_voucher_card.dart';
import '../widgets/rewards_progress_card.dart';
import '../widgets/rewards_refresh_listener.dart';

class RewardsScreen extends StatelessWidget {
  const RewardsScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final RewardsCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<RewardsCubit>.value(
        value: cubit!,
        child: const _RewardsView(),
      );
    }

    return BlocProvider(
      create: (_) => RewardsCubit(
        getRewardsOverviewUseCase: GetRewardsOverviewUseCase(
          Injection.instance.rewardsRepository,
        ),
      ),
      child: const RewardsRefreshListener(
        child: _RewardsView(),
      ),
    );
  }
}

class _RewardsView extends StatelessWidget {
  const _RewardsView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        title: const Text.rich(
          TextSpan(
            children: [
              TextSpan(
                text: 'Phần ',
                style: TextStyle(color: AppColors.accentRed),
              ),
              TextSpan(
                text: 'thưởng',
                style: TextStyle(color: AppColors.primaryBlue),
              ),
            ],
          ),
        ),
      ),
      body: BlocBuilder<RewardsCubit, RewardsState>(
        builder: (context, state) {
          switch (state.status) {
            case RewardsStatus.initial:
            case RewardsStatus.loading:
              return const AppLoadingView(message: 'Đang tải phần thưởng...');
            case RewardsStatus.failure:
              return AppErrorView(
                message: state.failure?.message ?? 'Không thể tải phần thưởng.',
                onRetry: () => context.read<RewardsCubit>().load(),
              );
            case RewardsStatus.loaded:
            case RewardsStatus.noRewardsYet:
              final overview = state.overview;
              if (overview == null) {
                return const AppLoadingView();
              }
              return _RewardsContent(
                overview: overview,
                showNoRewardsBanner: state.status == RewardsStatus.noRewardsYet,
              );
          }
        },
      ),
    );
  }
}

class _RewardsContent extends StatelessWidget {
  const _RewardsContent({
    required this.overview,
    required this.showNoRewardsBanner,
  });

  final RewardsOverview overview;
  final bool showNoRewardsBanner;

  @override
  Widget build(BuildContext context) {
    final milestones = overview.milestones;
    final rewards = overview.rewards;

    return RefreshIndicator(
      color: AppColors.primaryBlue,
      onRefresh: () => context.read<RewardsCubit>().refresh(),
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(AppSpacing.lg),
        children: [
          if (overview.partialErrors.isNotEmpty)
            _PartialErrorBanner(message: overview.partialErrors.first),
          if (overview.progress != null)
            RewardsProgressCard(overview: overview),
          if (overview.campaignName != null) ...[
            const SizedBox(height: AppSpacing.sm),
            Text(
              overview.campaignName!,
              style: AppTextStyles.bodyMedium,
            ),
          ],
          if (milestones.isNotEmpty) ...[
            const SizedBox(height: AppSpacing.xl),
            Text(
              'Cột mốc',
              style: AppTextStyles.titleLarge.copyWith(
                color: AppColors.primaryBlue,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            ...List.generate(milestones.length, (index) {
              return Padding(
                padding: EdgeInsets.only(
                  bottom: index == milestones.length - 1 ? 0 : AppSpacing.sm,
                ),
                child: MilestoneTimelineItem(
                  milestone: milestones[index],
                  collectedStampCount: overview.progress?.collected,
                  isFirst: index == 0,
                  isLast: index == milestones.length - 1,
                ),
              );
            }),
          ],
          const SizedBox(height: AppSpacing.xl),
          Text(
            'Voucher của bạn',
            style: AppTextStyles.titleLarge.copyWith(
              color: AppColors.primaryBlue,
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          if (rewards.isEmpty)
            AppEmptyState(
              title: 'Chưa có phần thưởng',
              message: showNoRewardsBanner
                  ? 'Tiếp tục thu stamp để mở khóa phần thưởng từ máy chủ.'
                  : 'Không thể tải đầy đủ dữ liệu phần thưởng. Kéo xuống để thử lại.',
              icon: Icons.card_giftcard_outlined,
            )
          else
            ...rewards.map(
              (reward) => Padding(
                padding: const EdgeInsets.only(bottom: AppSpacing.md),
                child: RewardVoucherCard(
                  reward: reward,
                  onTap: () => context.push(
                    RouteNames.voucherDetail(reward.id),
                  ),
                ),
              ),
            ),
          const SizedBox(height: AppSpacing.xl),
        ],
      ),
    );
  }
}

class _PartialErrorBanner extends StatelessWidget {
  const _PartialErrorBanner({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.md),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(AppSpacing.md),
        decoration: BoxDecoration(
          color: AppColors.redTint,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: AppColors.accentRed.withValues(alpha: 0.3)),
        ),
        child: Text(
          message,
          style: AppTextStyles.bodyMedium.copyWith(
            color: AppColors.accentRed,
          ),
        ),
      ),
    );
  }
}
