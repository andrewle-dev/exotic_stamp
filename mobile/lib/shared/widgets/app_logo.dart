import 'package:flutter/material.dart';

import '../../app/theme/app_assets.dart';
import '../../app/theme/app_colors.dart';
import '../../app/theme/app_dimensions.dart';

export '../../app/theme/app_assets.dart';

/// Transparent Exotic Stamp logo for top-level headers.
///
/// Do not wrap in a blue plate — the PNG already includes brand color.
class AppLogo extends StatelessWidget {
  const AppLogo({
    super.key,
    this.size = AppDimensions.headerMinHeight,
  });

  final double size;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size,
      height: size,
      child: Image.asset(
        AppAssets.logo,
        width: size,
        height: size,
        fit: BoxFit.contain,
        filterQuality: FilterQuality.medium,
        errorBuilder: (_, __, ___) => Icon(
          Icons.workspace_premium_outlined,
          color: AppColors.primaryBlue,
          size: size * 0.7,
        ),
      ),
    );
  }
}
