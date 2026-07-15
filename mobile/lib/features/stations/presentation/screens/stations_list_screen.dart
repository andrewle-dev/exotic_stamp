import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:geolocator/geolocator.dart';
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
import '../../../../shared/widgets/app_page_scaffold.dart';
import '../../../../shared/widgets/app_version_footer.dart';
import '../../domain/usecases/get_lines_usecase.dart';
import '../../domain/usecases/get_stations_usecase.dart';
import '../cubit/stations_cubit.dart';
import '../cubit/stations_state.dart';
import '../utils/stations_line_filter.dart';
import '../utils/stations_list_presenter.dart';
import '../widgets/nearby_station_hero_card.dart';
import '../widgets/station_directory_row.dart';
import '../widgets/stations_gps_banner.dart';
import '../widgets/stations_header.dart';
import '../widgets/stations_line_filter_bar.dart';
import '../widgets/stations_search_field.dart';

class StationsListScreen extends StatelessWidget {
  const StationsListScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final StationsCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<StationsCubit>.value(
        value: cubit!,
        child: const _StationsListView(),
      );
    }

    return BlocProvider(
      create: (_) => StationsCubit(
        getLinesUseCase: GetLinesUseCase(Injection.instance.stationsRepository),
        getStationsUseCase:
            GetStationsUseCase(Injection.instance.stationsRepository),
      )..load(),
      child: const _StationsListView(),
    );
  }
}

class _StationsListView extends StatefulWidget {
  const _StationsListView();

  @override
  State<_StationsListView> createState() => _StationsListViewState();
}

class _StationsListViewState extends State<_StationsListView> {
  final TextEditingController _searchController = TextEditingController();
  Timer? _searchDebounce;

  @override
  void initState() {
    super.initState();
    _resolveLocation();
  }

  @override
  void dispose() {
    _searchDebounce?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _resolveLocation() async {
    try {
      final serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        if (mounted) {
          context.read<StationsCubit>().markGpsDisabled();
        }
        return;
      }

      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }

      if (permission == LocationPermission.denied ||
          permission == LocationPermission.deniedForever) {
        if (mounted) {
          context.read<StationsCubit>().markGpsDisabled();
        }
        return;
      }

      final position = await Geolocator.getCurrentPosition();
      if (mounted) {
        context.read<StationsCubit>().updateUserLocation(
              latitude: position.latitude,
              longitude: position.longitude,
            );
      }
    } catch (_) {
      if (mounted) {
        context.read<StationsCubit>().markGpsDisabled();
      }
    }
  }

  void _onSearchChanged(String value) {
    _searchDebounce?.cancel();
    _searchDebounce = Timer(const Duration(milliseconds: 300), () {
      context.read<StationsCubit>().updateSearch(value);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Padding(
              padding: EdgeInsets.fromLTRB(
                AppSpacing.xl,
                AppSpacing.md,
                AppSpacing.xl,
                AppSpacing.md,
              ),
              child: StationsHeader(),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.xl),
              child: StationsSearchField(
                controller: _searchController,
                onChanged: _onSearchChanged,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.xl),
              child: BlocBuilder<StationsCubit, StationsState>(
                buildWhen: (previous, current) =>
                    previous.lines != current.lines ||
                    previous.selectedLineId != current.selectedLineId,
                builder: (context, state) {
                  return StationsLineFilterBar(
                    lines: state.lines,
                    selectedLineId: state.selectedLineId,
                    onSelected: context.read<StationsCubit>().selectLine,
                  );
                },
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            Expanded(
              child: BlocBuilder<StationsCubit, StationsState>(
                builder: (context, state) {
                  switch (state.status) {
                    case StationsStatus.initial:
                    case StationsStatus.loading:
                      return const AppLoadingView(
                        message: 'Đang tải danh sách ga...',
                      );
                    case StationsStatus.failure:
                      return _buildFailure(context, state.failure);
                    case StationsStatus.emptySearch:
                      return _buildEmptySearch(context, state);
                    case StationsStatus.loaded:
                    case StationsStatus.gpsDisabled:
                      return _buildContent(context, state);
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFailure(BuildContext context, Failure? failure) {
    return AppErrorView(
      message: failure?.message ?? 'Không thể tải danh sách ga.',
      failure: failure,
      onRetry: () => context.read<StationsCubit>().load(),
    );
  }

  Widget _buildEmptySearch(BuildContext context, StationsState state) {
    return AppEmptyState(
      title: 'Không tìm thấy ga',
      message: 'Không có ga phù hợp với từ khóa "${state.searchQuery}".',
      icon: Icons.search_off_outlined,
      actionLabel: 'Xóa tìm kiếm',
      onAction: () {
        _searchController.clear();
        context.read<StationsCubit>().updateSearch('');
      },
    );
  }

  Widget _buildContent(BuildContext context, StationsState state) {
    if (state.stations.isEmpty) {
      return const AppEmptyState(
        title: 'Không có ga',
        message: 'Chưa có ga nào cho tuyến đã chọn.',
        icon: Icons.train_outlined,
      );
    }

    final sorted = StationsListPresenter.sortedByDistance(
      stations: state.stations,
      userLatitude: state.userLatitude,
      userLongitude: state.userLongitude,
    );
    final nearest = StationsListPresenter.nearestStation(
      sortedStations: sorted,
      userLatitude: state.userLatitude,
      userLongitude: state.userLongitude,
    );
    final directory = StationsListPresenter.directoryStations(
      sortedStations: sorted,
      nearest: nearest,
    );
    final lineEndLabel = _lineEndLabel(state);

    return RefreshIndicator(
      color: AppColors.primaryBlue,
      onRefresh: () => context.read<StationsCubit>().load(),
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.xl,
          AppSpacing.md,
          AppSpacing.xl,
          AppPageScaffold.shellBottomInset,
        ),
        children: [
          if (state.status == StationsStatus.gpsDisabled) ...[
            const StationsGpsBanner(),
            const SizedBox(height: AppSpacing.lg),
          ],
          if (nearest != null) ...[
            Row(
              children: [
                const Expanded(
                  child: Text(
                    'Nearby Stations',
                    style: AppTextStyles.titleMedium,
                  ),
                ),
                TextButton(
                  onPressed: () {},
                  child: const Text('View Map'),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.md),
            NearbyStationHeroCard(
              station: nearest,
              distanceAwayLabel: StationsListPresenter.distanceAwayLabel(
                station: nearest,
                userLatitude: state.userLatitude,
                userLongitude: state.userLongitude,
              ),
              lineLabel: nearest.lineName ?? 'Line 1',
              onTap: () => context.push(RouteNames.stationDetail(nearest.id)),
            ),
            const SizedBox(height: AppSpacing.xxl),
          ],
          Row(
            children: [
              const Expanded(
                child: Text(
                  'Station Directory',
                  style: AppTextStyles.titleMedium,
                ),
              ),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    'Sorted by distance',
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.textSecondary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const Icon(
                    Icons.keyboard_arrow_down_rounded,
                    color: AppColors.textSecondary,
                    size: 18,
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.lg),
          for (final station in directory) ...[
            StationDirectoryRow(
              station: station,
              distanceLabel: StationsListPresenter.distanceLabel(
                station: station,
                userLatitude: state.userLatitude,
                userLongitude: state.userLongitude,
              ),
              lineBadgeLabel: station.lineName ?? 'Line 1',
              onTap: () => context.push(RouteNames.stationDetail(station.id)),
            ),
            const SizedBox(height: AppSpacing.md),
          ],
          if (lineEndLabel != null) StationsLineEndFooter(lineLabel: lineEndLabel),
          const AppVersionFooter(),
        ],
      ),
    );
  }

  String? _lineEndLabel(StationsState state) {
    if (state.selectedLineId == null ||
        state.selectedLineId == StationsLineFilter.allLines) {
      return null;
    }
    final line = state.selectedLine;
    return line?.displayName ?? line?.name ?? 'Line';
  }
}
