import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../widgets/app_bottom_nav_bar.dart';

/// Hosts the authenticated bottom-navigation shell and tab content.
class MainShellScreen extends StatelessWidget {
  const MainShellScreen({
    required this.navigationShell,
    super.key,
  });

  final StatefulNavigationShell navigationShell;

  void _onTabSelected(int index) {
    navigationShell.goBranch(
      index,
      initialLocation: index == navigationShell.currentIndex,
    );
  }

  @override
  Widget build(BuildContext context) {
    final isScanActive = navigationShell.currentIndex == ShellTabIndex.scan;

    return Scaffold(
      body: navigationShell,
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      floatingActionButton: Transform.translate(
        offset: const Offset(0, 12),
        child: Container(
          width: 78,
          height: 78,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: AppColors.backgroundWhite,
            boxShadow: [
              BoxShadow(
                color: AppColors.accentRed.withValues(alpha: 0.26),
                blurRadius: 22,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.all(6),
            child: FloatingActionButton(
              elevation: 0,
              backgroundColor: AppColors.accentRed,
              foregroundColor: AppColors.backgroundWhite,
              onPressed: () => _onTabSelected(ShellTabIndex.scan),
              shape: const CircleBorder(),
              child: Icon(
                Icons.nfc_rounded,
                size: isScanActive ? 32 : 30,
              ),
            ),
          ),
        ),
      ),
      bottomNavigationBar: AppBottomNavBar(
        currentIndex: navigationShell.currentIndex,
        onTabSelected: _onTabSelected,
      ),
    );
  }
}
