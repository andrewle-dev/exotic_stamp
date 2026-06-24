import 'package:flutter/material.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';

/// Bottom navigation for the authenticated app shell.
///
/// Layout: Home | Book | (scan FAB gap) | Stations | Rewards | Profile
class AppBottomNavBar extends StatelessWidget {
  const AppBottomNavBar({
    required this.currentIndex,
    required this.onTabSelected,
    super.key,
  });

  final int currentIndex;
  final ValueChanged<int> onTabSelected;

  static const _leftTabs = [
    _ShellTab(
      index: ShellTabIndex.home,
      icon: Icons.home_outlined,
      label: 'Home',
    ),
    _ShellTab(
      index: ShellTabIndex.book,
      icon: Icons.book_outlined,
      label: 'Book',
    ),
  ];

  static const _rightTabs = [
    _ShellTab(
      index: ShellTabIndex.stations,
      icon: Icons.format_list_bulleted_rounded,
      label: 'Stations',
    ),
    _ShellTab(
      index: ShellTabIndex.rewards,
      icon: Icons.card_giftcard_outlined,
      label: 'Rewards',
    ),
    _ShellTab(
      index: ShellTabIndex.profile,
      icon: Icons.person_outline_rounded,
      label: 'Profile',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return BottomAppBar(
      height: 78,
      padding: const EdgeInsets.symmetric(horizontal: 8),
      color: AppColors.backgroundWhite,
      surfaceTintColor: AppColors.backgroundWhite,
      child: Row(
        children: [
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: _leftTabs
                  .map(
                    (tab) => _ShellNavItem(
                      tab: tab,
                      isActive: currentIndex == tab.index,
                      onTap: () => onTabSelected(tab.index),
                    ),
                  )
                  .toList(),
            ),
          ),
          const SizedBox(width: 56),
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: _rightTabs
                  .map(
                    (tab) => _ShellNavItem(
                      tab: tab,
                      isActive: currentIndex == tab.index,
                      onTap: () => onTabSelected(tab.index),
                    ),
                  )
                  .toList(),
            ),
          ),
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
    final color = isActive ? AppColors.primaryBlue : AppColors.textPrimary;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 6),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(tab.icon, color: color),
            const SizedBox(height: 4),
            Text(
              tab.label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w700,
                color: color,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ShellTab {
  const _ShellTab({
    required this.index,
    required this.icon,
    required this.label,
  });

  final int index;
  final IconData icon;
  final String label;
}
