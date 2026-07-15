import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';

class StationDetailHero extends StatelessWidget {
  const StationDetailHero({
    super.key,
    required this.imageUrl,
    required this.lineBadgeLabel,
    required this.stationName,
    required this.locationLabel,
    required this.onBack,
    this.onShare,
    this.contentBottomInset = AppSpacing.xxl,
  });

  final String? imageUrl;
  final String lineBadgeLabel;
  final String stationName;
  final String? locationLabel;
  final VoidCallback onBack;
  final VoidCallback? onShare;

  /// Extra space under the badge/name/address so an overlapping white sheet
  /// does not cover the hero copy.
  final double contentBottomInset;

  @override
  Widget build(BuildContext context) {
    final screenHeight = MediaQuery.sizeOf(context).height;
    // Keep hero as a photo header, not the whole viewport (design ≈ top 40%).
    final heroHeight = (screenHeight * 0.38).clamp(300.0, 380.0);

    return SizedBox(
      height: heroHeight,
      width: double.infinity,
      child: Stack(
        fit: StackFit.expand,
        children: [
          if (imageUrl != null)
            Image.network(
              imageUrl!,
              fit: BoxFit.cover,
              alignment: Alignment.center,
              errorBuilder: (_, __, ___) => _placeholder(),
            )
          else
            _placeholder(),
          const DecoratedBox(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                stops: [0, 0.35, 0.7, 1],
                colors: [
                  Color(0x99000000),
                  Color(0x14000000),
                  Color(0x73000000),
                  Color(0xCC000000),
                ],
              ),
            ),
          ),
          // Top chrome pinned to the safe-area edge — never vertically centered.
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            child: SafeArea(
              bottom: false,
              child: Padding(
                padding: const EdgeInsets.fromLTRB(
                  AppSpacing.sm,
                  AppSpacing.xs,
                  AppSpacing.sm,
                  0,
                ),
                child: SizedBox(
                  height: 44,
                  child: Row(
                    children: [
                      _ChromeIconButton(
                        icon: Icons.chevron_left_rounded,
                        onTap: onBack,
                        iconSize: 28,
                      ),
                      const Expanded(
                        child: Text(
                          'Station Detail',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: AppColors.backgroundWhite,
                            fontWeight: FontWeight.w600,
                            fontSize: 17,
                            letterSpacing: -0.2,
                          ),
                        ),
                      ),
                      _ChromeIconButton(
                        // Material share-nodes (connected circles), not ios_share upload.
                        icon: Icons.share_rounded,
                        onTap: onShare,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
          Positioned(
            left: AppSpacing.xl,
            right: AppSpacing.xl,
            bottom: contentBottomInset,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.primaryBlue,
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(
                    lineBadgeLabel,
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.backgroundWhite,
                      fontWeight: FontWeight.w700,
                      fontSize: 12,
                    ),
                  ),
                ),
                const SizedBox(height: AppSpacing.md),
                Text(
                  stationName,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: AppTextStyles.displayMedium.copyWith(
                    color: AppColors.backgroundWhite,
                    fontWeight: FontWeight.w800,
                    fontSize: 28,
                    height: 1.15,
                  ),
                ),
                if (locationLabel != null) ...[
                  const SizedBox(height: AppSpacing.sm),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Padding(
                        padding: EdgeInsets.only(top: 1),
                        child: Icon(
                          Icons.location_on,
                          color: AppColors.backgroundWhite,
                          size: 16,
                        ),
                      ),
                      const SizedBox(width: 4),
                      Expanded(
                        child: Text(
                          locationLabel!,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: AppTextStyles.bodyMedium.copyWith(
                            color: AppColors.backgroundWhite
                                .withValues(alpha: 0.95),
                            fontSize: 14,
                            height: 1.35,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _placeholder() {
    return Container(
      color: AppColors.primaryBlue,
      child: const Center(
        child: Icon(
          Icons.train_outlined,
          size: 64,
          color: AppColors.backgroundWhite,
        ),
      ),
    );
  }
}

class _ChromeIconButton extends StatelessWidget {
  const _ChromeIconButton({
    required this.icon,
    this.onTap,
    this.iconSize = 22,
  });

  final IconData icon;
  final VoidCallback? onTap;
  final double iconSize;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 44,
      height: 44,
      child: IconButton(
        onPressed: onTap,
        padding: EdgeInsets.zero,
        splashRadius: 22,
        icon: Icon(
          icon,
          color: AppColors.backgroundWhite,
          size: iconSize,
        ),
      ),
    );
  }
}

class StationDetailHeroImageResolver {
  StationDetailHeroImageResolver({MediaUrlResolver? resolver})
      : _resolver = resolver ?? MediaUrlResolver();

  final MediaUrlResolver _resolver;

  /// Prefer the station photo for the detail hero — stamp art belongs elsewhere.
  String? resolve(String? stampPreviewUrl, String? imageUrl) {
    return _resolver.resolve(imageUrl ?? stampPreviewUrl);
  }
}
