import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/config/scan_capabilities.dart';
import '../../../../shared/widgets/app_version_footer.dart';
import '../../domain/entities/stamp_item.dart';

class StampBookFooter extends StatelessWidget {
  const StampBookFooter({super.key, required this.stations});

  final List<StampItem> stations;

  @override
  Widget build(BuildContext context) {
    final hasUncollected = stations.any((station) => !station.collected);

    return Column(
      children: [
        if (hasUncollected) ...[
          Text.rich(
            TextSpan(
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textSecondary,
              ),
              children: const [
                TextSpan(text: 'Ghé ga và tìm '),
                TextSpan(
                  text: 'thẻ NFC',
                  style: TextStyle(
                    color: AppColors.primaryBlue,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                // QR mention restored when ScanCapabilities.enableQrFlow is true.
                if (ScanCapabilities.enableQrFlow) ...[
                  TextSpan(text: ' hoặc '),
                  TextSpan(
                    text: 'mã QR',
                    style: TextStyle(
                      color: AppColors.accentRed,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
                TextSpan(text: ' để thu stamp còn thiếu.'),
              ],
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpacing.md),
        ],
        const AppVersionFooter(),
      ],
    );
  }
}
