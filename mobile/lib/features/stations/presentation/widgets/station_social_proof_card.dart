import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/station_extras.dart';

class StationSocialProofCard extends StatelessWidget {
  const StationSocialProofCard({super.key, required this.socialProof});

  final StationSocialProof socialProof;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: AppRadius.xlAll,
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 16,
            offset: Offset(0, 6),
          ),
        ],
      ),
      child: Row(
        children: [
          SizedBox(
            width: 72,
            height: 32,
            child: Stack(
              children: [
                _AvatarDot(color: AppColors.primaryBlue.withValues(alpha: 0.85)),
                Positioned(
                  left: 18,
                  child: _AvatarDot(
                    color: AppColors.accentRed.withValues(alpha: 0.85),
                  ),
                ),
                Positioned(
                  left: 36,
                  child: Container(
                    width: 32,
                    height: 32,
                    alignment: Alignment.center,
                    decoration: BoxDecoration(
                      color: AppColors.primaryBlue,
                      shape: BoxShape.circle,
                      border: Border.all(color: AppColors.backgroundWhite, width: 2),
                    ),
                    child: Text(
                      socialProof.overflowLabel ?? '+',
                      style: AppTextStyles.caption.copyWith(
                        color: AppColors.backgroundWhite,
                        fontWeight: FontWeight.w800,
                        fontSize: 10,
                      ),
                    ),
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
                fontWeight: FontWeight.w700,
                color: AppColors.primaryBlue,
                fontSize: 13,
                height: 1.3,
              ),
            ),
          ),
          const Icon(
            Icons.auto_awesome,
            color: AppColors.primaryBlue,
            size: 20,
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
      width: 32,
      height: 32,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.backgroundWhite, width: 2),
      ),
    );
  }
}
