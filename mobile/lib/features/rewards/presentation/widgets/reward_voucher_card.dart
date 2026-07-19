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
    required this.onRedeemTap,
    super.key,
    this.onFavoriteTap,
  });

  final UserReward reward;
  final VoidCallback? onRedeemTap;
  final VoidCallback? onFavoriteTap;

  @override
  Widget build(BuildContext context) {
    final mediaResolver = MediaUrlResolver();
    final imageUrl = mediaResolver.resolve(reward.rewardImageUrl);
    final disabled = reward.status != UserRewardStatus.available;
    final partnerName = reward.partnerName ?? reward.milestoneName ?? 'Partner';

    return Opacity(
      opacity: disabled ? 0.65 : 1,
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.backgroundWhite,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: AppColors.border),
          boxShadow: const [
            BoxShadow(
              color: AppColors.shadow,
              blurRadius: 12,
              offset: Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: Row(
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(999),
                    child: SizedBox(
                      width: 44,
                      height: 44,
                      child: imageUrl != null
                          ? CachedNetworkImage(
                              imageUrl: imageUrl,
                              fit: BoxFit.cover,
                            )
                          : ColoredBox(
                              color: AppColors.blueTint,
                              child: Icon(
                                Icons.storefront_outlined,
                                color: AppColors.primaryBlue.withValues(
                                  alpha: disabled ? 0.5 : 1,
                                ),
                              ),
                            ),
                    ),
                  ),
                  const SizedBox(width: AppSpacing.lg),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          partnerName,
                          style: AppTextStyles.titleMedium,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        if (reward.expiresAt != null) ...[
                          const SizedBox(height: AppSpacing.xs),
                          Text(
                            'Expires ${_formatDate(reward.expiresAt!)}',
                            style: AppTextStyles.caption.copyWith(
                              color: AppColors.textSecondary,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                  if (onFavoriteTap != null)
                    IconButton(
                      onPressed: onFavoriteTap,
                      icon: Icon(
                        reward.isFavorite
                            ? Icons.star_rounded
                            : Icons.star_outline_rounded,
                        color: reward.isFavorite
                            ? Colors.amber.shade700
                            : AppColors.textSecondary,
                      ),
                    ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg),
              child: LayoutBuilder(
                builder: (context, constraints) {
                  return CustomPaint(
                    size: Size(constraints.maxWidth, 1),
                    painter: _DottedDividerPainter(),
                  );
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: Row(
                children: [
                  Expanded(
                    child: Text(
                      reward.displayOfferTitle,
                      style: AppTextStyles.titleMedium.copyWith(
                        color: AppColors.primaryBlue,
                        fontWeight: FontWeight.w800,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  const SizedBox(width: AppSpacing.md),
                  OutlinedButton(
                    onPressed: disabled ? null : onRedeemTap,
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.primaryBlue,
                      side: const BorderSide(color: AppColors.primaryBlue),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(999),
                      ),
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppSpacing.xl,
                        vertical: AppSpacing.md,
                      ),
                    ),
                    child: Text(
                      _actionLabel(reward.status),
                      style: AppTextStyles.labelLarge.copyWith(
                        color: AppColors.primaryBlue,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  static String _formatDate(DateTime date) {
    const months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];
    return '${months[date.month - 1]} ${date.day}, ${date.year}';
  }

  static String _actionLabel(UserRewardStatus status) {
    return switch (status) {
      UserRewardStatus.available => 'Redeem code',
      UserRewardStatus.used => 'Used',
      UserRewardStatus.expired => 'Expired',
      UserRewardStatus.pending => 'Pending',
      UserRewardStatus.unavailable => 'Unavailable',
    };
  }
}

class _DottedDividerPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    const dashWidth = 4.0;
    const dashSpace = 4.0;
    final paint = Paint()
      ..color = AppColors.primaryBlue.withValues(alpha: 0.35)
      ..strokeWidth = 1;

    var startX = 0.0;
    while (startX < size.width) {
      canvas.drawLine(
        Offset(startX, 0),
        Offset(startX + dashWidth, 0),
        paint,
      );
      startX += dashWidth + dashSpace;
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class RewardHistoryTile extends StatelessWidget {
  const RewardHistoryTile({required this.reward, super.key});

  final UserReward reward;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: CircleAvatar(
        backgroundColor: AppColors.blueTint,
        child: Icon(
          Icons.receipt_long_outlined,
          color: AppColors.primaryBlue.withValues(alpha: 0.8),
        ),
      ),
      title: Text(
        reward.displayOfferTitle,
        style: AppTextStyles.bodyLarge,
      ),
      subtitle: Text(
        reward.partnerName ?? reward.milestoneName ?? '',
        style: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.textSecondary,
        ),
      ),
      trailing: Text(
        RewardVoucherCard._actionLabel(reward.status),
        style: AppTextStyles.caption.copyWith(
          color: AppColors.textSecondary,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
