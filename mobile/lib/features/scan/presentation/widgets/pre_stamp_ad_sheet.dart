import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/collect_stamp_result.dart';
import 'scan_action_buttons.dart';

class PreStampAdSheet extends StatelessWidget {
  const PreStampAdSheet({
    required this.ad,
    required this.onContinue,
    super.key,
  });

  final CollectSponsorAd ad;
  final VoidCallback onContinue;

  static Future<void> show(
    BuildContext context, {
    required CollectSponsorAd ad,
    required VoidCallback onContinue,
  }) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.backgroundWhite,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) {
        return PreStampAdSheet(ad: ad, onContinue: onContinue);
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.xxl,
          AppSpacing.xl,
          AppSpacing.xxl,
          AppSpacing.xxl,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Nhà tài trợ',
              style: AppTextStyles.caption.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            Text(ad.title, style: AppTextStyles.sectionTitle),
            if (ad.subtitle != null) ...[
              const SizedBox(height: AppSpacing.md),
              Text(
                ad.subtitle!,
                style: AppTextStyles.bodyLarge.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            ],
            const SizedBox(height: AppSpacing.xxl),
            ScanPrimaryButton(
              label: 'Tiếp tục',
              onPressed: () {
                Navigator.of(context).pop();
                onContinue();
              },
            ),
          ],
        ),
      ),
    );
  }
}
