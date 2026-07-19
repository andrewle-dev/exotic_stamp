import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:geolocator/geolocator.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../widgets/gps_status_card.dart';
import '../widgets/scan_action_buttons.dart';
import '../widgets/scan_flow_listener.dart';
import '../widgets/scan_flow_scope.dart';

class LocationVerificationScreen extends StatelessWidget {
  const LocationVerificationScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ScanFlowScope(
      child: ScanFlowListener(
        child: Scaffold(
          backgroundColor: AppColors.backgroundWhite,
          appBar: const AppSecondaryAppBar(
            title: 'Xác minh vị trí',
            showBottomDivider: false,
          ),
          body: BlocBuilder<ScanFlowCubit, ScanFlowState>(
            builder: (context, state) {
              final station = state.resolvedStation;
              final gps = state.gpsReading;

              if (state.phase == ScanFlowPhase.checkingLocation ||
                  state.phase == ScanFlowPhase.resolvingStation ||
                  state.phase == ScanFlowPhase.collecting) {
                return const AppLoadingView(message: 'Đang xác minh vị trí...');
              }

              if (station == null) {
                return const Center(
                  child: Text('Không có dữ liệu ga để xác minh.'),
                );
              }

              final needsPermission =
                  state.phase == ScanFlowPhase.locationPermissionDenied ||
                      state.phase == ScanFlowPhase.locationServiceDisabled;
              final hasGps = gps != null;
              final accuracyOk = hasGps && gps.accuracyMeters <= 50;

              return SingleChildScrollView(
                padding: const EdgeInsets.all(AppSpacing.xxl),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _LocationMapGraphic(stationName: station.name),
                    const SizedBox(height: AppSpacing.xl),
                    Text(
                      'Xác minh bạn đang ở ga',
                      style: AppTextStyles.displayMedium.copyWith(
                        color: AppColors.primaryBlue,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.md),
                    Text(
                      station.name,
                      style: AppTextStyles.cardTitle,
                    ),
                    if (station.lineName != null) ...[
                      const SizedBox(height: AppSpacing.sm),
                      Text(
                        station.lineName!,
                        style: AppTextStyles.bodyMedium.copyWith(
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ],
                    const SizedBox(height: AppSpacing.xl),
                    Text(
                      'Ứng dụng dùng GPS để kiểm tra bạn đang ở gần ga '
                      'trước khi thu stamp.',
                      style: AppTextStyles.bodyMedium.copyWith(
                        color: AppColors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.xl),
                    GpsStatusCard(
                      title: 'Vị trí hiện tại',
                      subtitle: hasGps
                          ? '${gps.latitude.toStringAsFixed(5)}, ${gps.longitude.toStringAsFixed(5)}'
                          : state.statusMessage ?? 'Chưa có tọa độ GPS',
                      icon: Icons.my_location_rounded,
                      statusLabel: hasGps ? 'Đã lấy' : 'Chưa có',
                      isPositive: hasGps,
                    ),
                    const SizedBox(height: AppSpacing.md),
                    GpsStatusCard(
                      title: 'Độ chính xác GPS',
                      subtitle: hasGps
                          ? '${gps.accuracyMeters.toStringAsFixed(0)} m'
                          : 'Cần quyền vị trí để tiếp tục',
                      icon: Icons.gps_fixed_rounded,
                      statusLabel: hasGps
                          ? (accuracyOk ? 'Tốt' : 'Thấp')
                          : 'Chưa có',
                      isPositive: hasGps && accuracyOk,
                    ),
                    if (station.zoneRadiusMeters != null) ...[
                      const SizedBox(height: AppSpacing.md),
                      GpsStatusCard(
                        title: 'Phạm vi ga',
                        subtitle:
                            'Bán kính khoảng ${station.zoneRadiusMeters} m quanh ga',
                        icon: Icons.radar_rounded,
                        statusLabel: 'Tham khảo',
                        isPositive: true,
                      ),
                    ],
                    const SizedBox(height: AppSpacing.xxxl),
                    if (needsPermission)
                      ScanPrimaryButton(
                        label: 'Cho phép vị trí',
                        onPressed: () async {
                          if (state.phase ==
                              ScanFlowPhase.locationServiceDisabled) {
                            await Geolocator.openLocationSettings();
                          } else {
                            await Geolocator.openAppSettings();
                          }
                          if (context.mounted) {
                            await context
                                .read<ScanFlowCubit>()
                                .refreshLocation();
                          }
                        },
                      ),
                    if (needsPermission) const SizedBox(height: AppSpacing.md),
                    ScanOutlineButton(
                      label: 'Thử lại',
                      onPressed: () =>
                          context.read<ScanFlowCubit>().refreshLocation(),
                    ),
                    const SizedBox(height: AppSpacing.md),
                    ScanPrimaryButton(
                      label: 'Thu thập stamp',
                      onPressed: hasGps &&
                              state.phase != ScanFlowPhase.collecting &&
                              !needsPermission
                          ? () =>
                              context.read<ScanFlowCubit>().confirmCollect()
                          : null,
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
}

class _LocationMapGraphic extends StatelessWidget {
  const _LocationMapGraphic({required this.stationName});

  final String stationName;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 180,
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: AppRadius.xlAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Stack(
        alignment: Alignment.center,
        children: [
          Icon(
            Icons.map_outlined,
            size: 120,
            color: AppColors.primaryBlue.withValues(alpha: 0.15),
          ),
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.location_on_rounded,
                color: AppColors.primaryBlue,
                size: 36,
              ),
              const SizedBox(height: AppSpacing.sm),
              Text(
                stationName,
                style: AppTextStyles.labelLarge.copyWith(
                  color: AppColors.primaryBlue,
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ],
      ),
    );
  }
}
