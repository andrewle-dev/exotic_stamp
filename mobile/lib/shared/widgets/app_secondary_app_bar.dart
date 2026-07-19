import 'package:flutter/material.dart';

import '../../app/router/route_names.dart';
import '../../app/theme/app_colors.dart';
import '../../app/theme/app_typography.dart';
import 'app_back_button.dart';

/// Preferred-size AppBar for pushed / secondary screens.
///
/// Pattern: simple [AppBackButton] + title, **no logo**, no circular back chrome.
/// Matches [AppScreenHeader.secondary] visual language inside Material [AppBar].
class AppSecondaryAppBar extends StatelessWidget implements PreferredSizeWidget {
  const AppSecondaryAppBar({
    super.key,
    required this.title,
    this.fallbackRoute = RouteNames.home,
    this.onBack,
    this.actions,
    this.showBottomDivider = true,
  });

  final String title;
  final String fallbackRoute;
  final VoidCallback? onBack;
  final List<Widget>? actions;
  final bool showBottomDivider;

  static const double _dividerHeight = 1;

  @override
  Size get preferredSize => Size.fromHeight(
        AppBackButton.minTapTarget +
            (showBottomDivider ? _dividerHeight : 0),
      );

  @override
  Widget build(BuildContext context) {
    return AppBar(
      backgroundColor: AppColors.backgroundWhite,
      foregroundColor: AppColors.textPrimary,
      elevation: 0,
      scrolledUnderElevation: 0,
      automaticallyImplyLeading: false,
      leadingWidth: AppBackButton.minTapTarget,
      leading: AppBackButton(
        onPressed: onBack,
        fallbackRoute: fallbackRoute,
      ),
      title: Text(
        title,
        style: AppTextStyles.appTitle,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      titleSpacing: 8,
      centerTitle: false,
      actions: actions,
      bottom: showBottomDivider
          ? const PreferredSize(
              preferredSize: Size.fromHeight(_dividerHeight),
              child: Divider(height: _dividerHeight, color: AppColors.border),
            )
          : null,
    );
  }
}
