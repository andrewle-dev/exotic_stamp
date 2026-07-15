import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/profile.dart';

class ProfileStatsRow extends StatelessWidget {
  const ProfileStatsRow({
    super.key,
    required this.stats,
  });

  final ProfileStats stats;

  @override
  Widget build(BuildContext context) {
    if (!stats.hasAnyData) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.all(AppSpacing.lg),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: AppRadius.xlAll,
          border: Border.all(color: AppColors.border),
        ),
        child: Text(
          'Stats will appear when the backend provides profile data.',
          style: AppTextStyles.bodyMedium.copyWith(
            color: AppColors.textSecondary,
          ),
          textAlign: TextAlign.center,
        ),
      );
    }

    final cards = <Widget>[];
    if (stats.collectedStampsCount != null) {
      cards.add(
        _StatCard(
          icon: Icons.emoji_events_outlined,
          label: 'STAMPS',
          value: '${stats.collectedStampsCount}',
        ),
      );
    }
    if (stats.linesCount != null) {
      cards.add(
        _StatCard(
          icon: Icons.map_outlined,
          label: 'LINES',
          value: '${stats.linesCount}',
        ),
      );
    }
    if (stats.rankPosition != null) {
      cards.add(
        _StatCard(
          icon: Icons.leaderboard_outlined,
          label: 'RANK',
          value: '#${stats.rankPosition}',
        ),
      );
    }
    if (cards.isEmpty && stats.memoriesCount != null) {
      cards.add(
        _StatCard(
          icon: Icons.photo_camera_outlined,
          label: 'MEMORIES',
          value: '${stats.memoriesCount}',
        ),
      );
    }

    return Row(
      children: [
        for (var i = 0; i < cards.length; i++) ...[
          if (i > 0) const SizedBox(width: AppSpacing.md),
          Expanded(child: cards[i]),
        ],
      ],
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({
    required this.icon,
    required this.label,
    required this.value,
  });

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        vertical: AppSpacing.lg,
        horizontal: AppSpacing.md,
      ),
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: AppRadius.xlAll,
        border: Border.all(color: AppColors.border),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 12,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          Icon(icon, color: AppColors.primaryBlue, size: 22),
          const SizedBox(height: AppSpacing.sm),
          Text(
            value,
            style: AppTextStyles.titleMedium.copyWith(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            label,
            style: AppTextStyles.caption.copyWith(
              letterSpacing: 0.8,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}
