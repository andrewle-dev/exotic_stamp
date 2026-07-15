import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/home_summary.dart';

class HomeOfferBanner extends StatelessWidget {
  const HomeOfferBanner({
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

    return Column(
      children: [
        Container(
          height: 140,
          width: double.infinity,
          padding: const EdgeInsets.all(AppSpacing.xl),
          decoration: BoxDecoration(
            borderRadius: AppRadius.xlAll,
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
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (banner.promoLabel != null)
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: AppSpacing.md,
                    vertical: AppSpacing.xs,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.accentRed,
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    banner.promoLabel!,
                    style: AppTextStyles.labelMedium.copyWith(
                      color: AppColors.backgroundWhite,
                      fontWeight: FontWeight.w700,
                      fontSize: 10,
                      letterSpacing: 0.4,
                    ),
                  ),
                ),
              const Spacer(),
              Text(
                banner.campaignName ?? 'Chiến dịch đang diễn ra',
                style: AppTextStyles.titleLarge.copyWith(
                  color: AppColors.backgroundWhite,
                  fontWeight: FontWeight.w800,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              if (banner.subtitle != null) ...[
                const SizedBox(height: AppSpacing.xs),
                Text(
                  banner.subtitle!,
                  style: AppTextStyles.bodyMedium.copyWith(
                    color: AppColors.backgroundWhite.withValues(alpha: 0.92),
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 18,
              height: 6,
              decoration: BoxDecoration(
                color: AppColors.primaryBlue,
                borderRadius: BorderRadius.circular(999),
              ),
            ),
            const SizedBox(width: AppSpacing.sm),
            Container(
              width: 6,
              height: 6,
              decoration: const BoxDecoration(
                color: AppColors.border,
                shape: BoxShape.circle,
              ),
            ),
            const SizedBox(width: AppSpacing.sm),
            Container(
              width: 6,
              height: 6,
              decoration: const BoxDecoration(
                color: AppColors.border,
                shape: BoxShape.circle,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
