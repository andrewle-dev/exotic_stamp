import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_icons.dart';
import '../../../../app/theme/app_typography.dart';
import '../../../../shared/widgets/app_screen_header.dart';

class RewardsScreenHeader extends StatelessWidget {
  const RewardsScreenHeader({super.key, this.onAction});

  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    // Secondary route (not in bottom nav) — back + title, no app logo.
    return AppScreenHeader.secondary(
      title: 'Rewards',
      actionIcon: AppIcons.reward,
      actionTooltip: 'Rewards',
      onAction: onAction,
    );
  }
}

class RewardsSectionHeader extends StatelessWidget {
  const RewardsSectionHeader({
    required this.title,
    super.key,
    this.actionLabel,
    this.onActionTap,
  });

  final String title;
  final String? actionLabel;
  final VoidCallback? onActionTap;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(
            title,
            style: AppTextStyles.sectionTitle.copyWith(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w800,
            ),
          ),
        ),
        if (actionLabel != null && onActionTap != null)
          TextButton(
            onPressed: onActionTap,
            style: TextButton.styleFrom(
              foregroundColor: AppColors.primaryBlue,
              padding: EdgeInsets.zero,
              minimumSize: Size.zero,
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            child: Text(
              actionLabel!,
              style: AppTextStyles.button.copyWith(
                color: AppColors.primaryBlue,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
      ],
    );
  }
}
