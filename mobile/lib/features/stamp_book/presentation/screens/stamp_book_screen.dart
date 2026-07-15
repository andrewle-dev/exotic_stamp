import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../../../shared/widgets/app_empty_state.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_page_scaffold.dart';
import '../../../../shared/widgets/stamp_tile.dart';
import '../../../stations/domain/usecases/get_lines_usecase.dart';
import '../../domain/usecases/get_stamp_book_usecase.dart';
import '../cubit/stamp_book_cubit.dart';
import '../cubit/stamp_book_state.dart';
import '../utils/stamp_book_line_filter.dart';
import '../widgets/stamp_book_footer.dart';
import '../widgets/stamp_book_header.dart';
import '../widgets/stamp_book_refresh_listener.dart';
import '../widgets/stamp_book_summary_card.dart';
import '../widgets/stamp_line_filter_bar.dart';

class StampBookScreen extends StatelessWidget {
  const StampBookScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final StampBookCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<StampBookCubit>.value(
        value: cubit!,
        child: const _StampBookView(),
      );
    }

    return BlocProvider(
      create: (_) => StampBookCubit(
        getStampBookUseCase: GetStampBookUseCase(
          Injection.instance.stampBookRepository,
        ),
        getLinesUseCase: GetLinesUseCase(
          Injection.instance.stationsRepository,
        ),
      ),
      child: const StampBookRefreshListener(
        child: _StampBookView(),
      ),
    );
  }
}

class _StampBookView extends StatelessWidget {
  const _StampBookView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: BlocBuilder<StampBookCubit, StampBookState>(
          builder: (context, state) {
            switch (state.status) {
              case StampBookStatus.initial:
              case StampBookStatus.loading:
                return const AppLoadingView(message: 'Đang tải Sổ stamp...');
              case StampBookStatus.failure:
                return _StampBookFailureView(state: state);
              case StampBookStatus.empty:
              case StampBookStatus.loaded:
                return _StampBookContent(state: state);
            }
          },
        ),
      ),
    );
  }
}

class _StampBookFailureView extends StatelessWidget {
  const _StampBookFailureView({required this.state});

  final StampBookState state;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(
        AppSpacing.xl,
        AppSpacing.md,
        AppSpacing.xl,
        AppSpacing.xl,
      ),
      child: Column(
        children: [
          if (state.lines.isNotEmpty) ...[
            const StampBookHeader(),
            const SizedBox(height: AppSpacing.xl),
            StampLineFilterBar(
              lines: state.lines,
              selectedLineId: state.selectedLineId,
              onSelected: context.read<StampBookCubit>().selectLine,
            ),
            const SizedBox(height: AppSpacing.xl),
          ],
          Expanded(
            child: AppErrorView(
              message: state.failure?.message ?? 'Không thể tải Sổ stamp.',
              onRetry: () => context.read<StampBookCubit>().load(),
            ),
          ),
        ],
      ),
    );
  }
}

class _StampBookContent extends StatelessWidget {
  const _StampBookContent({required this.state});

  final StampBookState state;

  @override
  Widget build(BuildContext context) {
    final stampBook = state.stampBook;
    if (stampBook == null) {
      return const AppLoadingView();
    }

    final stations = [...stampBook.stations]
      ..sort((a, b) => a.sequence.compareTo(b.sequence));
    final resolver = MediaUrlResolver();
    final showEmptyCatalogue = stations.isEmpty;

    return RefreshIndicator(
      color: AppColors.primaryBlue,
      onRefresh: () => context.read<StampBookCubit>().refresh(),
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.xl,
          AppSpacing.md,
          AppSpacing.xl,
          AppPageScaffold.shellBottomInset,
        ),
        children: [
          const StampBookHeader(),
          const SizedBox(height: AppSpacing.xl),
          StampBookSummaryCard(stampBook: stampBook),
          const SizedBox(height: AppSpacing.xl),
          if (state.lines.isNotEmpty) ...[
            StampLineFilterBar(
              lines: state.lines,
              selectedLineId: state.selectedLineId,
              onSelected: context.read<StampBookCubit>().selectLine,
            ),
            const SizedBox(height: AppSpacing.xl),
          ],
          if (showEmptyCatalogue)
            const AppEmptyState(
              title: 'Chưa có stamp',
              message: 'Danh sách stamp của tuyến này hiện trống.',
              icon: Icons.collections_bookmark_outlined,
            )
          else
            GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: stations.length,
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                crossAxisSpacing: AppSpacing.md,
                mainAxisSpacing: AppSpacing.lg,
                childAspectRatio: 0.72,
              ),
              itemBuilder: (context, index) {
                final item = stations[index];
                return StampTile(
                  item: item,
                  imageUrl: resolver.resolve(item.stampDesignUrl),
                  lineAccentColor: accentForLineSelection(
                    selectedLineId: state.selectedLineId,
                    lines: state.lines,
                    sequence: item.sequence,
                  ),
                  onTap: () => context.push(
                    RouteNames.stampDetail(item.stationId),
                    extra: state.selectedLineId,
                  ),
                );
              },
            ),
          const SizedBox(height: AppSpacing.lg),
          StampBookFooter(stations: stations),
        ],
      ),
    );
  }
}
