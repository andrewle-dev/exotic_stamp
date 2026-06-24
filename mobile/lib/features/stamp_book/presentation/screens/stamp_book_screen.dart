import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_empty_state.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../stations/domain/usecases/get_lines_usecase.dart';
import '../../domain/usecases/get_stamp_book_usecase.dart';
import '../cubit/stamp_book_cubit.dart';
import '../cubit/stamp_book_state.dart';
import '../../domain/entities/stamp_item.dart';
import '../widgets/stamp_book_summary_card.dart';
import '../widgets/stamp_grid_item.dart';
import '../widgets/stamp_book_refresh_listener.dart';
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
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        title: const Text.rich(
          TextSpan(
            children: [
              TextSpan(
                text: 'Sổ ',
                style: TextStyle(color: AppColors.accentRed),
              ),
              TextSpan(
                text: 'stamp',
                style: TextStyle(color: AppColors.primaryBlue),
              ),
            ],
          ),
        ),
      ),
      body: BlocBuilder<StampBookCubit, StampBookState>(
        builder: (context, state) {
          switch (state.status) {
            case StampBookStatus.initial:
            case StampBookStatus.loading:
              return const AppLoadingView(message: 'Đang tải Sổ stamp...');
            case StampBookStatus.failure:
              return AppErrorView(
                message: state.failure?.message ?? 'Không thể tải Sổ stamp.',
                onRetry: () => context.read<StampBookCubit>().load(),
              );
            case StampBookStatus.empty:
            case StampBookStatus.loaded:
              return _StampBookContent(state: state);
          }
        },
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

    if (state.status == StampBookStatus.empty) {
      return RefreshIndicator(
        color: AppColors.primaryBlue,
        onRefresh: () => context.read<StampBookCubit>().refresh(),
        child: ListView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            StampBookSummaryCard(stampBook: stampBook),
            const SizedBox(height: AppSpacing.lg),
            if (state.lines.isNotEmpty) ...[
              StampLineFilterBar(
                lines: state.lines,
                selectedLineId: state.selectedLineId,
                onSelected: context.read<StampBookCubit>().selectLine,
              ),
              const SizedBox(height: AppSpacing.lg),
            ],
            const AppEmptyState(
              title: 'Chưa có stamp',
              message: 'Bạn chưa có stamp nào',
              icon: Icons.collections_bookmark_outlined,
            ),
            const SizedBox(height: AppSpacing.lg),
            _HintFooter(stations: stampBook.stations),
          ],
        ),
      );
    }

    final stations = [...stampBook.stations]
      ..sort((a, b) => a.sequence.compareTo(b.sequence));

    return RefreshIndicator(
      color: AppColors.primaryBlue,
      onRefresh: () => context.read<StampBookCubit>().refresh(),
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(AppSpacing.lg),
        children: [
          StampBookSummaryCard(stampBook: stampBook),
          const SizedBox(height: AppSpacing.lg),
          if (state.lines.isNotEmpty) ...[
            StampLineFilterBar(
              lines: state.lines,
              selectedLineId: state.selectedLineId,
              onSelected: context.read<StampBookCubit>().selectLine,
            ),
            const SizedBox(height: AppSpacing.lg),
          ],
          GridView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: stations.length,
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              crossAxisSpacing: AppSpacing.sm,
              mainAxisSpacing: AppSpacing.md,
              childAspectRatio: 0.72,
            ),
            itemBuilder: (context, index) {
              final item = stations[index];
              return StampGridItem(
                item: item,
                onTap: () => context.push(
                  RouteNames.stampDetail(item.stationId),
                  extra: state.selectedLineId,
                ),
              );
            },
          ),
          const SizedBox(height: AppSpacing.xl),
          _HintFooter(stations: stations),
        ],
      ),
    );
  }
}

class _HintFooter extends StatelessWidget {
  const _HintFooter({required this.stations});

  final List<StampItem> stations;

  @override
  Widget build(BuildContext context) {
    final hasUncollected = stations.any((station) => !station.collected);
    if (!hasUncollected) {
      return const SizedBox.shrink();
    }

    return Text.rich(
      TextSpan(
        style: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.textSecondary,
        ),
        children: const [
          TextSpan(text: 'Ghé ga và '),
          TextSpan(
            text: 'chạm NFC',
            style: TextStyle(
              color: AppColors.primaryBlue,
              fontWeight: FontWeight.w700,
            ),
          ),
          TextSpan(
              text:
                  ' để thu stamp còn thiếu. QR chỉ dùng khi NFC không khả dụng.'),
        ],
      ),
      textAlign: TextAlign.center,
    );
  }
}
