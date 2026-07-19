import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_shadow.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';

class HomeShortcutGrid extends StatelessWidget {
  const HomeShortcutGrid({super.key});

  @override
  Widget build(BuildContext context) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: AppSpacing.md,
      crossAxisSpacing: AppSpacing.md,
      childAspectRatio: 1.55,
      children: [
        _ShortcutTile(
          label: 'Nearby Stations',
          icon: Icons.near_me_outlined,
          iconColor: AppColors.primaryBlue,
          onTap: () => context.go(RouteNames.stations),
        ),
        _ShortcutTile(
          label: 'Claim Rewards',
          icon: Icons.card_giftcard_outlined,
          iconColor: AppColors.accentRed,
          // Secondary route (not a bottom-nav tab). Back on Rewards returns here.
          onTap: () => context.go(RouteNames.rewards),
        ),
        _ShortcutTile(
          label: 'Line Progress',
          icon: Icons.timeline_outlined,
          iconColor: AppColors.primaryBlue,
          onTap: () => context.go(RouteNames.stampBook),
        ),
        _ShortcutTile(
          label: 'Achievements',
          icon: Icons.bolt_rounded,
          iconColor: AppColors.primaryBlue,
          onTap: () => context.go(RouteNames.profile),
        ),
      ],
    );
  }
}

class _ShortcutTile extends StatelessWidget {
  const _ShortcutTile({
    required this.label,
    required this.icon,
    required this.iconColor,
    required this.onTap,
  });

  final String label;
  final IconData icon;
  final Color iconColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.backgroundWhite,
      borderRadius: AppRadius.lgAll,
      child: InkWell(
        onTap: onTap,
        borderRadius: AppRadius.lgAll,
        child: Container(
          decoration: AppShadow.cardDecoration(borderRadius: AppRadius.lgAll),
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(icon, color: iconColor, size: 26),
              const Spacer(),
              Text(
                label,
                style: AppTextStyles.labelMedium.copyWith(
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
