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
    this.collectedStampCount,
    super.key,
  });

  final Milestone milestone;
  final int? collectedStampCount;
  final bool isFirst;
  final bool isLast;

  @override
  Widget build(BuildContext context) {
    final achieved = collectedStampCount != null &&
        milestone.isAchieved(collectedStampCount!);

    return SizedBox(
      height: 120,
      child: Row(
        children: [
          SizedBox(
            width: 56,
            child: Column(
              children: [
                Expanded(
                  child: Container(
                    width: 3,
                    color: isFirst ? Colors.transparent : AppColors.border,
                  ),
                ),
                Container(
                  width: 32,
                  height: 32,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: achieved
                        ? AppColors.primaryBlue
                        : AppColors.backgroundWhite,
                    border: Border.all(
                      color:
                          achieved ? AppColors.primaryBlue : AppColors.border,
                      width: 3,
                    ),
                  ),
                  alignment: Alignment.center,
                  child: achieved
                      ? const Icon(
                          Icons.check_rounded,
                          color: AppColors.backgroundWhite,
                          size: 16,
                        )
                      : Text(
                          '${milestone.requiredStampCount}',
                          style: AppTextStyles.labelMedium.copyWith(
                            color: AppColors.accentRed,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                ),
                Expanded(
                  child: Container(
                    width: 3,
                    color: isLast ? Colors.transparent : AppColors.border,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Expanded(
            child: Container(
              padding: const EdgeInsets.all(AppSpacing.md),
              decoration: BoxDecoration(
                color: achieved ? AppColors.blueSurface : AppColors.surface,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: AppColors.border),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    milestone.name,
                    style: AppTextStyles.titleMedium.copyWith(
                      color: AppColors.primaryBlue,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    milestone.rewardTitle,
                    style: AppTextStyles.bodyMedium,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
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
