import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/home_summary.dart';

class HomeMilestoneRow extends StatelessWidget {
  const HomeMilestoneRow({super.key, required this.milestones});

  final List<HomeMilestonePreview> milestones;

  @override
  Widget build(BuildContext context) {
    if (milestones.isEmpty) {
      return const SizedBox.shrink();
    }

    return Row(
      children: [
        for (var i = 0; i < milestones.length; i++) ...[
          if (i > 0) const SizedBox(width: AppSpacing.md),
          Expanded(child: _MilestoneChip(milestone: milestones[i])),
        ],
      ],
    );
  }
}

class _MilestoneChip extends StatelessWidget {
  const _MilestoneChip({required this.milestone});

  final HomeMilestonePreview milestone;

  @override
  Widget build(BuildContext context) {
    final achieved = milestone.achieved;

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.lg,
      ),
      decoration: BoxDecoration(
        color: achieved ? AppColors.blueTint : AppColors.backgroundWhite,
        borderRadius: AppRadius.lgAll,
        border: Border.all(
          color: achieved ? AppColors.primaryBlue : AppColors.border,
          width: achieved ? 1.5 : 1,
        ),
      ),
      child: Column(
        children: [
          Icon(
            achieved ? Icons.check_circle : Icons.card_giftcard_outlined,
            color: achieved ? AppColors.primaryBlue : AppColors.textSecondary,
            size: 22,
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            milestone.label,
            style: AppTextStyles.labelMedium.copyWith(
              color: achieved ? AppColors.primaryBlue : AppColors.textSecondary,
              fontWeight: FontWeight.w700,
              fontSize: 11,
            ),
            textAlign: TextAlign.center,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
