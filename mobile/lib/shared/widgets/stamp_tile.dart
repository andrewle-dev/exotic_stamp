import 'dart:ui' show ImageFilter;

import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';
import '../../app/theme/app_icons.dart';
import '../../app/theme/app_radius.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_text_styles.dart';
import '../../features/stamp_book/domain/entities/stamp_item.dart';

/// Stamp grid tile — collected state comes from [StampItem.collected].
class StampTile extends StatelessWidget {
  const StampTile({
    super.key,
    required this.item,
    required this.onTap,
    this.imageUrl,
    this.lineAccentColor,
  });

  final StampItem item;
  final VoidCallback onTap;
  final String? imageUrl;
  final Color? lineAccentColor;

  @override
  Widget build(BuildContext context) {
    final collected = item.collected;
    final borderColor =
        collected ? AppColors.primaryBlue : AppColors.border;

    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Expanded(
            child: Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: AppColors.backgroundWhite,
                borderRadius: AppRadius.lgAll,
                border: Border.all(color: borderColor, width: 2),
                boxShadow: const [
                  BoxShadow(
                    color: AppColors.shadow,
                    blurRadius: 8,
                    offset: Offset(0, 2),
                  ),
                ],
              ),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  ClipRRect(
                    borderRadius: AppRadius.mdAll,
                    child: _StampImage(
                      imageUrl: imageUrl,
                      collected: collected,
                    ),
                  ),
                  if (!collected) const _LockOverlay(),
                  if (lineAccentColor != null)
                    Positioned(
                      left: 0,
                      right: 0,
                      bottom: 0,
                      child: Container(
                        height: 4,
                        decoration: BoxDecoration(
                          color: lineAccentColor,
                          borderRadius: const BorderRadius.only(
                            bottomLeft: Radius.circular(8),
                            bottomRight: Radius.circular(8),
                          ),
                        ),
                      ),
                    ),
                  Positioned(
                    top: 6,
                    right: 6,
                    child: _StatusBadge(collected: collected),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          SizedBox(
            height: _labelHeight,
            child: Text(
              item.stationName,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              textAlign: TextAlign.center,
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.primaryBlue,
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// Caption line height × 2 (12 × 1.3 × 2).
  static const double _labelHeight = 31.2;
}

class _LockOverlay extends StatelessWidget {
  const _LockOverlay();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Icon(
        AppIcons.lock,
        size: 40,
        color: AppColors.backgroundWhite.withValues(alpha: 0.85),
        shadows: const [
          Shadow(
            color: AppColors.shadow,
            blurRadius: 12,
          ),
        ],
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge({required this.collected});

  final bool collected;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 24,
      height: 24,
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        shape: BoxShape.circle,
        border: Border.all(
          color: collected ? AppColors.primaryBlue : AppColors.border,
          width: 1.5,
        ),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 4,
            offset: Offset(0, 1),
          ),
        ],
      ),
      child: Icon(
        collected ? AppIcons.collected : AppIcons.lock,
        size: 13,
        color: collected ? AppColors.primaryBlue : AppColors.accentRed,
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
      fit: BoxFit.contain,
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

    // Locked: soft Gaussian blur + fade, matching design silhouettes.
    return ImageFiltered(
      imageFilter: ImageFilter.blur(sigmaX: 8, sigmaY: 8),
      child: Opacity(opacity: 0.45, child: image),
    );
  }
}
