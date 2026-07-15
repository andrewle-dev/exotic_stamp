import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_shadow.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/station.dart';

class NearbyStationHeroCard extends StatelessWidget {
  const NearbyStationHeroCard({
    super.key,
    required this.station,
    required this.distanceAwayLabel,
    required this.lineLabel,
    required this.onTap,
    this.onDirectionsTap,
    this.mediaUrlResolver,
  });

  final Station station;
  final String? distanceAwayLabel;
  final String lineLabel;
  final VoidCallback onTap;
  final VoidCallback? onDirectionsTap;
  final MediaUrlResolver? mediaUrlResolver;

  MediaUrlResolver get _resolver => mediaUrlResolver ?? MediaUrlResolver();

  @override
  Widget build(BuildContext context) {
    final imageUrl = _resolver.resolve(
      station.stampPreviewUrl ?? station.imageUrl,
    );

    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 168,
        width: double.infinity,
        decoration: BoxDecoration(
          borderRadius: AppRadius.xlAll,
          color: AppColors.primaryBlue,
          image: imageUrl == null
              ? null
              : DecorationImage(
                  image: NetworkImage(imageUrl),
                  fit: BoxFit.cover,
                  colorFilter: ColorFilter.mode(
                    AppColors.primaryBlue.withValues(alpha: 0.35),
                    BlendMode.darken,
                  ),
                ),
          boxShadow: AppShadow.card,
        ),
        child: Stack(
          children: [
            Padding(
              padding: const EdgeInsets.all(AppSpacing.xl),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: AppSpacing.md,
                      vertical: AppSpacing.xs,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.primaryBlue,
                      borderRadius: BorderRadius.circular(999),
                      border: Border.all(
                        color: AppColors.backgroundWhite.withValues(alpha: 0.35),
                      ),
                    ),
                    child: Text(
                      'Nearest Station',
                      style: AppTextStyles.caption.copyWith(
                        color: AppColors.backgroundWhite,
                        fontWeight: FontWeight.w700,
                        fontSize: 11,
                      ),
                    ),
                  ),
                  const Spacer(),
                  Text(
                    station.label,
                    style: AppTextStyles.titleLarge.copyWith(
                      color: AppColors.backgroundWhite,
                      fontWeight: FontWeight.w800,
                      fontSize: 22,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    [
                      if (distanceAwayLabel != null) distanceAwayLabel,
                      lineLabel,
                    ].whereType<String>().join(' • '),
                    style: AppTextStyles.bodyMedium.copyWith(
                      color: AppColors.backgroundWhite.withValues(alpha: 0.92),
                    ),
                  ),
                ],
              ),
            ),
            Positioned(
              right: AppSpacing.xl,
              bottom: AppSpacing.xl,
              child: Material(
                color: AppColors.primaryBlue,
                shape: const CircleBorder(),
                elevation: 2,
                child: InkWell(
                  onTap: onDirectionsTap ?? onTap,
                  customBorder: const CircleBorder(),
                  child: const SizedBox(
                    width: 44,
                    height: 44,
                    child: Icon(
                      Icons.navigation_rounded,
                      color: AppColors.backgroundWhite,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
