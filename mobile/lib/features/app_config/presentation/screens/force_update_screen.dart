import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_typography.dart';
import '../../../../shared/widgets/app_action_buttons.dart';
import '../../domain/entities/app_update_decision.dart';

/// Blocking screen when the installed binary is below minimum / force-update.
class ForceUpdateScreen extends StatelessWidget {
  const ForceUpdateScreen({
    required this.decision,
    super.key,
  });

  final AppUpdateDecision decision;

  Future<void> _openStore(BuildContext context) async {
    final storeUrl = decision.storeUrl;
    if (storeUrl == null || storeUrl.isEmpty) {
      if (!context.mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Liên kết cửa hàng chưa được cấu hình. Vui lòng cập nhật thủ công trên CH Play / App Store.',
          ),
        ),
      );
      return;
    }

    final uri = Uri.tryParse(storeUrl);
    if (uri == null) {
      return;
    }
    await launchUrl(uri, mode: LaunchMode.externalApplication);
  }

  @override
  Widget build(BuildContext context) {
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
                    color: AppColors.redTint,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.system_update_alt_rounded,
                    size: 44,
                    color: AppColors.accentRed,
                  ),
                ),
                const SizedBox(height: AppSpacing.xl),
                Text(
                  'Cần cập nhật ứng dụng',
                  style: AppTextStyles.headlineMedium.copyWith(
                    color: AppColors.textPrimary,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: AppSpacing.md),
                Text(
                  'Phiên bản bạn đang dùng không còn được hỗ trợ. '
                  'Vui lòng cập nhật để tiếp tục sử dụng Metro Stamp.',
                  style: AppTextStyles.body.copyWith(
                    color: AppColors.textSecondary,
                  ),
                  textAlign: TextAlign.center,
                ),
                const Spacer(),
                AppPrimaryButton(
                  label: 'Cập nhật ngay',
                  onPressed: () => _openStore(context),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
