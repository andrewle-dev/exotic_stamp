import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';

class StationCollectCta extends StatelessWidget {
  const StationCollectCta({
    super.key,
    required this.onTap,
    this.enabled = true,
  });

  final VoidCallback? onTap;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: enabled ? AppColors.accentRed : AppColors.border,
      borderRadius: AppRadius.xlAll,
      child: InkWell(
        onTap: enabled ? onTap : null,
        borderRadius: AppRadius.xlAll,
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.xl,
            vertical: AppSpacing.xl,
          ),
          child: Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  color: AppColors.backgroundWhite.withValues(alpha: 0.22),
                  borderRadius: AppRadius.mdAll,
                ),
                child: const Icon(
                  Icons.auto_awesome,
                  color: AppColors.backgroundWhite,
                  size: 20,
                ),
              ),
              const SizedBox(width: AppSpacing.lg),
              Expanded(
                child: Text(
                  'Collect this stamp',
                  style: AppTextStyles.buttonLabel.copyWith(
                    letterSpacing: 0.6,
                    fontSize: 15,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
