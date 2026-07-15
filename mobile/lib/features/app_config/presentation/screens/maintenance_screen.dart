import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_typography.dart';
import '../../../../shared/widgets/app_action_buttons.dart';
import '../../domain/entities/app_update_decision.dart';

/// Blocking maintenance screen with retry.
class MaintenanceScreen extends StatelessWidget {
  const MaintenanceScreen({
    required this.decision,
    required this.onRetry,
    this.isRetrying = false,
    super.key,
  });

  final AppUpdateDecision decision;
  final VoidCallback onRetry;
  final bool isRetrying;

  @override
  Widget build(BuildContext context) {
    final message = (decision.maintenanceMessage?.trim().isNotEmpty ?? false)
        ? decision.maintenanceMessage!.trim()
        : 'Ứng dụng đang bảo trì. Vui lòng thử lại sau.';

    return PopScope(
      canPop: false,
      child: Scaffold(
        backgroundColor: AppColors.backgroundWhite,
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.xxl),
            child: Column(
              children: [
                const Spacer(),
                Container(
                  width: 88,
                  height: 88,
                  decoration: const BoxDecoration(
                    color: AppColors.blueTint,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.construction_rounded,
                    size: 44,
                    color: AppColors.primaryBlue,
                  ),
                ),
                const SizedBox(height: AppSpacing.xl),
                Text(
                  'Đang bảo trì',
                  style: AppTextStyles.headlineMedium.copyWith(
                    color: AppColors.textPrimary,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: AppSpacing.md),
                Text(
                  message,
                  style: AppTextStyles.body.copyWith(
                    color: AppColors.textSecondary,
                  ),
                  textAlign: TextAlign.center,
                ),
                const Spacer(),
                AppPrimaryButton(
                  label: 'Thử lại',
                  isLoading: isRetrying,
                  onPressed: isRetrying ? null : onRetry,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
