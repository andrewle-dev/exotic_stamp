import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_dimensions.dart';
import '../../../../app/theme/app_icons.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_shadows.dart';
import '../../../../app/theme/app_typography.dart';
import 'shell_nav_metrics.dart';
import 'smooth_concave_notched_rectangle.dart';

/// Bottom navigation matching the Visily / premium expected-result mock.
///
/// Layout: Home | Stamp | (scan FAB gap) | Stations | Profile
class BottomNavBar extends StatelessWidget {
  const BottomNavBar({
    required this.currentIndex,
    required this.onTabSelected,
    super.key,
  });

  final int currentIndex;
  final ValueChanged<int> onTabSelected;

  static const _tabs = [
    _ShellTab(
      index: ShellTabIndex.home,
      icon: AppIcons.home,
      activeIcon: AppIcons.homeActive,
      label: 'Home',
    ),
    _ShellTab(
      index: ShellTabIndex.book,
      icon: AppIcons.stamp,
      activeIcon: AppIcons.stampActive,
      label: 'Stamp',
      useFontAwesome: true,
    ),
    _ShellTab(
      index: ShellTabIndex.stations,
      icon: AppIcons.stations,
      activeIcon: AppIcons.stationsActive,
      label: 'Stations',
    ),
    _ShellTab(
      index: ShellTabIndex.profile,
      icon: AppIcons.profile,
      activeIcon: AppIcons.profileActive,
      label: 'Profile',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return BottomAppBar(
      height: AppDimensions.bottomNavHeight,
      padding: EdgeInsets.zero,
      elevation: 10,
      shadowColor: AppShadows.bottomNav.first.color,
      color: AppColors.backgroundWhite,
      surfaceTintColor: AppColors.backgroundWhite,
      shape: const SmoothConcaveNotchedRectangle(
        cornerRadius: ShellNavMetrics.barTopRadius,
        lipRadius: ShellNavMetrics.notchLipRadius,
      ),
      notchMargin: ShellNavMetrics.notchMargin,
      child: Row(
        children: [
          for (var i = 0; i < _tabs.length; i++) ...[
            if (i == 2) const SizedBox(width: ShellNavMetrics.fabGap),
            Expanded(
              child: _ShellNavItem(
                tab: _tabs[i],
                isActive: currentIndex == _tabs[i].index,
                onTap: () => onTabSelected(_tabs[i].index),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _ShellNavItem extends StatelessWidget {
  const _ShellNavItem({
    required this.tab,
    required this.isActive,
    required this.onTap,
  });

  final _ShellTab tab;
  final bool isActive;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final color =
        isActive ? AppColors.activeIcon : AppColors.inactiveIcon;
    final iconData = isActive ? tab.activeIcon : tab.icon;

    return Center(
      child: SizedBox(
        width: ShellNavMetrics.tabTapWidth,
        height: ShellNavMetrics.tabTapHeight,
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            onTap: onTap,
            borderRadius: AppRadius.mdAll,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                if (tab.useFontAwesome)
                  FaIcon(
                    iconData,
                    size: ShellNavMetrics.tabIconSize - 2,
                    color: color,
                  )
                else
                  Icon(
                    iconData,
                    size: ShellNavMetrics.tabIconSize,
                    color: color,
                  ),
                const SizedBox(height: ShellNavMetrics.tabIconLabelGap),
                Text(
                  tab.label,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  textAlign: TextAlign.center,
                  style: AppTextStyles.navLabel.copyWith(
                    color: color,
                    fontSize: 12.5,
                    fontWeight: isActive ? FontWeight.w600 : FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _ShellTab {
  const _ShellTab({
    required this.index,
    required this.icon,
    required this.activeIcon,
    required this.label,
    this.useFontAwesome = false,
  });

  final int index;
  final IconData icon;
  final IconData activeIcon;
  final String label;
  final bool useFontAwesome;
}

/// Legacy export — prefer [BottomNavBar].
@Deprecated('Use BottomNavBar from bottom_nav_bar.dart')
typedef AppBottomNavBar = BottomNavBar;
