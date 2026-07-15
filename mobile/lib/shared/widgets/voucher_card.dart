import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';
import '../../app/theme/app_radius.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_text_styles.dart';
import '../../features/rewards/domain/entities/user_reward.dart';
import 'app_action_buttons.dart';
import 'app_card.dart';

String _formatDate(DateTime date) {
  final d = date.day.toString().padLeft(2, '0');
  final m = date.month.toString().padLeft(2, '0');
  return '$d/$m/${date.year}';
}

/// Voucher list card — status comes from [UserReward.status] (repository data).
class VoucherCard extends StatelessWidget {
  const VoucherCard({
    super.key,
    required this.reward,
    this.partnerName,
    this.onTap,
    this.onRedeem,
    this.showRedeemButton = false,
  });

  final UserReward reward;
  final String? partnerName;
  final VoidCallback? onTap;
  final VoidCallback? onRedeem;
  final bool showRedeemButton;

  @override
  Widget build(BuildContext context) {
    final disabled = reward.status != UserRewardStatus.available;
    final expiry = reward.expiresAt;

    return Opacity(
      opacity: disabled ? 0.65 : 1,
      child: AppCard(
        onTap: disabled ? null : onTap,
        showShadow: !disabled,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: AppColors.blueTint,
                    borderRadius: AppRadius.mdAll,
                  ),
                  child: Icon(
                    Icons.card_giftcard_outlined,
                    color: AppColors.primaryBlue.withValues(
                      alpha: disabled ? 0.5 : 1,
                    ),
                  ),
                ),
                const SizedBox(width: AppSpacing.lg),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (partnerName != null && partnerName!.isNotEmpty) ...[
                        Text(
                          partnerName!,
                          style: AppTextStyles.caption.copyWith(
                            color: AppColors.primaryBlue,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: AppSpacing.xs),
                      ],
                      Text(
                        reward.rewardTitle,
                        style: AppTextStyles.cardTitle,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
                _StatusChip(status: reward.status),
              ],
            ),
            if (expiry != null) ...[
              const SizedBox(height: AppSpacing.md),
              Text(
                'Hết hạn: ${_formatDate(expiry)}',
                style: AppTextStyles.bodyMedium,
              ),
            ],
            if (showRedeemButton &&
                reward.status == UserRewardStatus.available &&
                onRedeem != null) ...[
              const SizedBox(height: AppSpacing.lg),
              DangerActionButton(
                label: 'Đổi voucher',
                onPressed: onRedeem,
                expand: true,
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({required this.status});

  final UserRewardStatus status;

  @override
  Widget build(BuildContext context) {
    final (label, color) = switch (status) {
      UserRewardStatus.available => ('Khả dụng', AppColors.primaryBlue),
      UserRewardStatus.used => ('Đã dùng', AppColors.textSecondary),
      UserRewardStatus.expired => ('Hết hạn', AppColors.accentRed),
      UserRewardStatus.pending => ('Chờ mã', AppColors.accentRed),
      UserRewardStatus.unavailable => ('Không khả dụng', AppColors.textSecondary),
    };

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.sm,
        vertical: 2,
      ),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: AppRadius.mdAll,
      ),
      child: Text(
        label,
        style: AppTextStyles.caption.copyWith(
          color: color,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
