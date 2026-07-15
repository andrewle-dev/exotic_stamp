import 'package:flutter/material.dart';

import '../../../../app/theme/app_icons.dart';
import '../../../../shared/widgets/app_screen_header.dart';

class StationsHeader extends StatelessWidget {
  const StationsHeader({super.key, this.onFilterTap});

  final VoidCallback? onFilterTap;

  @override
  Widget build(BuildContext context) {
    return AppScreenHeader.title(
      title: 'Stations',
      actionIcon: AppIcons.filter,
      actionTooltip: 'Filter',
      onAction: onFilterTap,
    );
  }
}
