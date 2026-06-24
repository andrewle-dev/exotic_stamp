import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/stamp_item.dart';

class StampGridItem extends StatelessWidget {
  const StampGridItem({
    super.key,
    required this.item,
    required this.onTap,
    this.mediaUrlResolver,
  });

  final StampItem item;
  final VoidCallback onTap;
  final MediaUrlResolver? mediaUrlResolver;

  @override
  Widget build(BuildContext context) {
    final imageUrl = (mediaUrlResolver ?? MediaUrlResolver()).resolve(
      item.stampDesignUrl,
    );
    final borderColor =
        item.collected ? AppColors.primaryBlue : AppColors.border;

    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Expanded(
            child: Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: AppColors.backgroundWhite,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: borderColor, width: 2),
              ),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: _StampImage(
                      imageUrl: imageUrl,
                      collected: item.collected,
                    ),
                  ),
                  if (!item.collected)
                    Container(
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.45),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Center(
                        child: Icon(
                          Icons.lock_outline_rounded,
                          color: AppColors.accentRed,
                          size: 28,
                        ),
                      ),
                    ),
                  Positioned(
                    top: 6,
                    right: 6,
                    child: Container(
                      width: 24,
                      height: 24,
                      decoration: BoxDecoration(
                        color: AppColors.backgroundWhite,
                        shape: BoxShape.circle,
                        border: Border.all(color: borderColor, width: 1.5),
                      ),
                      child: Icon(
                        item.collected
                            ? Icons.check_rounded
                            : Icons.lock_outline_rounded,
                        size: 14,
                        color: item.collected
                            ? AppColors.primaryBlue
                            : AppColors.accentRed,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            item.stationName,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            textAlign: TextAlign.center,
            style: AppTextStyles.caption.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

class _StampImage extends StatelessWidget {
  const _StampImage({
    required this.imageUrl,
    required this.collected,
  });

  final String? imageUrl;
  final bool collected;

  @override
  Widget build(BuildContext context) {
    if (imageUrl == null) {
      return ColoredBox(
        color: AppColors.surface,
        child: Icon(
          Icons.image_not_supported_outlined,
          color: collected ? AppColors.primaryBlue : AppColors.textSecondary,
        ),
      );
    }

    final image = CachedNetworkImage(
      imageUrl: imageUrl!,
      fit: BoxFit.cover,
      placeholder: (_, __) => const ColoredBox(
        color: AppColors.surface,
        child: Center(
          child: CircularProgressIndicator(
            strokeWidth: 2,
            color: AppColors.primaryBlue,
          ),
        ),
      ),
      errorWidget: (_, __, ___) => const ColoredBox(
        color: AppColors.surface,
        child: Icon(Icons.broken_image_outlined),
      ),
    );

    if (collected) {
      return image;
    }

    return ColorFiltered(
      colorFilter: const ColorFilter.matrix(<double>[
        0.2126, 0.7152, 0.0722, 0, 0, //
        0.2126, 0.7152, 0.0722, 0, 0, //
        0.2126, 0.7152, 0.0722, 0, 0, //
        0, 0, 0, 1, 0,
      ]),
      child: image,
    );
  }
}
