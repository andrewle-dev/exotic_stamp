import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_icons.dart';
import '../../../app_config/presentation/widgets/optional_update_host.dart';
import '../widgets/bottom_nav_bar.dart';
import '../widgets/shell_nav_metrics.dart';

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
    return OptionalUpdateHost(
      child: Scaffold(
        backgroundColor: AppColors.backgroundWhite,
        body: navigationShell,
        floatingActionButtonLocation: const ShellScanFabLocation(),
        floatingActionButton: const _ScanFab(),
        bottomNavigationBar: BottomNavBar(
          currentIndex: navigationShell.currentIndex,
          onTabSelected: _onTabSelected,
        ),
      ),
    );
  }
}

/// Center scan action: red circle + white ring, docked in the bar notch.
class _ScanFab extends StatelessWidget {
  const _ScanFab();

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: ShellNavMetrics.fabOuterSize,
      height: ShellNavMetrics.fabOuterSize,
      child: Material(
        color: AppColors.backgroundWhite,
        elevation: 4,
        shadowColor: Colors.black.withValues(alpha: 0.18),
        shape: const CircleBorder(),
        child: Padding(
          padding: const EdgeInsets.all(ShellNavMetrics.fabRing),
          child: Material(
            color: AppColors.accentRed,
            elevation: 2,
            shadowColor: AppColors.accentRed.withValues(alpha: 0.35),
            shape: const CircleBorder(),
            clipBehavior: Clip.antiAlias,
            child: InkWell(
              customBorder: const CircleBorder(),
              onTap: () => context.go(RouteNames.scanTapToCollect),
              child: const Icon(
                AppIcons.scan,
                key: Key('shell_scan_fab'),
                size: ShellNavMetrics.fabIconSize,
                color: AppColors.backgroundWhite,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
