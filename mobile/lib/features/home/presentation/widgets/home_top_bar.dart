import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_icons.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_typography.dart';
import '../../../../shared/widgets/app_action_button.dart';

/// Legacy greeting top bar — prefer [HomeHeader] / [AppScreenHeader].
@Deprecated('Use HomeHeader with AppScreenHeader.brand')
class HomeTopBar extends StatelessWidget {
  const HomeTopBar({super.key, required this.displayName});

  final String displayName;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 34,
          height: 34,
          decoration: BoxDecoration(
            color: AppColors.primaryBlue,
            borderRadius: AppRadius.mdAll,
          ),
          child: const Icon(
            Icons.workspace_premium_outlined,
            color: AppColors.backgroundWhite,
            size: 20,
          ),
        ),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              RichText(
                text: TextSpan(
                  style: AppTextStyles.displayMedium.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                  children: const [
                    TextSpan(
                      text: 'Exotic ',
                      style: TextStyle(color: AppColors.accentRed),
                    ),
                    TextSpan(
                      text: 'Home',
                      style: TextStyle(color: AppColors.primaryBlue),
                    ),
                  ],
                ),
              ),
              Text(
                'Xin chào, $displayName',
                style: AppTextStyles.body,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
        AppActionButton(
          icon: AppIcons.settings,
          onPressed: () => context.push(RouteNames.settings),
          tooltip: 'Settings',
        ),
      ],
    );
  }
}
