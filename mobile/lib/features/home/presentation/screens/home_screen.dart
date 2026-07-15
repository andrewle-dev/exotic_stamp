import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/errors/failure.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_page_scaffold.dart';
import '../../../../shared/widgets/app_version_footer.dart';
import '../../domain/usecases/get_home_summary_usecase.dart';
import '../cubit/home_cubit.dart';
import '../cubit/home_state.dart';
import '../home_reload_signal.dart';
import '../widgets/home_collect_cta.dart';
import '../widgets/home_collection_card.dart';
import '../widgets/home_header.dart';
import '../widgets/home_milestone_row.dart';
import '../widgets/home_banner_carousel.dart';
import '../widgets/home_partial_error_banner.dart';
import '../widgets/home_recent_stamps_section.dart';
import '../widgets/home_shortcut_grid.dart';
import '../widgets/home_social_proof_strip.dart';

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

class _HomeView extends StatefulWidget {
  const _HomeView();

  @override
  State<_HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<_HomeView> {
  HomeReloadSignal? _reloadSignal;
  int _lastHandledGeneration = 0;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_reloadSignal != null) {
      return;
    }
    _reloadSignal = Injection.instance.homeReloadSignal;
    _lastHandledGeneration = _reloadSignal!.generation;
    _reloadSignal!.addListener(_onReloadSignal);
  }

  void _onReloadSignal() {
    final signal = _reloadSignal;
    if (signal == null || !mounted) {
      return;
    }
    if (signal.generation == _lastHandledGeneration) {
      return;
    }
    _lastHandledGeneration = signal.generation;
    context.read<HomeCubit>().refresh();
  }

  @override
  void dispose() {
    _reloadSignal?.removeListener(_onReloadSignal);
    super.dispose();
  }

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
                AppSpacing.xl,
                AppSpacing.md,
                AppSpacing.xl,
                AppPageScaffold.shellBottomInset,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const HomeHeader(),
                  if (summary.partialErrors.isNotEmpty) ...[
                    const SizedBox(height: AppSpacing.lg),
                    HomePartialErrorBanner(messages: summary.partialErrors),
                  ],
                  const SizedBox(height: AppSpacing.xl),
                  HomeBannerCarousel(banners: summary.promotionalBanners),
                  const SizedBox(height: AppSpacing.xl),
                  if (summary.progress != null) ...[
                    HomeCollectionCard(
                      progress: summary.progress!,
                      rankTitle: summary.rankTitle,
                      rankSubtitle: summary.rankSubtitle,
                    ),
                    if (summary.milestones.isNotEmpty) ...[
                      const SizedBox(height: AppSpacing.md),
                      HomeMilestoneRow(milestones: summary.milestones),
                    ],
                  ] else
                    const HomeCollectionPlaceholder(),
                  const SizedBox(height: AppSpacing.xxl),
                  HomeRecentStampsSection(stamps: summary.recentStamps),
                  const SizedBox(height: AppSpacing.xl),
                  HomeCollectCta(
                    onTap: () => context.go(RouteNames.scanTapToCollect),
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  const HomeShortcutGrid(),
                  if (summary.socialProof != null) ...[
                    const SizedBox(height: AppSpacing.xl),
                    HomeSocialProofStrip(socialProof: summary.socialProof!),
                  ],
                  const AppVersionFooter(),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
