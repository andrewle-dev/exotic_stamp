import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_shadow.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/station.dart';
import '../../domain/entities/station_collected_status.dart';

class StationDirectoryRow extends StatelessWidget {
  const StationDirectoryRow({
    super.key,
    required this.station,
    required this.onTap,
    this.distanceLabel,
    this.lineBadgeLabel,
    this.mediaUrlResolver,
  });

  final Station station;
  final VoidCallback onTap;
  final String? distanceLabel;
  final String? lineBadgeLabel;
  final MediaUrlResolver? mediaUrlResolver;

  MediaUrlResolver get _resolver => mediaUrlResolver ?? MediaUrlResolver();

  @override
  Widget build(BuildContext context) {
    final imageUrl = _resolver.resolve(
      station.stampPreviewUrl ?? station.imageUrl,
    );
    final collected = station.collectedStatus == StationCollectedStatus.collected;

    return Material(
      color: AppColors.backgroundWhite,
      borderRadius: AppRadius.lgAll,
      child: InkWell(
        onTap: onTap,
        borderRadius: AppRadius.lgAll,
        child: Ink(
          decoration: AppShadow.cardDecoration(borderRadius: AppRadius.lgAll),
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: Row(
              children: [
                _StationAvatar(
                  imageUrl: imageUrl,
                  collected: collected,
                ),
                const SizedBox(width: AppSpacing.lg),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        station.label,
                        style: AppTextStyles.titleMedium.copyWith(
                          fontWeight: FontWeight.w800,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: AppSpacing.sm),
                      Row(
                        children: [
                          if (lineBadgeLabel != null) ...[
                            _LineBadge(label: lineBadgeLabel!),
                            const SizedBox(width: AppSpacing.md),
                          ],
                          if (distanceLabel != null)
                            Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                const Icon(
                                  Icons.schedule_outlined,
                                  size: 14,
                                  color: AppColors.textSecondary,
                                ),
                                const SizedBox(width: AppSpacing.xs),
                                Text(
                                  distanceLabel!,
                                  style: AppTextStyles.caption.copyWith(
                                    color: AppColors.textSecondary,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ],
                            ),
                        ],
                      ),
                    ],
                  ),
                ),
                if (collected)
                  Container(
                    margin: const EdgeInsets.only(right: AppSpacing.sm),
                    width: 24,
                    height: 24,
                    decoration: BoxDecoration(
                      color: AppColors.blueTint,
                      shape: BoxShape.circle,
                      border: Border.all(color: AppColors.primaryBlue),
                    ),
                    child: const Icon(
                      Icons.check_rounded,
                      size: 14,
                      color: AppColors.primaryBlue,
                    ),
                  ),
                const Icon(
                  Icons.chevron_right_rounded,
                  color: AppColors.textSecondary,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _StationAvatar extends StatelessWidget {
  const _StationAvatar({
    required this.imageUrl,
    required this.collected,
  });

  final String? imageUrl;
  final bool collected;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 52,
      height: 52,
      decoration: BoxDecoration(
        color: collected ? AppColors.blueTint : AppColors.surface,
        shape: BoxShape.circle,
        border: Border.all(
          color: collected ? AppColors.primaryBlue : AppColors.border,
        ),
        image: imageUrl == null
            ? null
            : DecorationImage(
                image: NetworkImage(imageUrl!),
                fit: BoxFit.cover,
              ),
      ),
      child: imageUrl == null
          ? Icon(
              Icons.train_outlined,
              color: collected ? AppColors.primaryBlue : AppColors.textSecondary,
            )
          : null,
    );
  }
}

class _LineBadge extends StatelessWidget {
  const _LineBadge({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.sm,
        vertical: 2,
      ),
      decoration: BoxDecoration(
        color: AppColors.primaryBlue,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        label.toUpperCase(),
        style: AppTextStyles.caption.copyWith(
          color: AppColors.backgroundWhite,
          fontWeight: FontWeight.w800,
          fontSize: 10,
          letterSpacing: 0.3,
        ),
      ),
    );
  }
}
