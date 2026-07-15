import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_text_styles.dart';
import 'app_card.dart';

/// Collection progress card with bar and optional next-reward copy.
class ProgressCard extends StatelessWidget {
  const ProgressCard({
    super.key,
    required this.collectedCount,
    required this.totalCount,
    this.title,
    this.subtitle,
    this.nextRewardText,
    this.showShadow = false,
  });

  final int collectedCount;
  final int totalCount;
  final String? title;
  final String? subtitle;
  final String? nextRewardText;
  final bool showShadow;

  @override
  Widget build(BuildContext context) {
    final fraction = totalCount > 0 ? collectedCount / totalCount : 0.0;
    final percentage = (fraction * 100).round();

    return AppCard(
      showShadow: showShadow,
      backgroundColor: AppColors.blueTint,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (title != null) ...[
            Text(
              title!,
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.xs),
          ],
          Row(
            children: [
              Expanded(
                child: Text(
                  subtitle ?? '$collectedCount / $totalCount Stamps',
                  style: AppTextStyles.cardTitle.copyWith(
                    color: AppColors.primaryBlue,
                  ),
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.md,
                  vertical: AppSpacing.xs,
                ),
                decoration: BoxDecoration(
                  color: AppColors.primaryBlue,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  '$percentage%',
                  style: AppTextStyles.caption.copyWith(
                    color: AppColors.backgroundWhite,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: LinearProgressIndicator(
              value: fraction,
              minHeight: 8,
              backgroundColor: AppColors.surface,
              valueColor: const AlwaysStoppedAnimation(AppColors.primaryBlue),
            ),
          ),
          if (nextRewardText != null) ...[
            const SizedBox(height: AppSpacing.md),
            Text(
              nextRewardText!,
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ],
      ),
    );
  }
}
