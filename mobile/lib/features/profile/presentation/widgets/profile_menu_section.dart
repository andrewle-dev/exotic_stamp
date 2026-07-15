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
    this.onApiDebugTap,
    this.onAdminNfcWriterTap,
    this.showAdminTools = false,
  });

  final VoidCallback onSettingsTap;
  final VoidCallback onLogoutTap;
  final VoidCallback? onApiDebugTap;
  final VoidCallback? onAdminNfcWriterTap;
  final bool showAdminTools;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: AppRadius.xlAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          _MenuTile(
            icon: Icons.person_outline_rounded,
            title: 'Personal Information',
            onTap: onSettingsTap,
          ),
          const Divider(height: 1, color: AppColors.border),
          _MenuTile(
            icon: Icons.shield_outlined,
            title: 'Privacy & Security',
            onTap: onSettingsTap,
          ),
          if (onApiDebugTap != null) ...[
            const Divider(height: 1, color: AppColors.border),
            _MenuTile(
              icon: Icons.bug_report_outlined,
              title: 'API Debug',
              onTap: onApiDebugTap!,
            ),
          ],
          if (showAdminTools && onAdminNfcWriterTap != null) ...[
            const Divider(height: 1, color: AppColors.border),
            _MenuTile(
              icon: Icons.nfc_rounded,
              title: 'NFC Tag Writer',
              onTap: onAdminNfcWriterTap!,
            ),
          ],
          const Divider(height: 1, color: AppColors.border),
          _MenuTile(
            icon: Icons.help_outline_rounded,
            title: 'Help Center',
            onTap: () {},
          ),
          const Divider(height: 1, color: AppColors.border),
          _MenuTile(
            icon: Icons.logout_rounded,
            title: 'Log Out',
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
        horizontal: AppSpacing.lg,
        vertical: AppSpacing.xs,
      ),
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: AppRadius.lgAll,
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
