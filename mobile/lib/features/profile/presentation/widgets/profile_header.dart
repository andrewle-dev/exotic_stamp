import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_icons.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_typography.dart';
import '../../../../shared/widgets/app_screen_header.dart';
import '../../domain/entities/profile.dart';

class ProfileHeader extends StatelessWidget {
  const ProfileHeader({
    super.key,
    required this.profile,
    required this.onSettingsTap,
  });

  final Profile profile;
  final VoidCallback onSettingsTap;

  @override
  Widget build(BuildContext context) {
    final level = profile.stats?.level;
    final subtitleParts = <String>[
      if (profile.subtitle != null && profile.subtitle!.isNotEmpty)
        profile.subtitle!,
      if (profile.joinedLabel != null) profile.joinedLabel!,
    ];

    return Column(
      children: [
        AppScreenHeader.title(
          title: 'Profile',
          actionIcon: AppIcons.settings,
          actionTooltip: 'Settings',
          onAction: onSettingsTap,
        ),
        const SizedBox(height: AppSpacing.xxl),
        Center(
          child: Stack(
            clipBehavior: Clip.none,
            alignment: Alignment.center,
            children: [
              _Avatar(avatarUrl: profile.avatarUrl),
              if (level != null)
                Positioned(
                  right: -4,
                  bottom: -4,
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: AppSpacing.md,
                      vertical: AppSpacing.xs,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.primaryBlue,
                      borderRadius: AppRadius.pillAll,
                      border: Border.all(
                        color: AppColors.backgroundWhite,
                        width: 2,
                      ),
                    ),
                    child: Text(
                      'LVL $level',
                      style: AppTextStyles.caption.copyWith(
                        color: AppColors.backgroundWhite,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.xl),
        Text(
          profile.displayName,
          style: AppTextStyles.sectionTitle.copyWith(
            fontWeight: FontWeight.w800,
          ),
          textAlign: TextAlign.center,
        ),
        if (subtitleParts.isNotEmpty) ...[
          const SizedBox(height: AppSpacing.sm),
          Text(
            subtitleParts.join(' • '),
            style: AppTextStyles.body.copyWith(
              color: AppColors.textSecondary,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ],
    );
  }
}

class _Avatar extends StatelessWidget {
  const _Avatar({this.avatarUrl});

  final String? avatarUrl;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 96,
      height: 96,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.border, width: 3),
        color: AppColors.blueTint,
        image: avatarUrl != null && avatarUrl!.isNotEmpty
            ? DecorationImage(
                image: NetworkImage(avatarUrl!),
                fit: BoxFit.cover,
              )
            : null,
      ),
      child: avatarUrl == null || avatarUrl!.isEmpty
          ? const Icon(
              AppIcons.profile,
              size: 42,
              color: AppColors.primaryBlue,
            )
          : null,
    );
  }
}

class ProfileInviteCard extends StatelessWidget {
  const ProfileInviteCard({required this.invite, super.key});

  final ProfileInvite invite;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: AppRadius.xlAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Invite Friends',
                  style: AppTextStyles.cardTitle.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: AppSpacing.sm),
                Text(
                  invite.description,
                  style: AppTextStyles.body.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),
                InkWell(
                  onTap: () {
                    Clipboard.setData(
                      ClipboardData(text: invite.referralCode),
                    );
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Referral code copied'),
                        behavior: SnackBarBehavior.floating,
                      ),
                    );
                  },
                  borderRadius: AppRadius.pillAll,
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: AppSpacing.lg,
                      vertical: AppSpacing.md,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.backgroundWhite,
                      borderRadius: AppRadius.pillAll,
                      border: Border.all(
                        color: AppColors.primaryBlue,
                        style: BorderStyle.solid,
                      ),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          invite.referralCode,
                          style: AppTextStyles.button.copyWith(
                            color: AppColors.primaryBlue,
                            fontWeight: FontWeight.w800,
                            letterSpacing: 1.2,
                          ),
                        ),
                        const SizedBox(width: AppSpacing.md),
                        const Icon(
                          Icons.copy_rounded,
                          size: 18,
                          color: AppColors.primaryBlue,
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.lg),
          Icon(
            Icons.share_outlined,
            size: 56,
            color: AppColors.primaryBlue.withValues(alpha: 0.18),
          ),
        ],
      ),
    );
  }
}
