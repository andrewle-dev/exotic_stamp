import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../features/memories/domain/entities/photo_share_context.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/entities/stamp_detail.dart';
import '../../domain/usecases/get_stamp_detail_usecase.dart';
import '../cubit/stamp_detail_cubit.dart';
import '../cubit/stamp_detail_state.dart';
import '../widgets/stamp_detail_sections.dart';

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
        child: const _StampDetailView(),
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
      child: const _StampDetailView(),
    );
  }
}

class _StampDetailView extends StatelessWidget {
  const _StampDetailView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: const AppSecondaryAppBar(
        title: 'Chi tiết Stamp',
        showBottomDivider: false,
      ),
      body: BlocBuilder<StampDetailCubit, StampDetailState>(
        builder: (context, state) {
          switch (state.status) {
            case StampDetailStatus.initial:
            case StampDetailStatus.loading:
              return const AppLoadingView(
                message: 'Đang tải chi tiết stamp...',
              );
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

  @override
  Widget build(BuildContext context) {
    final stampImageUrl = resolveStampDetailMedia(detail.stampDesignUrl);
    final description = detail.stampDesignDescription?.trim() ?? '';
    final hasCollectedMeta =
        detail.collected && (detail.collectedAt != null || detail.nfcVerified);

    return Column(
      children: [
        Expanded(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(
              AppSpacing.xxl,
              AppSpacing.md,
              AppSpacing.xxl,
              AppSpacing.xl,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                StampDetailHeroCard(
                  detail: detail,
                  stampImageUrl: stampImageUrl,
                ),
                const SizedBox(height: AppSpacing.xl),
                StampDetailHeader(detail: detail),
                const SizedBox(height: AppSpacing.xl),
                if (detail.collected && hasCollectedMeta)
                  StampCollectedMetaCard(detail: detail)
                else if (!detail.collected)
                  const StampUnlockHintCard(),
                if (description.isNotEmpty) ...[
                  const SizedBox(height: AppSpacing.lg),
                  StampAboutSection(description: description),
                ],
                const SizedBox(height: AppSpacing.lg),
                StampInfoSection(detail: detail),
                if (detail.stationStory != null &&
                    detail.stationStory!.trim().isNotEmpty) ...[
                  const SizedBox(height: AppSpacing.lg),
                  StampDetailStorySection(story: detail.stationStory!.trim()),
                ],
                if (detail.collectionProgress != null) ...[
                  const SizedBox(height: AppSpacing.lg),
                  StampCollectionProgressCard(
                    progress: detail.collectionProgress!,
                  ),
                ],
              ],
            ),
          ),
        ),
        _StampDetailActions(detail: detail),
      ],
    );
  }
}

class _StampDetailActions extends StatelessWidget {
  const _StampDetailActions({required this.detail});

  final StampDetail detail;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: EdgeInsets.fromLTRB(
        AppSpacing.xxl,
        AppSpacing.lg,
        AppSpacing.xxl,
        AppSpacing.lg + MediaQuery.paddingOf(context).bottom,
      ),
      decoration: const BoxDecoration(
        color: AppColors.backgroundWhite,
        border: Border(top: BorderSide(color: AppColors.border)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (detail.collected) ...[
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
                padding: const EdgeInsets.symmetric(vertical: AppSpacing.lg),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(999),
                ),
              ),
              child: Text(
                'Chia sẻ Stamp',
                style: AppTextStyles.buttonLabel.copyWith(
                  color: AppColors.backgroundWhite,
                ),
              ),
            ),
            const SizedBox(height: AppSpacing.md),
          ],
          OutlinedButton(
            onPressed: () =>
                context.push(RouteNames.stationDetail(detail.stationId)),
            style: OutlinedButton.styleFrom(
              foregroundColor: AppColors.primaryBlue,
              side: const BorderSide(color: AppColors.primaryBlue, width: 1.5),
              padding: const EdgeInsets.symmetric(vertical: AppSpacing.lg),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(999),
              ),
            ),
            child: Text(
              'Xem nhà ga',
              style: AppTextStyles.buttonLabel.copyWith(
                color: AppColors.primaryBlue,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
