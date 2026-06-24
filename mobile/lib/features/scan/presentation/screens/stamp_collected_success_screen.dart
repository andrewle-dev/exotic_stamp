import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/rewards_route_refresh.dart';
import '../../../../app/router/stamp_book_route_refresh.dart';
import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import '../widgets/scan_action_buttons.dart';
import '../widgets/scan_flow_scope.dart';

class StampCollectedSuccessScreen extends StatelessWidget {
  const StampCollectedSuccessScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final ScanFlowCubit? cubit;

  @override
  Widget build(BuildContext context) {
    final body = Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: BlocBuilder<ScanFlowCubit, ScanFlowState>(
          builder: (context, state) {
            final result = state.collectResult;
            final stamp = result?.stamp;
            final progress = result?.progress;

            return Padding(
              padding: const EdgeInsets.all(AppSpacing.xl),
              child: Column(
                children: [
                  const Spacer(),
                  const Icon(
                    Icons.verified_rounded,
                    size: 88,
                    color: AppColors.primaryBlue,
                  ),
                  const SizedBox(height: AppSpacing.lg),
                  Text(
                    'Thu thập thành công!',
                    style: AppTextStyles.headlineMedium.copyWith(
                      color: AppColors.primaryBlue,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  Text(
                    stamp?.stationName ?? 'Stamp mới',
                    style: AppTextStyles.titleMedium,
                    textAlign: TextAlign.center,
                  ),
                  if (progress != null) ...[
                    const SizedBox(height: AppSpacing.md),
                    Text(
                      'Tiến độ: ${progress.collected}/${progress.total} (${progress.percentage}%)',
                      style: AppTextStyles.bodyLarge,
                      textAlign: TextAlign.center,
                    ),
                  ],
                  const Spacer(),
                  ScanPrimaryButton(
                    label: 'Xem Sổ stamp',
                    onPressed: () {
                      context.read<ScanFlowCubit>().resetFlow();
                      context.go(
                        StampBookRouteRefresh.locationWithRefresh(
                          StampBookRouteRefresh.newRefreshToken(),
                        ),
                      );
                    },
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  ScanOutlineButton(
                    label: 'Xem phần thưởng',
                    onPressed: () {
                      context.read<ScanFlowCubit>().resetFlow();
                      context.go(
                        RewardsRouteRefresh.locationWithRefresh(
                          RewardsRouteRefresh.newRefreshToken(),
                        ),
                      );
                    },
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  ScanOutlineButton(
                    label: 'Về trang chủ',
                    onPressed: () {
                      context.read<ScanFlowCubit>().resetFlow();
                      context.go(RouteNames.home);
                    },
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );

    if (cubit != null) {
      return BlocProvider<ScanFlowCubit>.value(
        value: cubit!,
        child: body,
      );
    }

    return ScanFlowScope(child: body);
  }
}
