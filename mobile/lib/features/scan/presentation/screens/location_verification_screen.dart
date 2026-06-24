import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import '../widgets/scan_action_buttons.dart';
import '../widgets/scan_flow_scope.dart';

class LocationVerificationScreen extends StatelessWidget {
  const LocationVerificationScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ScanFlowScope(
      child: Scaffold(
        backgroundColor: AppColors.backgroundWhite,
        appBar: AppBar(
          backgroundColor: AppColors.backgroundWhite,
          foregroundColor: AppColors.textPrimary,
          title: const Text('Xác minh vị trí'),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back_ios_new_rounded),
            onPressed: () => context.pop(),
          ),
        ),
        body: BlocBuilder<ScanFlowCubit, ScanFlowState>(
          builder: (context, state) {
            final station = state.resolvedStation;
            final gps = state.gpsReading;
            if (station == null || gps == null) {
              return const Center(
                  child: Text('Không có dữ liệu ga để xác minh.'));
            }

            final mediaResolver = MediaUrlResolver();
            final imageUrl = mediaResolver.resolve(
              station.stampPreviewUrl ?? station.imageUrl,
            );

            return Padding(
              padding: const EdgeInsets.all(AppSpacing.xl),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (imageUrl != null)
                    ClipRRect(
                      borderRadius: BorderRadius.circular(18),
                      child: Image.network(
                        imageUrl,
                        height: 160,
                        width: double.infinity,
                        fit: BoxFit.cover,
                      ),
                    ),
                  const SizedBox(height: AppSpacing.lg),
                  Text(
                    station.name,
                    style: AppTextStyles.headlineMedium.copyWith(
                      color: AppColors.primaryBlue,
                    ),
                  ),
                  if (station.lineName != null) ...[
                    const SizedBox(height: AppSpacing.xs),
                    Text(station.lineName!, style: AppTextStyles.bodyMedium),
                  ],
                  const SizedBox(height: AppSpacing.lg),
                  Text(
                    'GPS: ${gps.latitude.toStringAsFixed(5)}, ${gps.longitude.toStringAsFixed(5)}',
                    style: AppTextStyles.bodyMedium,
                  ),
                  Text(
                    'Độ chính xác: ${gps.accuracyMeters.toStringAsFixed(1)} m',
                    style: AppTextStyles.bodyMedium,
                  ),
                  if (station.zoneRadiusMeters != null) ...[
                    const SizedBox(height: AppSpacing.xs),
                    Text(
                      'Phạm vi ga: ${station.zoneRadiusMeters} m (xác minh bởi máy chủ)',
                      style: AppTextStyles.caption,
                    ),
                  ],
                  const Spacer(),
                  ScanPrimaryButton(
                    label: 'Thu thập stamp',
                    onPressed: state.phase == ScanFlowPhase.collecting
                        ? null
                        : () => context.read<ScanFlowCubit>().confirmCollect(),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
