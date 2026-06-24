import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/rewards_overview.dart';

class RewardsProgressCard extends StatelessWidget {
  const RewardsProgressCard({
    required this.overview,
    super.key,
  });

  final RewardsOverview overview;

  @override
  Widget build(BuildContext context) {
    final progress = overview.progress;
    final collected = progress?.collected ?? 0;
    final total = progress?.total ?? 0;
    final percentage = progress?.percentage ?? 0;
    final progressValue = total > 0 ? collected / total : 0.0;
    final next = overview.nextMilestone;

    return Container(
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(24),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'TIẾN ĐỘ CỦA BẠN',
            style: AppTextStyles.labelMedium.copyWith(
              color: AppColors.primaryBlue,
              fontWeight: FontWeight.w800,
              letterSpacing: 1.1,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          RichText(
            text: TextSpan(
              style: AppTextStyles.titleLarge.copyWith(
                color: AppColors.textPrimary,
              ),
              children: [
                TextSpan(
                  text: '$collected',
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
                TextSpan(
                  text: total > 0 ? ' / $total stamp' : ' stamp',
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: LinearProgressIndicator(
              value: progressValue.clamp(0, 1),
              minHeight: 14,
              backgroundColor: AppColors.border,
              valueColor:
                  const AlwaysStoppedAnimation<Color>(AppColors.primaryBlue),
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            '$percentage% hoàn thành',
            style: AppTextStyles.bodyMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          if (next != null) ...[
            const SizedBox(height: AppSpacing.md),
            Text.rich(
              TextSpan(
                style: AppTextStyles.bodyMedium,
                children: [
                  const TextSpan(text: 'Phần thưởng tiếp theo: '),
                  TextSpan(
                    text: next.rewardTitle,
                    style: const TextStyle(
                      color: AppColors.primaryBlue,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  TextSpan(text: ' (còn ${next.stampsRemaining} stamp)'),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}
