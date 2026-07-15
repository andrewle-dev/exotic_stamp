import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/profile.dart';

class ProfileMemoriesCarousel extends StatelessWidget {
  const ProfileMemoriesCarousel({
    required this.memories,
    super.key,
    this.onViewAllTap,
  });

  final List<ProfileMemory> memories;
  final VoidCallback? onViewAllTap;

  @override
  Widget build(BuildContext context) {
    if (memories.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _SectionHeader(
          icon: Icons.photo_camera_outlined,
          title: 'Memories',
          trailingLabel: 'View All',
          onTrailingTap: onViewAllTap,
        ),
        const SizedBox(height: AppSpacing.lg),
        SizedBox(
          height: 160,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: memories.length,
            separatorBuilder: (_, __) => const SizedBox(width: AppSpacing.lg),
            itemBuilder: (context, index) {
              return _MemoryCard(memory: memories[index]);
            },
          ),
        ),
      ],
    );
  }
}

class _MemoryCard extends StatelessWidget {
  const _MemoryCard({required this.memory});

  final ProfileMemory memory;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 220,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(18),
        color: AppColors.surface,
        border: Border.all(color: AppColors.border),
        image: memory.imageUrl != null && memory.imageUrl!.isNotEmpty
            ? DecorationImage(
                image: NetworkImage(memory.imageUrl!),
                fit: BoxFit.cover,
              )
            : null,
      ),
      clipBehavior: Clip.antiAlias,
      child: Stack(
        fit: StackFit.expand,
        children: [
          if (memory.imageUrl == null || memory.imageUrl!.isEmpty)
            Center(
              child: Icon(
                Icons.landscape_outlined,
                size: 48,
                color: AppColors.textSecondary.withValues(alpha: 0.35),
              ),
            ),
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.transparent,
                  Colors.black.withValues(alpha: 0.55),
                ],
              ),
            ),
          ),
          Positioned(
            left: AppSpacing.lg,
            right: AppSpacing.lg,
            bottom: AppSpacing.lg,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (memory.capturedAtLabel != null)
                  Text(
                    memory.capturedAtLabel!,
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.backgroundWhite.withValues(alpha: 0.85),
                    ),
                  ),
                Text(
                  memory.title,
                  style: AppTextStyles.titleMedium.copyWith(
                    color: AppColors.backgroundWhite,
                    fontWeight: FontWeight.w800,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class ProfileAchievementsGrid extends StatelessWidget {
  const ProfileAchievementsGrid({
    required this.achievements,
    super.key,
  });

  final List<ProfileAchievement> achievements;

  @override
  Widget build(BuildContext context) {
    if (achievements.isEmpty) {
      return const SizedBox.shrink();
    }

    final unlockedCount =
        achievements.where((achievement) => !achievement.locked).length;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _SectionHeader(
          icon: Icons.emoji_events_outlined,
          title: 'Achievements',
          trailingLabel: '$unlockedCount/${achievements.length}',
        ),
        const SizedBox(height: AppSpacing.lg),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: achievements.length,
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: AppSpacing.md,
            mainAxisSpacing: AppSpacing.md,
            childAspectRatio: 1.35,
          ),
          itemBuilder: (context, index) {
            return _AchievementCard(achievement: achievements[index]);
          },
        ),
      ],
    );
  }
}

class _AchievementCard extends StatelessWidget {
  const _AchievementCard({required this.achievement});

  final ProfileAchievement achievement;

  @override
  Widget build(BuildContext context) {
    final locked = achievement.locked;

    return Container(
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: locked ? AppColors.surface : AppColors.backgroundWhite,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            _iconForAchievement(achievement),
            color: locked ? AppColors.textSecondary : AppColors.primaryBlue,
            size: 24,
          ),
          const Spacer(),
          Text(
            achievement.title,
            style: AppTextStyles.titleMedium.copyWith(
              color: locked ? AppColors.textSecondary : AppColors.textPrimary,
              fontWeight: FontWeight.w700,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            locked
                ? 'Locked'
                : achievement.earnedAtLabel ?? 'Unlocked',
            style: AppTextStyles.caption.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  IconData _iconForAchievement(ProfileAchievement achievement) {
    return switch (achievement.iconName) {
      'early_bird' => Icons.home_outlined,
      'line_hero' => Icons.format_list_bulleted_rounded,
      'globetrotter' => Icons.public_outlined,
      _ => Icons.qr_code_scanner_rounded,
    };
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({
    required this.icon,
    required this.title,
    this.trailingLabel,
    this.onTrailingTap,
  });

  final IconData icon;
  final String title;
  final String? trailingLabel;
  final VoidCallback? onTrailingTap;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, color: AppColors.primaryBlue, size: 22),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Text(
            title,
            style: AppTextStyles.titleLarge.copyWith(
              fontWeight: FontWeight.w800,
            ),
          ),
        ),
        if (trailingLabel != null)
          TextButton(
            onPressed: onTrailingTap,
            style: TextButton.styleFrom(
              foregroundColor: onTrailingTap == null
                  ? AppColors.textSecondary
                  : AppColors.primaryBlue,
              padding: EdgeInsets.zero,
              minimumSize: Size.zero,
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            child: Text(
              trailingLabel!,
              style: AppTextStyles.labelLarge.copyWith(
                color: onTrailingTap == null
                    ? AppColors.textSecondary
                    : AppColors.primaryBlue,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
      ],
    );
  }
}
