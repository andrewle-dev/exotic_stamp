import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';
import '../utils/scan_error_presentation.dart';
import '../widgets/scan_action_buttons.dart';
import '../widgets/scan_flow_listener.dart';
import '../widgets/scan_flow_scope.dart';

class ScanErrorScreen extends StatelessWidget {
  const ScanErrorScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ScanFlowScope(
      child: ScanFlowListener(
        child: Scaffold(
          backgroundColor: AppColors.backgroundWhite,
          appBar: AppSecondaryAppBar(
            title: 'Không thể thu stamp',
            showBottomDivider: false,
            onBack: () => _returnHome(context),
          ),
          body: BlocBuilder<ScanFlowCubit, ScanFlowState>(
            builder: (context, state) {
              if (state.phase == ScanFlowPhase.checkingCollectStatus) {
                return const AppLoadingView(message: 'Đang kiểm tra...');
              }

              final presentation = ScanErrorPresentation.forPhase(state);

              return Padding(
                padding: const EdgeInsets.all(AppSpacing.xxl),
                child: Column(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppSpacing.lg,
                        vertical: AppSpacing.sm,
                      ),
                      decoration: BoxDecoration(
                        color: AppColors.redTint,
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        presentation.tagLabel,
                        style: AppTextStyles.caption.copyWith(
                          color: AppColors.accentRed,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                    const SizedBox(height: AppSpacing.xl),
                    Icon(
                      presentation.icon,
                      size: 72,
                      color: AppColors.accentRed,
                    ),
                    const SizedBox(height: AppSpacing.xl),
                    Text(
                      presentation.title,
                      style: AppTextStyles.headlineMedium.copyWith(
                        color: AppColors.accentRed,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: AppSpacing.md),
                    Text(
                      presentation.message,
                      style: AppTextStyles.bodyLarge,
                      textAlign: TextAlign.center,
                    ),
                    const Spacer(),
                    ..._buildActions(context, state, presentation),
                  ],
                ),
              );
            },
          ),
        ),
      ),
    );
  }

  List<Widget> _buildActions(
    BuildContext context,
    ScanFlowState state,
    ScanErrorPresentation presentation,
  ) {
    if (state.phase == ScanFlowPhase.duplicate) {
      return [
        ScanPrimaryButton(
          label: presentation.primaryActionLabel,
          onPressed: () {
            context.read<ScanFlowCubit>().resetFlow();
            context.go(RouteNames.stampBook);
          },
        ),
        const SizedBox(height: AppSpacing.md),
        ScanOutlineButton(
          label: presentation.secondaryActionLabel,
          onPressed: () => _returnToStation(context, state),
        ),
      ];
    }

    if (state.isUncertainOutcome || state.phase == ScanFlowPhase.networkError) {
      return [
        if (state.idempotencyKey != null)
          ScanPrimaryButton(
            label: presentation.primaryActionLabel,
            onPressed: () =>
                context.read<ScanFlowCubit>().checkCollectStatus(),
          ),
        if (state.idempotencyKey != null)
          const SizedBox(height: AppSpacing.md),
        ScanOutlineButton(
          label: 'Kiểm tra Sổ stamp',
          onPressed: () {
            context.read<ScanFlowCubit>().resetFlow();
            context.go(RouteNames.stampBook);
          },
        ),
        const SizedBox(height: AppSpacing.md),
        ScanOutlineButton(
          label: 'Thử lại',
          onPressed: () => _retryScan(context),
        ),
        const SizedBox(height: AppSpacing.md),
        ScanOutlineButton(
          label: presentation.secondaryActionLabel,
          onPressed: () => _returnHome(context),
        ),
      ];
    }

    return [
      ScanPrimaryButton(
        label: presentation.primaryActionLabel,
        onPressed: () => _retryScan(context),
      ),
      const SizedBox(height: AppSpacing.md),
      ScanOutlineButton(
        label: presentation.secondaryActionLabel,
        onPressed: () => _returnToStation(context, state),
      ),
    ];
  }

  void _retryScan(BuildContext context) {
    context.read<ScanFlowCubit>().resetFlow();
    context.go(RouteNames.scan);
  }

  void _returnHome(BuildContext context) {
    context.read<ScanFlowCubit>().resetFlow();
    context.go(RouteNames.home);
  }

  void _returnToStation(BuildContext context, ScanFlowState state) {
    final stationId = state.resolvedStation?.id;
    context.read<ScanFlowCubit>().resetFlow();
    if (stationId != null) {
      context.go(RouteNames.stationDetail(stationId));
      return;
    }
    context.go(RouteNames.home);
  }
}
