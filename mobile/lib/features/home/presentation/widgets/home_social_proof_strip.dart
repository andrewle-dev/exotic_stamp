import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/home_summary.dart';

class HomeSocialProofStrip extends StatelessWidget {
  const HomeSocialProofStrip({super.key, required this.socialProof});

  final HomeSocialProof socialProof;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.lg,
        vertical: AppSpacing.md,
      ),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.surface,
            AppColors.blueTint.withValues(alpha: 0.5),
          ],
        ),
        borderRadius: AppRadius.lgAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          SizedBox(
            width: 56,
            height: 28,
            child: Stack(
              children: [
                _AvatarDot(color: AppColors.primaryBlue.withValues(alpha: 0.85)),
                Positioned(
                  left: 16,
                  child: _AvatarDot(
                    color: AppColors.accentRed.withValues(alpha: 0.85),
                  ),
                ),
                Positioned(
                  left: 32,
                  child: _AvatarDot(
                    color: AppColors.textSecondary.withValues(alpha: 0.75),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Text(
              socialProof.message,
              style: AppTextStyles.bodyMedium.copyWith(
                fontWeight: FontWeight.w600,
                fontSize: 13,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _AvatarDot extends StatelessWidget {
  const _AvatarDot({required this.color});

  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 28,
      height: 28,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.backgroundWhite, width: 2),
      ),
    );
  }
}
