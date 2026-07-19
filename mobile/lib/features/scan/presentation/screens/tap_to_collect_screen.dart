import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';
import '../widgets/onboarding_feature_chips.dart';
import '../widgets/onboarding_page_indicator.dart';
import '../widgets/scan_action_buttons.dart';
import '../widgets/tap_to_collect_illustration.dart';

class TapToCollectScreen extends StatelessWidget {
  const TapToCollectScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppSecondaryAppBar(
        title: 'Thu stamp',
        showBottomDivider: false,
        actions: [
          TextButton(
            onPressed: () => context.go(RouteNames.home),
            child: Text(
              'Bỏ qua',
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(AppSpacing.xxl),
        child: Column(
          children: [
            const Expanded(
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    TapToCollectIllustration(),
                    SizedBox(height: AppSpacing.xxl),
                    Text(
                      'Chạm NFC để nhận Stamp',
                      style: AppTextStyles.displayMedium,
                      textAlign: TextAlign.center,
                    ),
                    SizedBox(height: AppSpacing.md),
                    Text(
                      'Đến nhà ga, chạm điện thoại vào thẻ NFC và mở khóa '
                      'stamp độc bản của bạn trong tích tắc.',
                      style: AppTextStyles.bodyLarge,
                      textAlign: TextAlign.center,
                    ),
                    SizedBox(height: AppSpacing.xl),
                    OnboardingFeatureChips(),
                  ],
                ),
              ),
            ),
            const OnboardingPageIndicator(activeIndex: 1),
            const SizedBox(height: AppSpacing.xl),
            ScanPrimaryButton(
              label: 'Tiếp theo',
              onPressed: () => context.go(RouteNames.scan),
            ),
            const SizedBox(height: AppSpacing.xl),
          ],
        ),
      ),
    );
  }
}
