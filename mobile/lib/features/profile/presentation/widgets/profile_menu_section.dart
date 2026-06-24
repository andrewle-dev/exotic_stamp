import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';

class ProfileMenuSection extends StatelessWidget {
  const ProfileMenuSection({
    super.key,
    required this.onSettingsTap,
    required this.onLogoutTap,
  });

  final VoidCallback onSettingsTap;
  final VoidCallback onLogoutTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: AppRadius.lgAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          _MenuTile(
            icon: Icons.person_outline_rounded,
            title: 'Thông tin cá nhân',
            onTap: onSettingsTap,
          ),
          const Divider(height: 1, color: AppColors.border),
          _MenuTile(
            icon: Icons.shield_outlined,
            title: 'Quyền riêng tư & bảo mật',
            onTap: onSettingsTap,
          ),
          const Divider(height: 1, color: AppColors.border),
          _MenuTile(
            icon: Icons.help_outline_rounded,
            title: 'Trung tâm hỗ trợ',
            onTap: () {},
          ),
          const Divider(height: 1, color: AppColors.border),
          _MenuTile(
            icon: Icons.logout_rounded,
            title: 'Đăng xuất',
            titleColor: AppColors.accentRed,
            iconColor: AppColors.accentRed,
            onTap: onLogoutTap,
          ),
        ],
      ),
    );
  }
}

class _MenuTile extends StatelessWidget {
  const _MenuTile({
    required this.icon,
    required this.title,
    required this.onTap,
    this.titleColor = AppColors.textPrimary,
    this.iconColor = AppColors.primaryBlue,
  });

  final IconData icon;
  final String title;
  final VoidCallback onTap;
  final Color titleColor;
  final Color iconColor;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      contentPadding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.xxs,
      ),
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: AppRadius.mdAll,
        ),
        child: Icon(icon, color: iconColor, size: 22),
      ),
      title: Text(
        title,
        style: AppTextStyles.titleMedium.copyWith(color: titleColor),
      ),
      trailing: const Icon(
        Icons.chevron_right_rounded,
        color: AppColors.textSecondary,
      ),
    );
  }
}
