import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_shadow.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_action_buttons.dart';

/// First-launch onboarding aligned with `visily-welcome.png`.
class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  Future<void> _completeOnboarding(BuildContext context) async {
    await Injection.instance.localPreferences.setOnboardingCompleted(value: true);
    Injection.instance.notifySessionChanged();
    if (context.mounted) {
      context.go(RouteNames.login);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.xxl),
          child: Column(
            children: [
              const SizedBox(height: AppSpacing.md),
              Row(
                children: [
                  const _BrandMark(),
                  const Spacer(),
                  TextButton(
                    onPressed: () => _completeOnboarding(context),
                    child: Text(
                      'Bỏ qua',
                      style: AppTextStyles.bodyMedium.copyWith(
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.xl),
              Expanded(
                child: SingleChildScrollView(
                  child: Column(
                    children: [
                      const _HeroVisual(),
                      const SizedBox(height: AppSpacing.xxl),
                      RichText(
                        textAlign: TextAlign.center,
                        text: TextSpan(
                          style: AppTextStyles.displayMedium,
                          children: [
                            const TextSpan(text: 'Khám phá Metro\n'),
                            TextSpan(
                              text: 'theo cách mới',
                              style: AppTextStyles.displayMedium.copyWith(
                                color: AppColors.primaryBlue,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: AppSpacing.lg),
                      Text(
                        'Biến mỗi chuyến đi hàng ngày thành một hành trình '
                        'sưu tập tem kỹ thuật số đầy thú vị và bất ngờ.',
                        style: AppTextStyles.bodyLarge.copyWith(
                          color: AppColors.textSecondary,
                          height: 1.45,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: AppSpacing.xl),
                      const Row(
                        children: [
                          Expanded(
                            child: _FeatureChip(
                              icon: Icons.confirmation_number_outlined,
                              label: 'Tích điểm',
                            ),
                          ),
                          SizedBox(width: AppSpacing.md),
                          Expanded(
                            child: _FeatureChip(
                              icon: Icons.location_on_outlined,
                              label: 'Mở khóa ga',
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
              const _PageIndicator(activeIndex: 0),
              const SizedBox(height: AppSpacing.xl),
              PrimaryButton(
                label: 'Tiếp theo',
                icon: const Icon(
                  Icons.chevron_right_rounded,
                  color: AppColors.backgroundWhite,
                ),
                onPressed: () => _completeOnboarding(context),
              ),
              const SizedBox(height: AppSpacing.xxl),
            ],
          ),
        ),
      ),
    );
  }
}

class _BrandMark extends StatelessWidget {
  const _BrandMark();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        color: AppColors.textPrimary,
        borderRadius: BorderRadius.circular(12),
      ),
      child: const Icon(
        Icons.bolt_rounded,
        color: AppColors.backgroundWhite,
      ),
    );
  }
}

class _HeroVisual extends StatelessWidget {
  const _HeroVisual();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 320,
      decoration: BoxDecoration(
        borderRadius: AppRadius.xxlAll,
        gradient: const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            AppColors.blueTint,
            AppColors.surface,
          ],
        ),
        border: Border.all(color: AppColors.border),
      ),
      child: Stack(
        children: [
          Positioned(
            top: AppSpacing.xl,
            right: AppSpacing.xl,
            child: Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: AppColors.backgroundWhite,
                shape: BoxShape.circle,
                boxShadow: AppShadow.cardSubtle,
              ),
              child: const Icon(
                Icons.auto_awesome_rounded,
                color: AppColors.primaryBlue,
                size: 20,
              ),
            ),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: Padding(
              padding: const EdgeInsets.all(AppSpacing.xl),
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.all(AppSpacing.lg),
                decoration: AppShadow.cardDecoration(borderRadius: AppRadius.xlAll),
                child: Row(
                  children: [
                    Container(
                      width: 44,
                      height: 44,
                      decoration: const BoxDecoration(
                        color: AppColors.primaryBlue,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.train_rounded,
                        color: AppColors.backgroundWhite,
                      ),
                    ),
                    const SizedBox(width: AppSpacing.lg),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'TRẠM HIỆN TẠI',
                            style: AppTextStyles.caption.copyWith(
                              color: AppColors.primaryBlue,
                              fontWeight: FontWeight.w700,
                              letterSpacing: 0.6,
                            ),
                          ),
                          const Text(
                            'Ga Bến Thành',
                            style: AppTextStyles.titleMedium,
                          ),
                        ],
                      ),
                    ),
                    Text(
                      'ACTIVE',
                      style: AppTextStyles.caption.copyWith(
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
          const Center(
            child: Icon(
              Icons.apartment_rounded,
              size: 120,
              color: AppColors.primaryBlue,
            ),
          ),
        ],
      ),
    );
  }
}

class _FeatureChip extends StatelessWidget {
  const _FeatureChip({
    required this.icon,
    required this.label,
  });

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.lg,
        vertical: AppSpacing.md,
      ),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: AppRadius.pillAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, size: 18, color: AppColors.primaryBlue),
          const SizedBox(width: AppSpacing.sm),
          Flexible(
            child: Text(
              label,
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w600,
              ),
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ],
      ),
    );
  }
}

class _PageIndicator extends StatelessWidget {
  const _PageIndicator({required this.activeIndex});

  final int activeIndex;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(3, (index) {
        final active = index == activeIndex;
        return Container(
          width: active ? 28 : 8,
          height: 8,
          margin: const EdgeInsets.symmetric(horizontal: 4),
          decoration: BoxDecoration(
            color: active ? AppColors.primaryBlue : AppColors.border,
            borderRadius: AppRadius.pillAll,
          ),
        );
      }),
    );
  }
}
