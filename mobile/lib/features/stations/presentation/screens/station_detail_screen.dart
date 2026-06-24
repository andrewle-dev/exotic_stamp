import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../../../shared/widgets/app_empty_state.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/entities/station_collected_status.dart';
import '../../domain/entities/station_detail.dart';
import '../../domain/usecases/get_station_detail_usecase.dart';
import '../cubit/station_detail_cubit.dart';
import '../cubit/station_detail_state.dart';

class StationDetailScreen extends StatelessWidget {
  const StationDetailScreen({
    super.key,
    required this.stationId,
  });

  final String stationId;

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => StationDetailCubit(
        getStationDetailUseCase:
            GetStationDetailUseCase(Injection.instance.stationsRepository),
        stationId: stationId,
      )..load(),
      child: const _StationDetailView(),
    );
  }
}

class _StationDetailView extends StatelessWidget {
  const _StationDetailView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        title: const Text('Chi tiết ga'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded),
          onPressed: () => context.pop(),
        ),
      ),
      body: BlocBuilder<StationDetailCubit, StationDetailState>(
        builder: (context, state) {
          switch (state.status) {
            case StationDetailStatus.initial:
            case StationDetailStatus.loading:
              return const AppLoadingView(message: 'Đang tải chi tiết ga...');
            case StationDetailStatus.notFound:
              return AppEmptyState(
                title: 'Không tìm thấy ga',
                message: 'Ga này không tồn tại hoặc đã bị gỡ.',
                icon: Icons.location_off_outlined,
                actionLabel: 'Quay lại',
                onAction: () => context.pop(),
              );
            case StationDetailStatus.failure:
              return AppErrorView(
                message: state.failure?.message ?? 'Không thể tải chi tiết ga.',
                failure: state.failure,
                onRetry: () => context.read<StationDetailCubit>().load(),
              );
            case StationDetailStatus.loaded:
              return _StationDetailContent(detail: state.detail!);
          }
        },
      ),
    );
  }
}

class _StationDetailContent extends StatelessWidget {
  const _StationDetailContent({required this.detail});

  final StationDetail detail;

  @override
  Widget build(BuildContext context) {
    final mediaUrlResolver = MediaUrlResolver();
    final imageUrl = mediaUrlResolver.resolve(
      detail.stampPreviewUrl ?? detail.imageUrl,
    );

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (imageUrl != null)
            ClipRRect(
              borderRadius: BorderRadius.circular(18),
              child: Image.network(
                imageUrl,
                height: 180,
                width: double.infinity,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => _imagePlaceholder(),
              ),
            )
          else
            _imagePlaceholder(),
          const SizedBox(height: AppSpacing.lg),
          Text(
            detail.label,
            style: AppTextStyles.headlineMedium.copyWith(
              color: AppColors.primaryBlue,
            ),
          ),
          if (detail.lineName != null) ...[
            const SizedBox(height: AppSpacing.xs),
            Text(
              detail.lineName!,
              style: AppTextStyles.bodyMedium,
            ),
          ],
          const SizedBox(height: AppSpacing.md),
          if (detail.collectedStatus != StationCollectedStatus.unknown) ...[
            _StatusRow(
              label: detail.isCollected ? 'Đã thu stamp' : 'Chưa thu stamp',
              isPositive: detail.isCollected,
            ),
          ] else ...[
            const _StatusRow(
              label: 'Trạng thái stamp chưa khả dụng',
              isPositive: false,
              muted: true,
            ),
          ],
          if (!detail.isActive) ...[
            const SizedBox(height: AppSpacing.xs),
            const _StatusRow(
              label: 'Ga không hoạt động',
              isPositive: false,
            ),
          ],
          if (detail.address != null) ...[
            const SizedBox(height: AppSpacing.lg),
            const Text('Địa chỉ', style: AppTextStyles.titleMedium),
            const SizedBox(height: AppSpacing.xs),
            Text(detail.address!, style: AppTextStyles.bodyLarge),
          ],
          if (detail.description != null) ...[
            const SizedBox(height: AppSpacing.lg),
            const Text('Mô tả', style: AppTextStyles.titleMedium),
            const SizedBox(height: AppSpacing.xs),
            Text(detail.description!, style: AppTextStyles.bodyLarge),
          ],
          if (detail.latitude != null && detail.longitude != null) ...[
            const SizedBox(height: AppSpacing.xl),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                onPressed: () => _openDirections(
                  detail.latitude!,
                  detail.longitude!,
                ),
                icon: const Icon(Icons.directions_outlined),
                label: const Text('Chỉ đường'),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _imagePlaceholder() {
    return Container(
      height: 180,
      width: double.infinity,
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: const Icon(
        Icons.train_outlined,
        size: 48,
        color: AppColors.primaryBlue,
      ),
    );
  }

  Future<void> _openDirections(double lat, double lng) async {
    final uri = Uri.parse(
      'https://www.google.com/maps/dir/?api=1&destination=$lat,$lng',
    );
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }
}

class _StatusRow extends StatelessWidget {
  const _StatusRow({
    required this.label,
    required this.isPositive,
    this.muted = false,
  });

  final String label;
  final bool isPositive;
  final bool muted;

  @override
  Widget build(BuildContext context) {
    final color = muted
        ? AppColors.textSecondary
        : isPositive
            ? AppColors.primaryBlue
            : AppColors.accentRed;

    return Row(
      children: [
        Icon(
          muted
              ? Icons.info_outline
              : isPositive
                  ? Icons.check_circle_outline
                  : Icons.radio_button_unchecked,
          color: color,
          size: 20,
        ),
        const SizedBox(width: AppSpacing.xs),
        Text(
          label,
          style: AppTextStyles.bodyMedium.copyWith(
            color: color,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}
