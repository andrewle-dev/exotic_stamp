import 'dart:async';

import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/home_summary.dart';
import 'metro_carousel_indicator.dart';

/// Partner promotional banner carousel for Home.
///
/// The [PageView] always contains **every** eligible banner. The metro train
/// indicator uses at most [MetroCarouselIndicator.maxSlots] visual slots:
/// `activeSlot = currentPage % slotCount` where
/// `slotCount = min(bannerCount, 4)` for 2+ banners.
///
/// Banner count behavior:
/// - 0: fallback card, no autoplay, no indicator
/// - 1: single banner, no autoplay, no indicator
/// - 2–3: all banners + that many indicator slots (no fake empty cars)
/// - 4+: all banners + exactly 4 indicator slots
class HomeBannerCarousel extends StatefulWidget {
  const HomeBannerCarousel({
    super.key,
    required this.banners,
    this.mediaUrlResolver,
    this.autoPlay = true,
    this.autoPlayInterval = const Duration(milliseconds: 4200),
    this.pageAnimationDuration = const Duration(milliseconds: 420),
  });

  final List<PartnerBanner> banners;
  final MediaUrlResolver? mediaUrlResolver;
  final bool autoPlay;
  final Duration autoPlayInterval;
  final Duration pageAnimationDuration;

  @override
  State<HomeBannerCarousel> createState() => _HomeBannerCarouselState();
}

class _HomeBannerCarouselState extends State<HomeBannerCarousel> {
  late final PageController _pageController;
  Timer? _autoPlayTimer;
  int _activeIndex = 0;
  bool _userInteracting = false;

  MediaUrlResolver get _resolver =>
      widget.mediaUrlResolver ?? MediaUrlResolver();

  bool get _canAutoPlay =>
      widget.autoPlay && widget.banners.length > 1 && !_userInteracting;

  int get _indicatorSlotCount =>
      MetroCarouselIndicator.slotCountFor(widget.banners.length);

  int get _activeIndicatorSlot => MetroCarouselIndicator.slotForPage(
        _activeIndex,
        widget.banners.length,
      );

  @override
  void initState() {
    super.initState();
    _pageController = PageController();
    _scheduleAutoPlay();
  }

  @override
  void didUpdateWidget(covariant HomeBannerCarousel oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.banners.length != widget.banners.length ||
        oldWidget.autoPlay != widget.autoPlay ||
        oldWidget.autoPlayInterval != widget.autoPlayInterval) {
      _clampActiveIndexToBanners();
      _scheduleAutoPlay();
    }
  }

  @override
  void dispose() {
    _autoPlayTimer?.cancel();
    _pageController.dispose();
    super.dispose();
  }

  void _clampActiveIndexToBanners() {
    final length = widget.banners.length;
    if (length == 0) {
      _activeIndex = 0;
      return;
    }
    if (_activeIndex < length) {
      return;
    }
    _activeIndex = 0;
    if (_pageController.hasClients) {
      _pageController.jumpToPage(0);
    }
  }

  void _scheduleAutoPlay() {
    _autoPlayTimer?.cancel();
    if (!_canAutoPlay) {
      return;
    }
    _autoPlayTimer = Timer.periodic(widget.autoPlayInterval, (_) {
      _goToNextPage();
    });
  }

  void _goToNextPage() {
    if (!mounted || !_canAutoPlay || !_pageController.hasClients) {
      return;
    }
    final length = widget.banners.length;
    if (length <= 1) {
      return;
    }
    final next = (_activeIndex + 1) % length;
    _pageController.animateToPage(
      next,
      duration: widget.pageAnimationDuration,
      curve: Curves.easeInOutCubic,
    );
  }

  void _onInteractionStart() {
    if (_userInteracting) {
      return;
    }
    setState(() => _userInteracting = true);
    _autoPlayTimer?.cancel();
  }

  void _onInteractionEnd() {
    if (!_userInteracting) {
      return;
    }
    setState(() => _userInteracting = false);
    _scheduleAutoPlay();
  }

  @override
  Widget build(BuildContext context) {
    final banners = widget.banners;
    if (banners.isEmpty) {
      return const _FallbackPromoCard();
    }

    final slotCount = _indicatorSlotCount;

    return Column(
      children: [
        SizedBox(
          height: 148,
          child: NotificationListener<ScrollNotification>(
            onNotification: (notification) {
              if (notification is ScrollStartNotification &&
                  notification.dragDetails != null) {
                _onInteractionStart();
              } else if (notification is ScrollEndNotification) {
                _onInteractionEnd();
              }
              return false;
            },
            child: PageView.builder(
              controller: _pageController,
              // Full eligible list — never truncated to four partners.
              itemCount: banners.length,
              onPageChanged: (index) {
                setState(() => _activeIndex = index);
              },
              itemBuilder: (context, index) {
                return _PartnerBannerSlide(
                  banner: banners[index],
                  mediaUrlResolver: _resolver,
                );
              },
            ),
          ),
        ),
        if (slotCount > 0) ...[
          const SizedBox(height: AppSpacing.lg),
          MetroCarouselIndicator(
            slotCount: slotCount,
            activeSlot: _activeIndicatorSlot,
          ),
        ],
      ],
    );
  }
}

class _PartnerBannerSlide extends StatelessWidget {
  const _PartnerBannerSlide({
    required this.banner,
    required this.mediaUrlResolver,
  });

  final PartnerBanner banner;
  final MediaUrlResolver mediaUrlResolver;

  @override
  Widget build(BuildContext context) {
    final imageUrl = mediaUrlResolver.resolve(banner.bannerImageUrl);
    final logoUrl = mediaUrlResolver.resolve(banner.logoUrl);
    final name = banner.partnerName.trim().isEmpty
        ? 'Partner'
        : banner.partnerName.trim();

    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        borderRadius: AppRadius.xlAll,
        color: AppColors.primaryBlue,
      ),
      clipBehavior: Clip.antiAlias,
      child: Stack(
        fit: StackFit.expand,
        children: [
          if (imageUrl != null)
            Image.network(
              imageUrl,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => const ColoredBox(
                color: AppColors.primaryBlue,
              ),
            )
          else
            const ColoredBox(color: AppColors.primaryBlue),
          // Soft bottom veil so the chip stays readable on busy art.
          DecoratedBox(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  AppColors.textPrimary.withValues(alpha: 0),
                  AppColors.textPrimary.withValues(alpha: 0.28),
                ],
                stops: const [0.45, 1],
              ),
            ),
          ),
          Positioned(
            left: AppSpacing.md,
            right: AppSpacing.md,
            bottom: AppSpacing.md,
            child: Align(
              alignment: Alignment.bottomLeft,
              child: _PartnerBrandChip(
                name: name,
                logoUrl: logoUrl,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _PartnerBrandChip extends StatelessWidget {
  const _PartnerBrandChip({
    required this.name,
    this.logoUrl,
  });

  final String name;
  final String? logoUrl;

  @override
  Widget build(BuildContext context) {
    return ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 220),
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: AppColors.backgroundWhite.withValues(alpha: 0.92),
          borderRadius: AppRadius.smAll,
          border: Border.all(
            color: AppColors.border.withValues(alpha: 0.85),
          ),
          boxShadow: const [
            BoxShadow(
              color: AppColors.shadow,
              blurRadius: 8,
              offset: Offset(0, 2),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.sm,
            vertical: AppSpacing.xs + 1,
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (logoUrl != null) ...[
                ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: Image.network(
                    logoUrl!,
                    width: 16,
                    height: 16,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const SizedBox.shrink(),
                  ),
                ),
                const SizedBox(width: AppSpacing.xs + 2),
              ],
              Flexible(
                child: Text(
                  name,
                  style: AppTextStyles.labelMedium.copyWith(
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w600,
                    fontSize: 12,
                    height: 1.2,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _FallbackPromoCard extends StatelessWidget {
  const _FallbackPromoCard();

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 148,
      width: double.infinity,
      decoration: BoxDecoration(
        borderRadius: AppRadius.xlAll,
        color: AppColors.primaryBlue,
      ),
      clipBehavior: Clip.antiAlias,
      child: Stack(
        children: [
          const ColoredBox(
            color: AppColors.primaryBlue,
            child: SizedBox.expand(),
          ),
          Positioned(
            left: AppSpacing.md,
            bottom: AppSpacing.md,
            child: DecoratedBox(
              decoration: BoxDecoration(
                color: AppColors.backgroundWhite.withValues(alpha: 0.92),
                borderRadius: AppRadius.smAll,
                border: Border.all(color: AppColors.border),
              ),
              child: Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.sm,
                  vertical: AppSpacing.xs + 1,
                ),
                child: Text(
                  'Metro Hanoi',
                  style: AppTextStyles.labelMedium.copyWith(
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w600,
                    fontSize: 12,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
