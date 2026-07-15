import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';

/// Metro-themed page indicator rendered as a train convoy.
///
/// Active slide = subway locomotive icon; inactive slides = carriage wagons
/// in one horizontal row so the whole control reads as a single train.
///
/// Slot count is capped by the parent (typically `min(bannerCount, 4)`).
/// It must **not** equal the carousel page count when there are 4+ banners.
///
/// Every slot uses the **same fixed size** so swapping active/inactive never
/// reflows the row (avoids the post-transition “jump”).
class MetroCarouselIndicator extends StatelessWidget {
  const MetroCarouselIndicator({
    super.key,
    required this.slotCount,
    required this.activeSlot,
  });

  /// Maximum visual train slots for 4+ banner carousels.
  static const int maxSlots = 4;

  static const subwayAsset = 'assets/animations/subway.png';

  /// Fixed slot size for every car (active and inactive).
  /// Tall enough for the pantograph; wide enough for the subway asset.
  static const double slotWidth = 36;
  static const double slotHeight = 28;

  static const double carriageWidth = 20;
  static const double carriageHeight = 12;
  static const double subwayWidth = 34;
  static const double subwayHeight = 24;

  /// Tight gaps so cars read as one coupled convoy.
  static const double carGap = 4;

  /// Number of visual train slots (not banner page count when capped).
  final int slotCount;

  /// Active slot index in `[0, slotCount)`.
  final int activeSlot;

  /// Indicator slot mapping for a carousel page index.
  static int slotForPage(int pageIndex, int bannerCount) {
    final slots = slotCountFor(bannerCount);
    if (slots <= 0) {
      return 0;
    }
    return pageIndex % slots;
  }

  /// Slot count: actual banner count for 2–3; exactly 4 for 4+ banners.
  static int slotCountFor(int bannerCount) {
    if (bannerCount <= 1) {
      return 0;
    }
    return bannerCount < maxSlots ? bannerCount : maxSlots;
  }

  @override
  Widget build(BuildContext context) {
    if (slotCount <= 0) {
      return const SizedBox.shrink();
    }

    final safeSlot = activeSlot.clamp(0, slotCount - 1);

    return Semantics(
      label: 'Banner indicator slot ${safeSlot + 1} of $slotCount',
      child: SizedBox(
        height: slotHeight,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: List.generate(slotCount, (index) {
            final isActive = index == safeSlot;
            return Padding(
              padding: EdgeInsets.only(left: index == 0 ? 0 : carGap),
              child: _TrainCarSlot(
                key: ValueKey('slot-$index'),
                index: index,
                isActive: isActive,
              ),
            );
          }),
        ),
      ),
    );
  }
}

class _TrainCarSlot extends StatelessWidget {
  const _TrainCarSlot({
    super.key,
    required this.index,
    required this.isActive,
  });

  final int index;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    // Outer box NEVER changes size — this is what stopped the jump.
    return SizedBox(
      width: MetroCarouselIndicator.slotWidth,
      height: MetroCarouselIndicator.slotHeight,
      child: AnimatedSwitcher(
        duration: const Duration(milliseconds: 220),
        switchInCurve: Curves.easeOut,
        switchOutCurve: Curves.easeIn,
        // Keep both transient children stacked in the same fixed box.
        layoutBuilder: (currentChild, previousChildren) {
          return Stack(
            alignment: Alignment.center,
            fit: StackFit.expand,
            children: <Widget>[
              ...previousChildren,
              if (currentChild != null) currentChild,
            ],
          );
        },
        // Fade only — scale was also contributing to a settle-jolt.
        transitionBuilder: (child, animation) {
          return FadeTransition(opacity: animation, child: child);
        },
        child: isActive
            ? _ActiveLocomotive(key: ValueKey('subway-$index'), index: index)
            : _CarriageWagon(key: ValueKey('carriage-$index'), index: index),
      ),
    );
  }
}

class _ActiveLocomotive extends StatelessWidget {
  const _ActiveLocomotive({super.key, required this.index});

  final int index;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Image.asset(
        MetroCarouselIndicator.subwayAsset,
        width: MetroCarouselIndicator.subwayWidth,
        height: MetroCarouselIndicator.subwayHeight,
        fit: BoxFit.contain,
        alignment: Alignment.center,
        filterQuality: FilterQuality.medium,
        errorBuilder: (_, __, ___) => Container(
          key: ValueKey('subway-fallback-$index'),
          width: MetroCarouselIndicator.carriageWidth + 8,
          height: MetroCarouselIndicator.carriageHeight + 2,
          decoration: BoxDecoration(
            color: AppColors.primaryBlue,
            borderRadius: AppRadius.smAll,
          ),
        ),
      ),
    );
  }
}

/// Simplified metro wagon block for inactive slides.
class _CarriageWagon extends StatelessWidget {
  const _CarriageWagon({super.key, required this.index});

  final int index;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        width: MetroCarouselIndicator.carriageWidth,
        height: MetroCarouselIndicator.carriageHeight,
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(4),
          border: Border.all(
            color: AppColors.inactiveIcon.withValues(alpha: 0.55),
            width: 1.2,
          ),
        ),
        padding: const EdgeInsets.symmetric(horizontal: 3, vertical: 2.5),
        child: Row(
          children: [
            Expanded(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  color: AppColors.border,
                  borderRadius: BorderRadius.circular(1.5),
                ),
              ),
            ),
            const SizedBox(width: 2),
            Expanded(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  color: AppColors.border,
                  borderRadius: BorderRadius.circular(1.5),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
