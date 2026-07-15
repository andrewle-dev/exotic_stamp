import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_icons.dart';
import '../../../../shared/widgets/app_screen_header.dart';

class HomeHeader extends StatelessWidget {
  const HomeHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return AppScreenHeader.brand(
      title: 'Metro Stamp',
      actionIcon: AppIcons.settings,
      actionTooltip: 'Settings',
      onAction: () => context.push(RouteNames.settings),
    );
  }
}
