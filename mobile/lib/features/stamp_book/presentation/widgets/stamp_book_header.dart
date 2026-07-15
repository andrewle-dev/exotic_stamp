import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_icons.dart';
import '../../../../shared/widgets/app_screen_header.dart';

class StampBookHeader extends StatelessWidget {
  const StampBookHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return AppScreenHeader.title(
      title: 'Stamp Book',
      actionIcon: AppIcons.search,
      actionTooltip: 'Search stations',
      onAction: () => context.go(RouteNames.stations),
    );
  }
}
