import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/stamp_book.dart';

class StampBookSummaryCard extends StatelessWidget {
  const StampBookSummaryCard({
    super.key,
    required this.stampBook,
  });

  final StampBook stampBook;

  @override
  Widget build(BuildContext context) {
    final progress = stampBook.progress;
    final collected = progress?.collected ?? 0;
    final total = progress?.total ?? stampBook.stations.length;
    final percentage = progress?.percentage ??
        (total == 0 ? 0 : ((collected / total) * 100).round());
    final progressValue = total == 0 ? 0.0 : collected / total;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            stampBook.campaignName ?? 'Sổ stamp',
            style: AppTextStyles.caption.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            stampBook.lineName,
            style: AppTextStyles.titleLarge.copyWith(
              color: AppColors.primaryBlue,
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.sm,
                  vertical: AppSpacing.xs,
                ),
                decoration: BoxDecoration(
                  color: AppColors.primaryBlue,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  '$percentage% hoàn thành',
                  style: AppTextStyles.labelMedium.copyWith(
                    color: AppColors.backgroundWhite,
                  ),
                ),
              ),
              const Spacer(),
              Text(
                '$collected / $total stamp',
                style: AppTextStyles.titleMedium,
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.sm),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: LinearProgressIndicator(
              value: progressValue,
              minHeight: 8,
              backgroundColor: AppColors.border,
              valueColor: const AlwaysStoppedAnimation(AppColors.primaryBlue),
            ),
          ),
        ],
      ),
    );
  }
}
