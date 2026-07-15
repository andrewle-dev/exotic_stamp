import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_typography.dart';
import '../../../../shared/widgets/app_action_buttons.dart';
import '../../domain/entities/app_update_decision.dart';

/// Optional (non-blocking) update prompt. Dismissal is handled by the caller.
Future<bool?> showOptionalUpdateDialog({
  required BuildContext context,
  required AppUpdateDecision decision,
}) {
  return showDialog<bool>(
    context: context,
    barrierDismissible: true,
    builder: (dialogContext) {
      return AlertDialog(
        backgroundColor: AppColors.backgroundWhite,
        title: Text(
          'Đã có phiên bản mới',
          style: AppTextStyles.titleMedium.copyWith(
            color: AppColors.textPrimary,
          ),
        ),
        content: Text(
          'Một phiên bản mới hơn đã sẵn sàng. '
          'Bạn có thể cập nhật ngay hoặc tiếp tục dùng phiên bản hiện tại.',
          style: AppTextStyles.body.copyWith(
            color: AppColors.textSecondary,
          ),
        ),
        actionsAlignment: MainAxisAlignment.spaceBetween,
        actions: [
          AppSecondaryButton(
            label: 'Để sau',
            expand: false,
            onPressed: () => Navigator.of(dialogContext).pop(false),
          ),
          AppPrimaryButton(
            label: 'Cập nhật',
            expand: false,
            onPressed: () async {
              final storeUrl = decision.storeUrl;
              if (storeUrl != null && storeUrl.isNotEmpty) {
                final uri = Uri.tryParse(storeUrl);
                if (uri != null) {
                  await launchUrl(uri, mode: LaunchMode.externalApplication);
                }
              }
              if (dialogContext.mounted) {
                Navigator.of(dialogContext).pop(true);
              }
            },
          ),
        ],
        actionsPadding: const EdgeInsets.fromLTRB(
          AppSpacing.lg,
          0,
          AppSpacing.lg,
          AppSpacing.lg,
        ),
      );
    },
  );
}
