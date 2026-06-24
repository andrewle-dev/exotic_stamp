import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/user_reward.dart';

class RewardVoucherCard extends StatelessWidget {
  const RewardVoucherCard({
    required this.reward,
    required this.onTap,
    super.key,
  });

  final UserReward reward;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final mediaResolver = MediaUrlResolver();
    final imageUrl = mediaResolver.resolve(reward.rewardImageUrl);
    final disabled = reward.status != UserRewardStatus.available;

    return Opacity(
      opacity: disabled ? 0.65 : 1,
      child: Material(
        color: AppColors.backgroundWhite,
        borderRadius: BorderRadius.circular(24),
        elevation: 0,
        child: InkWell(
          onTap: disabled ? null : onTap,
          borderRadius: BorderRadius.circular(24),
          child: Ink(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: AppColors.border),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Padding(
                  padding: const EdgeInsets.all(AppSpacing.md),
                  child: Row(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(12),
                        child: SizedBox(
                          width: 48,
                          height: 48,
                          child: imageUrl != null
                              ? CachedNetworkImage(
                                  imageUrl: imageUrl,
                                  fit: BoxFit.cover,
                                )
                              : ColoredBox(
                                  color: AppColors.blueTint,
                                  child: Icon(
                                    Icons.card_giftcard_outlined,
                                    color: AppColors.primaryBlue.withValues(
                                      alpha: disabled ? 0.5 : 1,
                                    ),
                                  ),
                                ),
                        ),
                      ),
                      const SizedBox(width: AppSpacing.md),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              reward.rewardTitle,
                              style: AppTextStyles.titleMedium,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                            if (reward.milestoneName != null) ...[
                              const SizedBox(height: AppSpacing.xs),
                              Text(
                                reward.milestoneName!,
                                style: AppTextStyles.bodyMedium.copyWith(
                                  color: AppColors.textSecondary,
                                ),
                              ),
                            ],
                          ],
                        ),
                      ),
                      _StatusChip(status: reward.status),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: AppSpacing.md,
                    vertical: AppSpacing.sm,
                  ),
                  decoration: const BoxDecoration(
                    color: AppColors.blueSurface,
                    borderRadius: BorderRadius.vertical(
                      bottom: Radius.circular(24),
                    ),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: Text(
                          _footerText(reward),
                          style: AppTextStyles.bodyMedium.copyWith(
                            color: AppColors.primaryBlue,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      if (!disabled)
                        const Icon(
                          Icons.chevron_right_rounded,
                          color: AppColors.primaryBlue,
                        ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  static String _footerText(UserReward reward) {
    switch (reward.status) {
      case UserRewardStatus.available:
        return 'Xem chi tiết voucher';
      case UserRewardStatus.used:
        return 'Đã sử dụng';
      case UserRewardStatus.expired:
        return 'Đã hết hạn';
      case UserRewardStatus.pending:
        return 'Đang chờ mã voucher';
      case UserRewardStatus.unavailable:
        return 'Không khả dụng';
    }
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
      UserRewardStatus.unavailable => ('Không dùng', AppColors.textSecondary),
    };

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.sm,
        vertical: AppSpacing.xs,
      ),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(999),
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
