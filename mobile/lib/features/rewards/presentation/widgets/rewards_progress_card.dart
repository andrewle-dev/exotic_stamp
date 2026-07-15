import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/rewards_overview.dart';

class RewardsProgressCard extends StatelessWidget {
  const RewardsProgressCard({
    required this.overview,
    super.key,
  });

  final RewardsOverview overview;

  @override
  Widget build(BuildContext context) {
    final progress = overview.progress;
    final collected = progress?.collected ?? 0;
    final total = progress?.total ?? 14;
    final progressValue = total > 0 ? collected / total : 0.0;
    final next = overview.nextMilestone;
    final markers = overview.milestones
        .map((milestone) => milestone.requiredStampCount)
        .where((count) => count > 0 && count < total)
        .toSet()
        .toList()
      ..sort();

    return Container(
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'YOUR PROGRESS',
                      style: AppTextStyles.caption.copyWith(
                        color: AppColors.primaryBlue,
                        fontWeight: FontWeight.w800,
                        letterSpacing: 1.1,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.md),
                    RichText(
                      text: TextSpan(
                        style: AppTextStyles.displayMedium.copyWith(
                          color: AppColors.textPrimary,
                        ),
                        children: [
                          TextSpan(
                            text: '$collected',
                            style: const TextStyle(
                              color: AppColors.primaryBlue,
                              fontWeight: FontWeight.w900,
                            ),
                          ),
                          TextSpan(
                            text: ' / $total Stamps',
                            style: AppTextStyles.titleLarge.copyWith(
                              color: AppColors.textSecondary,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              if (overview.rankTitle != null)
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: AppSpacing.lg,
                    vertical: AppSpacing.sm,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.backgroundWhite,
                    borderRadius: BorderRadius.circular(999),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: Text(
                    overview.rankTitle!,
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.primaryBlue,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(height: AppSpacing.xl),
          Stack(
            alignment: Alignment.centerLeft,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(999),
                child: LinearProgressIndicator(
                  value: progressValue.clamp(0, 1),
                  minHeight: 12,
                  backgroundColor: AppColors.backgroundWhite,
                  valueColor: const AlwaysStoppedAnimation<Color>(
                    AppColors.primaryBlue,
                  ),
                ),
              ),
              ...markers.map((marker) {
                final position = total > 0 ? marker / total : 0.0;
                return Positioned(
                  left: position * (MediaQuery.sizeOf(context).width - 88),
                  child: Container(
                    width: 10,
                    height: 10,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: collected >= marker
                          ? AppColors.primaryBlue
                          : AppColors.backgroundWhite,
                      border: Border.all(color: AppColors.primaryBlue),
                    ),
                  ),
                );
              }),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('START', style: _markerLabelStyle),
              for (final marker in markers) Text('$marker', style: _markerLabelStyle),
              Text('$total (GOAL)', style: _markerLabelStyle),
            ],
          ),
          if (next != null) ...[
            const SizedBox(height: AppSpacing.lg),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(AppSpacing.lg),
              decoration: BoxDecoration(
                color: AppColors.backgroundWhite.withValues(alpha: 0.75),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Text.rich(
                TextSpan(
                  style: AppTextStyles.bodyMedium,
                  children: [
                    WidgetSpan(
                      alignment: PlaceholderAlignment.middle,
                      child: Padding(
                        padding: const EdgeInsets.only(right: AppSpacing.sm),
                        child: Icon(
                          Icons.bolt_rounded,
                          size: 18,
                          color: Colors.amber.shade700,
                        ),
                      ),
                    ),
                    const TextSpan(text: 'Next reward '),
                    TextSpan(
                      text: next.rewardTitle,
                      style: const TextStyle(
                        color: AppColors.primaryBlue,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    TextSpan(
                      text: next.stampsRemaining == 1
                          ? ' in 1 stamp!'
                          : ' in ${next.stampsRemaining} stamps!',
                    ),
                  ],
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  TextStyle get _markerLabelStyle => AppTextStyles.caption.copyWith(
        color: AppColors.textSecondary,
        fontWeight: FontWeight.w700,
        letterSpacing: 0.4,
      );
}
