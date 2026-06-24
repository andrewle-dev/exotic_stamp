import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../shared/widgets/app_network_image.dart';
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
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Hồ sơ',
                style: AppTextStyles.headlineMedium.copyWith(
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              Row(
                children: [
                  _Avatar(avatarUrl: profile.avatarUrl),
                  const SizedBox(width: AppSpacing.md),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          profile.displayName,
                          style: AppTextStyles.titleLarge.copyWith(
                            color: AppColors.textPrimary,
                          ),
                        ),
                        if (profile.email.isNotEmpty) ...[
                          const SizedBox(height: AppSpacing.xxs),
                          Text(
                            profile.email,
                            style: AppTextStyles.bodyMedium,
                          ),
                        ],
                        if (profile.phoneNumber != null &&
                            profile.phoneNumber!.isNotEmpty) ...[
                          const SizedBox(height: AppSpacing.xxs),
                          Text(
                            profile.phoneNumber!,
                            style: AppTextStyles.bodyMedium,
                          ),
                        ],
                        if (profile.createdAt != null) ...[
                          const SizedBox(height: AppSpacing.xs),
                          Text(
                            _joinedLabel(profile.createdAt!),
                            style: AppTextStyles.caption,
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        IconButton(
          onPressed: onSettingsTap,
          icon: const Icon(
            Icons.settings_outlined,
            color: AppColors.textPrimary,
          ),
        ),
      ],
    );
  }

  String _joinedLabel(DateTime createdAt) {
    final month = createdAt.month.toString().padLeft(2, '0');
    return 'Tham gia $month/${createdAt.year}';
  }
}

class _Avatar extends StatelessWidget {
  const _Avatar({this.avatarUrl});

  final String? avatarUrl;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 72,
      height: 72,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.border, width: 2),
        color: AppColors.blueTint,
      ),
      clipBehavior: Clip.antiAlias,
      child: avatarUrl != null && avatarUrl!.isNotEmpty
          ? AppNetworkImage(
              imageUrl: avatarUrl,
              width: 72,
              height: 72,
              errorWidget: const _AvatarFallback(),
            )
          : const _AvatarFallback(),
    );
  }
}

class _AvatarFallback extends StatelessWidget {
  const _AvatarFallback();

  @override
  Widget build(BuildContext context) {
    return const ColoredBox(
      color: AppColors.blueTint,
      child: Icon(
        Icons.person_outline_rounded,
        size: 36,
        color: AppColors.primaryBlue,
      ),
    );
  }
}
