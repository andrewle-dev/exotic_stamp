import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:share_plus/share_plus.dart';

import '../../../../app/router/stamp_book_route_refresh.dart';
import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/collect_stamp_result.dart';
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
            final mediaResolver = MediaUrlResolver();
            final stampImageUrl =
                mediaResolver.resolve(stamp?.stampDesignUrl);

            return Padding(
              padding: const EdgeInsets.all(AppSpacing.xxl),
              child: Column(
                children: [
                  const Spacer(),
                  _StampSuccessCard(
                    stationName: stamp?.stationName ?? 'Stamp mới',
                    lineName: stamp?.lineName,
                    imageUrl: stampImageUrl,
                    collectedAt: stamp?.collectedAt,
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  Text(
                    'Nhận stamp thành công!',
                    style: AppTextStyles.displayMedium.copyWith(
                      color: AppColors.primaryBlue,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  if (progress != null) ...[
                    const SizedBox(height: AppSpacing.xl),
                    _ProgressCard(progress: progress),
                  ],
                  if (result?.nextRewardHint != null) ...[
                    const SizedBox(height: AppSpacing.lg),
                    Text(
                      result!.nextRewardHint!,
                      style: AppTextStyles.bodyMedium.copyWith(
                        color: AppColors.textSecondary,
                      ),
                      textAlign: TextAlign.center,
                    ),
                  ],
                  const Spacer(),
                  ScanPrimaryButton(
                    label: 'Xem Stamp Book',
                    onPressed: () {
                      context.read<ScanFlowCubit>().resetFlow();
                      context.go(
                        StampBookRouteRefresh.locationWithRefresh(
                          StampBookRouteRefresh.newRefreshToken(),
                        ),
                      );
                    },
                  ),
                  const SizedBox(height: AppSpacing.md),
                  ScanOutlineButton(
                    label: 'Chia sẻ',
                    onPressed: stamp == null
                        ? null
                        : () {
                            Share.share(
                              'Tôi vừa nhận stamp tại ${stamp.stationName} '
                              'trên Exotic Stamp!',
                            );
                          },
                  ),
                  const SizedBox(height: AppSpacing.md),
                  ScanOutlineButton(
                    label: 'Quét tiếp',
                    onPressed: () {
                      context.read<ScanFlowCubit>().resetFlow();
                      context.go(RouteNames.scanTapToCollect);
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

class _StampSuccessCard extends StatelessWidget {
  const _StampSuccessCard({
    required this.stationName,
    this.lineName,
    this.imageUrl,
    this.collectedAt,
  });

  final String stationName;
  final String? lineName;
  final String? imageUrl;
  final DateTime? collectedAt;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          if (imageUrl != null)
            ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: Image.network(
                imageUrl!,
                height: 120,
                width: 120,
                fit: BoxFit.cover,
              ),
            )
          else
            Container(
              height: 120,
              width: 120,
              decoration: BoxDecoration(
                color: AppColors.backgroundWhite,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.border),
              ),
              child: const Icon(
                Icons.verified_rounded,
                size: 56,
                color: AppColors.primaryBlue,
              ),
            ),
          const SizedBox(height: AppSpacing.lg),
          Text(
            stationName,
            style: AppTextStyles.sectionTitle.copyWith(
              color: AppColors.primaryBlue,
            ),
            textAlign: TextAlign.center,
          ),
          if (lineName != null) ...[
            const SizedBox(height: AppSpacing.sm),
            Text(lineName!, style: AppTextStyles.bodyMedium),
          ],
          if (collectedAt != null) ...[
            const SizedBox(height: AppSpacing.sm),
            Text(
              'Thu lúc ${collectedAt!.hour.toString().padLeft(2, '0')}:'
              '${collectedAt!.minute.toString().padLeft(2, '0')}',
              style: AppTextStyles.caption.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _ProgressCard extends StatelessWidget {
  const _ProgressCard({required this.progress});

  final CollectStampProgress progress;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Tiến độ tuyến', style: AppTextStyles.labelLarge),
          const SizedBox(height: AppSpacing.md),
          LinearProgressIndicator(
            value: progress.total == 0 ? 0 : progress.collected / progress.total,
            backgroundColor: AppColors.border,
            color: AppColors.primaryBlue,
            minHeight: 8,
            borderRadius: BorderRadius.circular(999),
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            '${progress.collected}/${progress.total} ga (${progress.percentage}%)',
            style: AppTextStyles.bodyMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}
