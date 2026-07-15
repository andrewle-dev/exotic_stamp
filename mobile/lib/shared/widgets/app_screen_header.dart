import 'package:flutter/material.dart';

import '../../app/theme/app_dimensions.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_typography.dart';
import 'app_action_button.dart';
import 'app_logo.dart';

/// Shared top-level screen header for shell tabs.
///
/// Layout: `[logo] [title] … [optional action]`
class AppScreenHeader extends StatelessWidget {
  const AppScreenHeader({
    super.key,
    required this.title,
    this.showLogo = true,
    this.actionIcon,
    this.onAction,
    this.actionTooltip,
    this.trailing,
  });

  /// Home brand chrome (logo + product title + trailing).
  factory AppScreenHeader.brand({
    Key? key,
    String title = 'Metro Stamp',
    IconData? actionIcon,
    VoidCallback? onAction,
    String? actionTooltip,
    Widget? trailing,
  }) {
    return AppScreenHeader(
      key: key,
      title: title,
      showLogo: true,
      actionIcon: trailing == null ? actionIcon : null,
      onAction: trailing == null ? onAction : null,
      actionTooltip: actionTooltip,
      trailing: trailing,
    );
  }

  /// Regular titled chrome (still shows brand logo for consistency).
  factory AppScreenHeader.title({
    Key? key,
    required String title,
    IconData? actionIcon,
    VoidCallback? onAction,
    String? actionTooltip,
    Widget? trailing,
  }) {
    return AppScreenHeader(
      key: key,
      title: title,
      showLogo: true,
      actionIcon: trailing == null ? actionIcon : null,
      onAction: trailing == null ? onAction : null,
      actionTooltip: actionTooltip,
      trailing: trailing,
    );
  }

  final String title;
  final bool showLogo;
  final IconData? actionIcon;
  final VoidCallback? onAction;
  final String? actionTooltip;
  final Widget? trailing;

  static const double horizontalPadding = AppDimensions.screenHorizontalPadding;
  static const double topPadding = AppSpacing.md;
  static const double logoSize = AppDimensions.headerMinHeight;

  @override
  Widget build(BuildContext context) {
    final trailingWidget = trailing ??
        (actionIcon == null
            ? null
            : AppActionButton(
                icon: actionIcon!,
                onPressed: onAction,
                tooltip: actionTooltip,
              ));

    return SizedBox(
      height: AppDimensions.headerMinHeight,
      child: Row(
        children: [
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
