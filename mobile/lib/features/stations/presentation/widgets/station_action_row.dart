import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';

class StationActionRow extends StatelessWidget {
  const StationActionRow({
    super.key,
    this.onDirections,
    this.onVirtualTour,
  });

  final VoidCallback? onDirections;
  final VoidCallback? onVirtualTour;

  @override
  Widget build(BuildContext context) {
    final actions = <Widget>[
      if (onDirections != null)
        Expanded(
          child: _ActionCard(
            label: 'Directions',
            icon: Icons.send_rounded,
            iconColor: AppColors.primaryBlue,
            onTap: onDirections,
          ),
        ),
      if (onVirtualTour != null)
        Expanded(
          child: _ActionCard(
            label: 'Virtual Tour',
            icon: Icons.explore_outlined,
            iconColor: AppColors.primaryBlue,
            onTap: onVirtualTour,
          ),
        ),
    ];

    if (actions.isEmpty) {
      return const SizedBox.shrink();
    }

    return Row(
      children: [
        for (var i = 0; i < actions.length; i++) ...[
          if (i > 0) const SizedBox(width: AppSpacing.md),
          actions[i],
        ],
      ],
    );
  }
}

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.label,
    required this.icon,
    required this.iconColor,
    this.onTap,
  });

  final String label;
  final IconData icon;
  final Color iconColor;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.backgroundWhite,
      borderRadius: AppRadius.lgAll,
      child: InkWell(
        onTap: onTap,
        borderRadius: AppRadius.lgAll,
        child: Ink(
          decoration: BoxDecoration(
            color: AppColors.backgroundWhite,
            borderRadius: AppRadius.lgAll,
            border: Border.all(color: AppColors.border),
          ),
          padding: const EdgeInsets.symmetric(
            vertical: AppSpacing.lg,
            horizontal: AppSpacing.sm,
          ),
          child: Column(
            children: [
              Icon(icon, color: iconColor, size: 26),
              const SizedBox(height: AppSpacing.sm),
              Text(
                label,
                style: AppTextStyles.caption.copyWith(
                  fontWeight: FontWeight.w600,
                  color: AppColors.textPrimary,
                  fontSize: 12,
                ),
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
