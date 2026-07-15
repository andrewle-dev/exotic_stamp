import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/milestone.dart';

class MilestoneTimelineItem extends StatelessWidget {
  const MilestoneTimelineItem({
    required this.milestone,
    required this.isFirst,
    required this.isLast,
    this.onClaimTap,
    super.key,
  });

  final Milestone milestone;
  final bool isFirst;
  final bool isLast;
  final VoidCallback? onClaimTap;

  @override
  Widget build(BuildContext context) {
    final status = milestone.claimStatus ?? MilestoneClaimStatus.locked;
    final isClaimed = status == MilestoneClaimStatus.claimed;
    final isClaimable = status == MilestoneClaimStatus.claimable;
    final isLocked = status == MilestoneClaimStatus.locked;

    return IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          SizedBox(
            width: 56,
            child: Column(
              children: [
                Expanded(
                  child: Container(
                    width: 2,
                    color: isFirst ? Colors.transparent : AppColors.border,
                  ),
                ),
                _TimelineNode(
                  milestone: milestone,
                  status: status,
                ),
                Expanded(
                  child: Container(
                    width: 2,
                    color: isLast ? Colors.transparent : AppColors.border,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Container(
              margin: const EdgeInsets.symmetric(vertical: AppSpacing.sm),
              padding: const EdgeInsets.all(AppSpacing.lg),
              decoration: BoxDecoration(
                color: isLocked ? AppColors.surface : AppColors.backgroundWhite,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: isClaimable
                      ? AppColors.primaryBlue.withValues(alpha: 0.35)
                      : AppColors.border,
                ),
                boxShadow: isLocked
                    ? null
                    : const [
                        BoxShadow(
                          color: AppColors.shadow,
                          blurRadius: 10,
                          offset: Offset(0, 4),
                        ),
                      ],
              ),
              child: Row(
                children: [
                  _MilestoneIcon(status: status),
                  const SizedBox(width: AppSpacing.lg),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          milestone.name,
                          style: AppTextStyles.titleMedium.copyWith(
                            color: isLocked
                                ? AppColors.textSecondary
                                : AppColors.primaryBlue,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: AppSpacing.sm),
                        Text(
                          milestone.rewardTitle,
                          style: AppTextStyles.bodyMedium.copyWith(
                            color: AppColors.textSecondary,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: AppSpacing.md),
                  if (isClaimed)
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppSpacing.lg,
                        vertical: AppSpacing.sm,
                      ),
                      decoration: BoxDecoration(
                        color: AppColors.blueTint,
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        'Claimed',
                        style: AppTextStyles.caption.copyWith(
                          color: AppColors.primaryBlue,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    )
                  else if (isClaimable)
                    FilledButton(
                      onPressed: onClaimTap,
                      style: FilledButton.styleFrom(
                        backgroundColor: AppColors.primaryBlue,
                        foregroundColor: AppColors.backgroundWhite,
                        padding: const EdgeInsets.symmetric(
                          horizontal: AppSpacing.xl,
                          vertical: AppSpacing.md,
                        ),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(999),
                        ),
                      ),
                      child: const Text('Claim'),
                    )
                  else if (isLocked)
                    const Icon(
                      Icons.lock_outline_rounded,
                      color: AppColors.textSecondary,
                    ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _TimelineNode extends StatelessWidget {
  const _TimelineNode({
    required this.milestone,
    required this.status,
  });

  final Milestone milestone;
  final MilestoneClaimStatus status;

  @override
  Widget build(BuildContext context) {
    final isClaimed = status == MilestoneClaimStatus.claimed;
    final isClaimable = status == MilestoneClaimStatus.claimable;

    return Container(
      width: 34,
      height: 34,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: isClaimed
            ? AppColors.primaryBlue
            : AppColors.backgroundWhite,
        border: Border.all(
          color: isClaimable || isClaimed
              ? AppColors.primaryBlue
              : AppColors.border,
          width: 3,
        ),
      ),
      alignment: Alignment.center,
      child: isClaimed
          ? const Icon(
              Icons.check_rounded,
              color: AppColors.backgroundWhite,
              size: 18,
            )
          : Text(
              '${milestone.requiredStampCount}',
              style: AppTextStyles.labelMedium.copyWith(
                color: isClaimable
                    ? AppColors.primaryBlue
                    : AppColors.textSecondary,
                fontWeight: FontWeight.w800,
              ),
            ),
    );
  }
}

class _MilestoneIcon extends StatelessWidget {
  const _MilestoneIcon({required this.status});

  final MilestoneClaimStatus status;

  @override
  Widget build(BuildContext context) {
    final (icon, color, bg) = switch (status) {
      MilestoneClaimStatus.claimed => (
          Icons.star_rounded,
          AppColors.primaryBlue,
          AppColors.blueTint,
        ),
      MilestoneClaimStatus.claimable => (
          Icons.local_cafe_outlined,
          AppColors.primaryBlue,
          AppColors.blueTint,
        ),
      MilestoneClaimStatus.inProgress => (
          Icons.directions_railway_outlined,
          AppColors.primaryBlue,
          AppColors.blueTint,
        ),
      MilestoneClaimStatus.locked => (
          Icons.emoji_events_outlined,
          AppColors.textSecondary,
          AppColors.surface,
        ),
    };

    return Container(
      width: 44,
      height: 44,
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(14),
      ),
      child: Icon(icon, color: color, size: 22),
    );
  }
}
