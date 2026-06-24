import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import '../widgets/scan_action_buttons.dart';
import '../widgets/scan_flow_scope.dart';

class ScanErrorScreen extends StatelessWidget {
  const ScanErrorScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ScanFlowScope(
      child: BlocListener<ScanFlowCubit, ScanFlowState>(
        listenWhen: (previous, current) =>
            previous.phase != current.phase &&
            current.phase == ScanFlowPhase.success,
        listener: (context, state) {
          context.push(RouteNames.scanSuccess);
        },
        child: Scaffold(
          backgroundColor: AppColors.backgroundWhite,
          appBar: AppBar(
            backgroundColor: AppColors.backgroundWhite,
            foregroundColor: AppColors.textPrimary,
            title: const Text('Không thể thu stamp'),
            leading: IconButton(
              icon: const Icon(Icons.close_rounded),
              onPressed: () => _returnToScan(context),
            ),
          ),
          body: BlocBuilder<ScanFlowCubit, ScanFlowState>(
            builder: (context, state) {
              if (state.phase == ScanFlowPhase.checkingCollectStatus) {
                return const Center(
                  child:
                      CircularProgressIndicator(color: AppColors.primaryBlue),
                );
              }

              final title = _titleForPhase(state.phase);
              final message = state.statusMessage ??
                  state.failure?.message ??
                  'Đã xảy ra lỗi khi thu thập stamp.';

              return Padding(
                padding: const EdgeInsets.all(AppSpacing.xl),
                child: Column(
                  children: [
                    Icon(
                      _iconForPhase(state.phase),
                      size: 72,
                      color: AppColors.accentRed,
                    ),
                    const SizedBox(height: AppSpacing.lg),
                    Text(
                      title,
                      style: AppTextStyles.headlineMedium.copyWith(
                        color: AppColors.accentRed,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    Text(
                      message,
                      style: AppTextStyles.bodyLarge,
                      textAlign: TextAlign.center,
                    ),
                    const Spacer(),
                    if (state.phase == ScanFlowPhase.duplicate) ...[
                      ScanPrimaryButton(
                        label: 'Mở Sổ stamp',
                        onPressed: () {
                          context.read<ScanFlowCubit>().resetFlow();
                          context.go(RouteNames.stampBook);
                        },
                      ),
                      const SizedBox(height: AppSpacing.sm),
                      if (state.resolvedStation?.id != null)
                        ScanOutlineButton(
                          label: 'Xem chi tiết ga',
                          onPressed: () {
                            final stationId = state.resolvedStation!.id;
                            context.read<ScanFlowCubit>().resetFlow();
                            context.push(RouteNames.stationDetail(stationId));
                          },
                        ),
                    ] else if (state.isUncertainOutcome ||
                        state.phase == ScanFlowPhase.networkError) ...[
                      if (state.idempotencyKey != null)
                        ScanPrimaryButton(
                          label: 'Kiểm tra trạng thái',
                          onPressed: () => context
                              .read<ScanFlowCubit>()
                              .checkCollectStatus(),
                        ),
                      if (state.idempotencyKey != null)
                        const SizedBox(height: AppSpacing.sm),
                      ScanOutlineButton(
                        label: 'Kiểm tra Sổ stamp',
                        onPressed: () {
                          context.read<ScanFlowCubit>().resetFlow();
                          context.go(RouteNames.stampBook);
                        },
                      ),
                      const SizedBox(height: AppSpacing.sm),
                      ScanOutlineButton(
                        label: 'Thử lại',
                        onPressed: () => _returnToScan(context),
                      ),
                    ] else
                      ScanPrimaryButton(
                        label: 'Thử lại',
                        onPressed: () => _returnToScan(context),
                      ),
                  ],
                ),
              );
            },
          ),
        ),
      ),
    );
  }

  void _returnToScan(BuildContext context) {
    context.read<ScanFlowCubit>().resetFlow();
    context.go(RouteNames.scan);
  }

  String _titleForPhase(ScanFlowPhase phase) {
    return switch (phase) {
      ScanFlowPhase.duplicate => 'Stamp đã được thu',
      ScanFlowPhase.invalidTag => 'Tag NFC không hợp lệ',
      ScanFlowPhase.qrExpired => 'Mã QR không còn hiệu lực',
      ScanFlowPhase.gpsOutsideRange => 'Ngoài phạm vi ga',
      ScanFlowPhase.stationInactive => 'Ga không hoạt động',
      ScanFlowPhase.campaignInactive => 'Chiến dịch không khả dụng',
      ScanFlowPhase.networkError => 'Lỗi kết nối',
      ScanFlowPhase.locationPermissionDenied => 'Cần quyền vị trí',
      ScanFlowPhase.locationServiceDisabled => 'Bật dịch vụ vị trí',
      ScanFlowPhase.locationLowAccuracy => 'GPS không đủ chính xác',
      ScanFlowPhase.locationTimeout => 'Không lấy được vị trí',
      _ => 'Không thể thu stamp',
    };
  }

  IconData _iconForPhase(ScanFlowPhase phase) {
    return switch (phase) {
      ScanFlowPhase.duplicate => Icons.collections_bookmark_outlined,
      ScanFlowPhase.gpsOutsideRange => Icons.location_off_outlined,
      ScanFlowPhase.locationPermissionDenied =>
        Icons.location_disabled_outlined,
      ScanFlowPhase.locationServiceDisabled => Icons.location_off_outlined,
      ScanFlowPhase.locationLowAccuracy => Icons.gps_not_fixed_outlined,
      ScanFlowPhase.locationTimeout => Icons.timer_off_outlined,
      ScanFlowPhase.networkError => Icons.wifi_off_rounded,
      _ => Icons.error_outline_rounded,
    };
  }
}
