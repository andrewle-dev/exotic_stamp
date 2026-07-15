import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/home_summary.dart';

class HomeProgressCard extends StatelessWidget {
  const HomeProgressCard({
    super.key,
    required this.progress,
    this.lineName,
  });

  final CollectionProgress progress;
  final String? lineName;

  @override
  Widget build(BuildContext context) {
    final fraction =
        progress.total > 0 ? progress.collected / progress.total : 0.0;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(
        AppSpacing.xl,
        AppSpacing.xl,
        AppSpacing.xl,
        AppSpacing.xxl,
      ),
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          if (lineName != null) ...[
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                lineName!,
                style: AppTextStyles.titleMedium.copyWith(
                  color: AppColors.primaryBlue,
                ),
              ),
            ),
            const SizedBox(height: AppSpacing.md),
          ],
          SizedBox(
            width: 118,
            height: 118,
            child: Stack(
              alignment: Alignment.center,
              children: [
                SizedBox(
                  width: 118,
                  height: 118,
                  child: CircularProgressIndicator(
                    value: fraction,
                    strokeWidth: 7,
                    backgroundColor: AppColors.surface,
                    valueColor: const AlwaysStoppedAnimation<Color>(
                      AppColors.primaryBlue,
                    ),
                    strokeCap: StrokeCap.round,
                  ),
                ),
                Container(
                  width: 90,
                  height: 90,
                  decoration: const BoxDecoration(
                    color: AppColors.blueSurface,
                    shape: BoxShape.circle,
                  ),
                ),
                Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      '${progress.collected}/${progress.total}',
                      style: AppTextStyles.headlineMedium.copyWith(
                        fontSize: 28,
                        color: AppColors.primaryBlue,
                      ),
                    ),
                    Text(
                      'STAMPS',
                      style: AppTextStyles.caption.copyWith(
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.6,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            '${progress.percentage}% hoàn thành',
            style: AppTextStyles.bodyMedium.copyWith(
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }
}

class HomeProgressPlaceholder extends StatelessWidget {
  const HomeProgressPlaceholder({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.border),
      ),
      child: const Text(
        'Tiến độ sưu tập sẽ hiển thị khi có dữ liệu tuyến metro.',
        style: AppTextStyles.bodyMedium,
        textAlign: TextAlign.center,
      ),
    );
  }
}

class HomeActiveBanner extends StatelessWidget {
  const HomeActiveBanner({
    super.key,
    required this.banner,
    this.mediaUrlResolver,
  });

  final ActiveBanner banner;
  final MediaUrlResolver? mediaUrlResolver;

  MediaUrlResolver get _resolver => mediaUrlResolver ?? MediaUrlResolver();

  @override
  Widget build(BuildContext context) {
    final imageUrl = _resolver.resolve(banner.imageUrl);

    return Container(
      height: 148,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(22),
        color: AppColors.primaryBlue,
        image: imageUrl == null
            ? null
            : DecorationImage(
                image: NetworkImage(imageUrl),
                fit: BoxFit.cover,
                colorFilter: ColorFilter.mode(
                  AppColors.primaryBlue.withValues(alpha: 0.55),
                  BlendMode.darken,
                ),
              ),
      ),
      child: Align(
        alignment: Alignment.bottomLeft,
        child: Text(
          banner.campaignName ?? 'Chiến dịch đang diễn ra',
          style: AppTextStyles.titleLarge.copyWith(
            color: AppColors.backgroundWhite,
          ),
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
      ),
    );
  }
}

class HomeNextRewardCard extends StatelessWidget {
  const HomeNextRewardCard({super.key, required this.nextReward});

  final NextReward nextReward;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.redTint,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          const Icon(
            Icons.card_giftcard_outlined,
            color: AppColors.accentRed,
            size: 32,
          ),
          const SizedBox(width: AppSpacing.lg),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Phần thưởng tiếp theo',
                  style: AppTextStyles.caption.copyWith(
                    fontWeight: FontWeight.w700,
                    color: AppColors.accentRed,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  nextReward.rewardTitle,
                  style: AppTextStyles.titleMedium,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                Text(
                  'Còn ${nextReward.stampsRemaining} stamp nữa',
                  style: AppTextStyles.bodyMedium,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class HomePartialErrorBanner extends StatelessWidget {
  const HomePartialErrorBanner({super.key, required this.messages});

  final List<String> messages;

  @override
  Widget build(BuildContext context) {
    if (messages.isEmpty) {
      return const SizedBox.shrink();
    }

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border),
      ),
      child: Text(
        messages.first,
        style: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.primaryBlue,
        ),
      ),
    );
  }
}
