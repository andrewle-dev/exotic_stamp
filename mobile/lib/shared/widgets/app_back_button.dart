import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../app/router/route_names.dart';
import '../../app/theme/app_colors.dart';
import '../../app/theme/app_icons.dart';

/// Simple icon-only back control for secondary screens.
///
/// Transparent background — no circular border or filled container.
/// Pops when [GoRouterState] can pop; otherwise navigates to [fallbackRoute].
class AppBackButton extends StatelessWidget {
  const AppBackButton({
    super.key,
    this.onPressed,
    this.fallbackRoute = RouteNames.home,
    this.iconColor,
    this.iconSize = 24,
    this.tooltip = 'Back',
  });

  /// Material / a11y minimum tap target.
  static const double minTapTarget = 44;

  final VoidCallback? onPressed;
  final String fallbackRoute;
  final Color? iconColor;
  final double iconSize;
  final String tooltip;

  void _handlePressed(BuildContext context) {
    if (onPressed != null) {
      onPressed!();
      return;
    }
    if (context.canPop()) {
      context.pop();
      return;
    }
    context.go(fallbackRoute);
  }

  @override
  Widget build(BuildContext context) {
    return IconButton(
      tooltip: tooltip,
      onPressed: () => _handlePressed(context),
      iconSize: iconSize,
      color: iconColor ?? AppColors.textPrimary,
      padding: EdgeInsets.zero,
      constraints: const BoxConstraints(
        minWidth: minTapTarget,
        minHeight: minTapTarget,
      ),
      icon: Icon(AppIcons.back, size: iconSize),
    );
  }
}
