import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/reward_unlocked_share_payload.dart';

class RewardUnlockedCelebrationHeader extends StatelessWidget {
  const RewardUnlockedCelebrationHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          width: 112,
          height: 112,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: RadialGradient(
              colors: [
                AppColors.blueTint,
                AppColors.blueSurface.withValues(alpha: 0.7),
                AppColors.backgroundWhite.withValues(alpha: 0),
              ],
              stops: const [0.0, 0.55, 1.0],
            ),
          ),
          child: Container(
            width: 88,
            height: 88,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  AppColors.backgroundWhite,
                  AppColors.blueTint,
                ],
              ),
              shape: BoxShape.circle,
              border: Border.all(
                color: AppColors.primaryBlue.withValues(alpha: 0.35),
                width: 2,
              ),
            ),
            child: const Icon(
              Icons.celebration_rounded,
              size: 48,
              color: AppColors.primaryBlue,
            ),
          ),
        ),
        const SizedBox(height: AppSpacing.xl),
        Text(
          'Chúc mừng!',
          style: AppTextStyles.displayMedium.copyWith(
            color: AppColors.primaryBlue,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: AppSpacing.md),
        Text(
          'Bạn vừa mở khóa phần thưởng mới',
          style: AppTextStyles.bodyLarge.copyWith(
            color: AppColors.textSecondary,
          ),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }
}

class RewardUnlockedCard extends StatelessWidget {
  const RewardUnlockedCard({
    super.key,
    required this.payload,
  });

  final RewardUnlockedSharePayload payload;

  @override
  Widget build(BuildContext context) {
    final mediaResolver = MediaUrlResolver();
    final imageUrl = mediaResolver.resolve(payload.rewardImageUrl);
    final partner = payload.partnerName ?? 'Đối tác';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            AppColors.backgroundWhite,
            AppColors.blueSurface,
          ],
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.primaryBlue, width: 2),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 16,
            offset: Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        children: [
          if (imageUrl != null)
            ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: SizedBox(
                width: 96,
                height: 96,
                child: CachedNetworkImage(
                  imageUrl: imageUrl,
                  fit: BoxFit.cover,
                ),
              ),
            )
          else
            Container(
              width: 96,
              height: 96,
              decoration: BoxDecoration(
                color: AppColors.blueTint,
                borderRadius: BorderRadius.circular(16),
              ),
              child: const Icon(
                Icons.card_giftcard_rounded,
                size: 48,
                color: AppColors.primaryBlue,
              ),
            ),
          const SizedBox(height: AppSpacing.lg),
          Text(
            partner,
            style: AppTextStyles.titleMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            payload.displayTitle,
            style: AppTextStyles.sectionTitle.copyWith(
              color: AppColors.primaryBlue,
              fontWeight: FontWeight.w800,
            ),
            textAlign: TextAlign.center,
          ),
          if (payload.unlockCondition != null) ...[
            const SizedBox(height: AppSpacing.md),
            Container(
              padding: const EdgeInsets.symmetric(
                horizontal: AppSpacing.lg,
                vertical: AppSpacing.sm,
              ),
              decoration: BoxDecoration(
                color: AppColors.blueTint,
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                payload.unlockCondition!,
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.primaryBlue,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ],
          if (payload.voucherCode != null) ...[
            const SizedBox(height: AppSpacing.lg),
            Text(
              payload.voucherCode!,
              style: AppTextStyles.titleLarge.copyWith(
                color: AppColors.primaryBlue,
                letterSpacing: 1.2,
              ),
            ),
          ],
          if (payload.pendingFulfillment) ...[
            const SizedBox(height: AppSpacing.lg),
            Text(
              'Mã voucher sẽ được cập nhật sớm.',
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textSecondary,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ],
      ),
    );
  }
}
