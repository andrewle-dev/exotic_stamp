import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/errors/failure.dart';
import '../../../../shared/widgets/app_empty_state.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/usecases/get_home_summary_usecase.dart';
import '../cubit/home_cubit.dart';
import '../cubit/home_state.dart';
import '../widgets/home_progress_card.dart';
import '../widgets/home_top_bar.dart';
import '../widgets/recent_stamp_card.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final HomeCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<HomeCubit>.value(
        value: cubit!,
        child: const _HomeView(),
      );
    }

    return BlocProvider(
      create: (_) => HomeCubit(
        getHomeSummaryUseCase: GetHomeSummaryUseCase(
          Injection.instance.homeRepository,
        ),
      )..load(),
      child: const _HomeView(),
    );
  }
}

class _HomeView extends StatelessWidget {
  const _HomeView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: BlocBuilder<HomeCubit, HomeState>(
          builder: (context, state) {
            switch (state.status) {
              case HomeStatus.initial:
              case HomeStatus.loading:
                return const AppLoadingView(
                  message: 'Đang tải trang chủ...',
                );
              case HomeStatus.failure:
                return _buildFailure(context, state.failure);
              case HomeStatus.loaded:
                return _buildLoaded(context, state);
            }
          },
        ),
      ),
    );
  }

  Widget _buildFailure(BuildContext context, Failure? failure) {
    final code = failure?.code;
    final message = failure?.message ?? 'Không thể tải trang chủ.';

    if (code == FailureCode.unauthorized || code == FailureCode.tokenExpired) {
      return AppErrorView(
        message: 'Phiên đăng nhập đã hết hạn.',
        failure: failure,
        onRetry: () => context.go(RouteNames.login),
        retryLabel: 'Đăng nhập lại',
      );
    }

    if (code == FailureCode.networkError) {
      return AppErrorView(
        message: message,
        failure: failure,
        onRetry: () => context.read<HomeCubit>().load(),
      );
    }

    return AppErrorView(
      message: message,
      failure: failure,
      onRetry: () => context.read<HomeCubit>().load(),
    );
  }

  Widget _buildLoaded(BuildContext context, HomeState state) {
    final summary = state.summary!;
    return RefreshIndicator(
      color: AppColors.primaryBlue,
      onRefresh: () => context.read<HomeCubit>().load(),
      child: CustomScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(
                AppSpacing.lg,
                AppSpacing.sm,
                AppSpacing.lg,
                AppSpacing.xl,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  HomeTopBar(displayName: summary.displayName),
                  if (summary.partialErrors.isNotEmpty) ...[
                    const SizedBox(height: AppSpacing.md),
                    HomePartialErrorBanner(messages: summary.partialErrors),
                  ],
                  if (summary.activeBanner != null) ...[
                    const SizedBox(height: AppSpacing.lg),
                    HomeActiveBanner(banner: summary.activeBanner!),
                  ],
                  const SizedBox(height: AppSpacing.lg),
                  if (summary.progress != null)
                    HomeProgressCard(
                      progress: summary.progress!,
                      lineName: summary.lineName,
                    )
                  else
                    const HomeProgressPlaceholder(),
                  if (summary.nextReward != null) ...[
                    const SizedBox(height: AppSpacing.lg),
                    HomeNextRewardCard(nextReward: summary.nextReward!),
                  ],
                  const SizedBox(height: AppSpacing.xl),
                  Row(
                    children: [
                      const Expanded(
                        child: Text(
                          'Stamp gần đây',
                          style: AppTextStyles.titleMedium,
                        ),
                      ),
                      TextButton(
                        onPressed: () => context.go(RouteNames.stampBook),
                        child: const Text('Xem sổ'),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  if (summary.recentStamps.isEmpty)
                    const AppEmptyState(
                      title: 'Chưa có stamp',
                      message:
                          'Chạm NFC tại ga metro để bắt đầu sưu tập stamp.',
                      icon: Icons.collections_bookmark_outlined,
                    )
                  else
                    SizedBox(
                      height: 154,
                      child: ListView.separated(
                        scrollDirection: Axis.horizontal,
                        itemCount: summary.recentStamps.length,
                        separatorBuilder: (_, __) =>
                            const SizedBox(width: AppSpacing.sm),
                        itemBuilder: (context, index) {
                          return RecentStampCard(
                            stamp: summary.recentStamps[index],
                          );
                        },
                      ),
                    ),
                  const SizedBox(height: AppSpacing.xl),
                  _ScanPromptCard(
                    onTap: () => context.go(RouteNames.scan),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ScanPromptCard extends StatelessWidget {
  const _ScanPromptCard({required this.onTap});

  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.primaryBlue,
      borderRadius: BorderRadius.circular(18),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Row(
            children: [
              const Icon(
                Icons.nfc_rounded,
                color: AppColors.backgroundWhite,
                size: 32,
              ),
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Sẵn sàng thu thập',
                      style: AppTextStyles.titleMedium.copyWith(
                        color: AppColors.backgroundWhite,
                      ),
                    ),
                    Text(
                      'Chạm NFC tại ga để nhận stamp.',
                      style: AppTextStyles.bodyMedium.copyWith(
                        color: AppColors.backgroundWhite.withValues(alpha: 0.9),
                      ),
                    ),
                  ],
                ),
              ),
              const Icon(
                Icons.chevron_right_rounded,
                color: AppColors.backgroundWhite,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
