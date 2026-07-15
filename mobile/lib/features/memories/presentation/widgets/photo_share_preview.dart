import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/photo_share_context.dart';
import '../utils/photo_share_capture.dart';

class PhotoSharePreview extends StatelessWidget {
  const PhotoSharePreview({
    super.key,
    required this.photoPath,
    required this.context,
    required this.showStationName,
    required this.showCollectionDate,
  });

  final String photoPath;
  final PhotoShareContext? context;
  final bool showStationName;
  final bool showCollectionDate;

  @override
  Widget build(BuildContext context) {
    final stampContext = this.context;
    final mediaResolver = MediaUrlResolver();
    final stampUrl = mediaResolver.resolve(stampContext?.stampDesignUrl);

    return AspectRatio(
      aspectRatio: 1,
      child: ClipRRect(
        borderRadius: AppRadius.xxlAll,
        child: Stack(
          fit: StackFit.expand,
          children: [
            Image(
              image: photoImageProvider(photoPath)!,
              fit: BoxFit.cover,
            ),
            if (showCollectionDate && stampContext?.collectedAt != null)
              Positioned(
                left: AppSpacing.lg,
                bottom: AppSpacing.lg,
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: AppSpacing.md,
                    vertical: AppSpacing.sm,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.black.withValues(alpha: 0.55),
                    borderRadius: AppRadius.lgAll,
                  ),
                  child: Text(
                    formatShareDate(stampContext!.collectedAt),
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.backgroundWhite,
                    ),
                  ),
                ),
              ),
            if (stampContext != null)
              Positioned(
                right: AppSpacing.lg,
                top: AppSpacing.lg,
                child: _StampSticker(
                  stationName:
                      showStationName ? stampContext.stationName : null,
                  stampImageUrl: stampUrl,
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _StampSticker extends StatelessWidget {
  const _StampSticker({
    required this.stationName,
    required this.stampImageUrl,
  });

  final String? stationName;
  final String? stampImageUrl;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 112,
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: AppRadius.xlAll,
        border: Border.all(color: AppColors.primaryBlue, width: 2),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.12),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ClipRRect(
            borderRadius: AppRadius.lgAll,
            child: SizedBox(
              width: 72,
              height: 72,
              child: stampImageUrl != null
                  ? CachedNetworkImage(
                      imageUrl: stampImageUrl!,
                      fit: BoxFit.cover,
                    )
                  : const ColoredBox(
                      color: AppColors.surface,
                      child: Icon(
                        Icons.train_rounded,
                        color: AppColors.primaryBlue,
                      ),
                    ),
            ),
          ),
          if (stationName != null) ...[
            const SizedBox(height: AppSpacing.sm),
            Text(
              stationName!.toUpperCase(),
              textAlign: TextAlign.center,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: AppTextStyles.caption.copyWith(
                color: AppColors.primaryBlue,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
          const SizedBox(height: AppSpacing.xs),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.nfc_rounded,
                size: 12,
                color: AppColors.textSecondary,
              ),
              const SizedBox(width: 2),
              Text(
                'Verified via NFC',
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.textSecondary,
                  fontSize: 9,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
