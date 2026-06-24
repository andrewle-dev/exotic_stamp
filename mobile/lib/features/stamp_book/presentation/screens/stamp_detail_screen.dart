import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../features/memories/domain/entities/photo_share_context.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/entities/stamp_detail.dart';
import '../../domain/usecases/get_stamp_detail_usecase.dart';
import '../cubit/stamp_detail_cubit.dart';
import '../cubit/stamp_detail_state.dart';

class StampDetailScreen extends StatelessWidget {
  const StampDetailScreen({
    super.key,
    required this.stationId,
    this.lineId,
    this.cubit,
  });

  final String stationId;
  final String? lineId;
  final StampDetailCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<StampDetailCubit>.value(
        value: cubit!,
        child: _StampDetailView(stationId: stationId),
      );
    }

    final resolvedLineId = lineId ?? GoRouterState.of(context).extra as String?;

    return BlocProvider(
      create: (_) => StampDetailCubit(
        getStampDetailUseCase: GetStampDetailUseCase(
          Injection.instance.stampBookRepository,
        ),
        stationId: stationId,
        lineId: resolvedLineId,
      )..load(),
      child: _StampDetailView(stationId: stationId),
    );
  }
}

class _StampDetailView extends StatelessWidget {
  const _StampDetailView({required this.stationId});

  final String stationId;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        title: const Text('Chi tiết stamp'),
      ),
      body: BlocBuilder<StampDetailCubit, StampDetailState>(
        builder: (context, state) {
          switch (state.status) {
            case StampDetailStatus.initial:
            case StampDetailStatus.loading:
              return const AppLoadingView(
                  message: 'Đang tải chi tiết stamp...');
            case StampDetailStatus.failure:
              return AppErrorView(
                message:
                    state.failure?.message ?? 'Không thể tải chi tiết stamp.',
                onRetry: () => context.read<StampDetailCubit>().load(),
              );
            case StampDetailStatus.loaded:
              final detail = state.detail;
              if (detail == null) {
                return const AppLoadingView();
              }
              return _StampDetailBody(detail: detail);
          }
        },
      ),
    );
  }
}

class _StampDetailBody extends StatelessWidget {
  const _StampDetailBody({required this.detail});

  final StampDetail detail;

  static String _formatDateTime(DateTime value) {
    final local = value.toLocal();
    final day = local.day.toString().padLeft(2, '0');
    final month = local.month.toString().padLeft(2, '0');
    final year = local.year.toString();
    final hour = local.hour.toString().padLeft(2, '0');
    final minute = local.minute.toString().padLeft(2, '0');
    return '$day/$month/$year $hour:$minute';
  }

  @override
  Widget build(BuildContext context) {
    final mediaResolver = MediaUrlResolver();
    final imageUrl = mediaResolver.resolve(detail.stampDesignUrl);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppSpacing.xl),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          AspectRatio(
            aspectRatio: 1,
            child: Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: detail.collected
                      ? AppColors.primaryBlue
                      : AppColors.border,
                  width: 2,
                ),
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(18),
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    if (imageUrl != null)
                      CachedNetworkImage(
                        imageUrl: imageUrl,
                        fit: BoxFit.cover,
                      )
                    else
                      const ColoredBox(
                        color: AppColors.surface,
                        child: Icon(Icons.image_not_supported_outlined),
                      ),
                    if (!detail.collected)
                      Container(
                        color: Colors.white.withValues(alpha: 0.5),
                        child: const Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(
                                Icons.lock_outline_rounded,
                                size: 48,
                                color: AppColors.accentRed,
                              ),
                              SizedBox(height: AppSpacing.sm),
                              Text('Stamp chưa thu'),
                            ],
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.lg),
          Text(
            detail.stationName,
            style: AppTextStyles.titleLarge.copyWith(
              color: AppColors.primaryBlue,
            ),
          ),
          if (detail.lineName != null) ...[
            const SizedBox(height: AppSpacing.xs),
            Text(detail.lineName!, style: AppTextStyles.bodyLarge),
          ],
          if (detail.campaignName != null) ...[
            const SizedBox(height: AppSpacing.xs),
            Text(detail.campaignName!, style: AppTextStyles.bodyMedium),
          ],
          const SizedBox(height: AppSpacing.lg),
          if (detail.collected) ...[
            if (detail.collectedAt != null)
              _InfoRow(
                label: 'Thời gian thu',
                value: _formatDateTime(detail.collectedAt!),
              ),
            if (detail.collectMethod != null)
              _InfoRow(
                label: 'Phương thức quét',
                value: detail.collectMethod!,
              ),
            if (detail.availability == StampDetailAvailability.limited)
              Padding(
                padding: const EdgeInsets.only(top: AppSpacing.sm),
                child: Text(
                  'Một số thông tin chi tiết chưa có từ máy chủ.',
                  style: AppTextStyles.caption.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ),
          ] else
            Text(
              'Thu stamp tại ga này bằng NFC để mở khóa.',
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          const SizedBox(height: AppSpacing.xl),
          if (detail.collected)
            ElevatedButton(
              onPressed: () {
                context.push(
                  RouteNames.memoriesCreate,
                  extra: PhotoShareContext(
                    stationId: detail.stationId,
                    stationName: detail.stationName,
                    shareType: PhotoShareContext.shareTypeStampCollected,
                    stampId: detail.stampId,
                    stampDesignUrl: detail.stampDesignUrl,
                    collectedAt: detail.collectedAt,
                    lineName: detail.lineName,
                  ),
                );
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryBlue,
                foregroundColor: AppColors.backgroundWhite,
                padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
              ),
              child: const Text('Chia sẻ Stamp'),
            ),
          if (detail.collected) const SizedBox(height: AppSpacing.sm),
          OutlinedButton(
            onPressed: () =>
                context.push(RouteNames.stationDetail(detail.stationId)),
            style: OutlinedButton.styleFrom(
              foregroundColor: AppColors.primaryBlue,
              side: const BorderSide(color: AppColors.primaryBlue),
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
            ),
            child: const Text('Xem nhà ga'),
          ),
        ],
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({
    required this.label,
    required this.value,
  });

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text(
              label,
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: AppTextStyles.bodyLarge,
            ),
          ),
        ],
      ),
    );
  }
}
