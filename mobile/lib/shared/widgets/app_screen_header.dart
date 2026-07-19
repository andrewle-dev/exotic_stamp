import 'package:flutter/material.dart';

import '../../app/router/route_names.dart';
import '../../app/theme/app_dimensions.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_typography.dart';
import 'app_action_button.dart';
import 'app_back_button.dart';
import 'app_logo.dart';

/// Shared top-level / secondary screen header.
///
/// Top-level tabs use [AppScreenHeader.brand] / [AppScreenHeader.title] (logo).
/// Pushed detail screens use [AppScreenHeader.secondary] (back + title, no logo).
class AppScreenHeader extends StatelessWidget {
  const AppScreenHeader({
    super.key,
    required this.title,
    this.showLogo = true,
    this.showBackButton = false,
    this.onBack,
    this.fallbackRoute = RouteNames.home,
    this.leading,
    this.actionIcon,
    this.onAction,
    this.actionTooltip,
    this.trailing,
  });

  /// Home brand chrome (logo + product title + trailing).
  factory AppScreenHeader.brand({
    Key? key,
    String title = 'Metro Stamp',
    bool showBackButton = false,
    VoidCallback? onBack,
    String fallbackRoute = RouteNames.home,
    Widget? leading,
    IconData? actionIcon,
    VoidCallback? onAction,
    String? actionTooltip,
    Widget? trailing,
  }) {
    return AppScreenHeader(
      key: key,
      title: title,
      showLogo: true,
      showBackButton: showBackButton,
      onBack: onBack,
      fallbackRoute: fallbackRoute,
      leading: leading,
      actionIcon: trailing == null ? actionIcon : null,
      onAction: trailing == null ? onAction : null,
      actionTooltip: actionTooltip,
      trailing: trailing,
    );
  }

  /// Top-level titled chrome (logo + title). Used by Stamp Book, Stations, Profile.
  factory AppScreenHeader.title({
    Key? key,
    required String title,
    bool showBackButton = false,
    VoidCallback? onBack,
    String fallbackRoute = RouteNames.home,
    Widget? leading,
    IconData? actionIcon,
    VoidCallback? onAction,
    String? actionTooltip,
    Widget? trailing,
  }) {
    return AppScreenHeader(
      key: key,
      title: title,
      showLogo: true,
      showBackButton: showBackButton,
      onBack: onBack,
      fallbackRoute: fallbackRoute,
      leading: leading,
      actionIcon: trailing == null ? actionIcon : null,
      onAction: trailing == null ? onAction : null,
      actionTooltip: actionTooltip,
      trailing: trailing,
    );
  }

  /// Secondary / detail chrome: `[Back] [Title] … [optional trailing]`.
  ///
  /// No app logo. Back uses the simple [AppBackButton] style.
  factory AppScreenHeader.secondary({
    Key? key,
    required String title,
    VoidCallback? onBack,
    String fallbackRoute = RouteNames.home,
    Widget? leading,
    IconData? actionIcon,
    VoidCallback? onAction,
    String? actionTooltip,
    Widget? trailing,
  }) {
    return AppScreenHeader(
      key: key,
      title: title,
      showLogo: false,
      showBackButton: true,
      onBack: onBack,
      fallbackRoute: fallbackRoute,
      leading: leading,
      actionIcon: trailing == null ? actionIcon : null,
      onAction: trailing == null ? onAction : null,
      actionTooltip: actionTooltip,
      trailing: trailing,
    );
  }

  final String title;
  final bool showLogo;
  final bool showBackButton;
  final VoidCallback? onBack;
  final String fallbackRoute;
  final Widget? leading;
  final IconData? actionIcon;
  final VoidCallback? onAction;
  final String? actionTooltip;
  final Widget? trailing;

  static const double horizontalPadding = AppDimensions.screenHorizontalPadding;
  static const double topPadding = AppSpacing.md;
  static const double logoSize = AppDimensions.headerMinHeight;

  Widget? _resolveLeading() {
    if (leading != null) return leading;
    if (!showBackButton) return null;
    return AppBackButton(
      onPressed: onBack,
      fallbackRoute: fallbackRoute,
    );
  }

  @override
  Widget build(BuildContext context) {
    final leadingWidget = _resolveLeading();
    final trailingWidget = trailing ??
        (actionIcon == null
            ? null
            : AppActionButton(
                icon: actionIcon!,
                onPressed: onAction,
                tooltip: actionTooltip,
              ));

    final rowHeight = leadingWidget != null
        ? AppBackButton.minTapTarget
        : AppDimensions.headerMinHeight;

    return SizedBox(
      height: rowHeight,
      child: Row(
        children: [
          if (leadingWidget != null) ...[
            leadingWidget,
            const SizedBox(width: AppSpacing.sm),
          ],
          if (showLogo) ...[
            const AppLogo(size: logoSize),
            const SizedBox(width: AppSpacing.md),
          ],
          Expanded(
            child: Text(
              title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              textAlign: TextAlign.start,
              style: AppTextStyles.appTitle,
            ),
          ),
          if (trailingWidget != null) ...[
            const SizedBox(width: AppSpacing.sm),
            trailingWidget,
          ],
        ],
      ),
    );
  }
}

/// Legacy name — prefer [AppActionButton].
@Deprecated('Use AppActionButton')
typedef AppHeaderActionButton = AppActionButton;
