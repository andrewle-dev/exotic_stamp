import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../shared/widgets/app_action_buttons.dart';

/// Scan-flow primary CTA — visual tokens aligned with [AppPrimaryButton].
class ScanPrimaryButton extends StatelessWidget {
  const ScanPrimaryButton({
    required this.label,
    required this.onPressed,
    super.key,
    this.expand = true,
  });

  final String label;
  final VoidCallback? onPressed;
  final bool expand;

  @override
  Widget build(BuildContext context) {
    return AppPrimaryButton(
      label: label,
      onPressed: onPressed,
      expand: expand,
    );
  }
}

/// Scan-flow outline CTA — visual tokens aligned with [AppSecondaryButton].
class ScanOutlineButton extends StatelessWidget {
  const ScanOutlineButton({
    required this.label,
    required this.onPressed,
    super.key,
  });

  final String label;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return AppSecondaryButton(
      label: label,
      onPressed: onPressed,
    );
  }
}

class ScanHeroCard extends StatelessWidget {
  const ScanHeroCard({
    required this.title,
    required this.subtitle,
    required this.icon,
    super.key,
  });

  final String title;
  final String subtitle;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xxl),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: AppRadius.xxlAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          Icon(icon, size: 72, color: AppColors.primaryBlue),
          const SizedBox(height: AppSpacing.xl),
          Text(
            title,
            style: AppTextStyles.headlineMedium.copyWith(
              color: AppColors.primaryBlue,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            subtitle,
            style: AppTextStyles.bodyLarge,
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}
