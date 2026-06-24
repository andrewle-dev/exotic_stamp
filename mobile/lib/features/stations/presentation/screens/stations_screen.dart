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
import '../../domain/entities/station.dart';
import '../../domain/usecases/get_lines_usecase.dart';
import '../../domain/usecases/get_stations_usecase.dart';
import '../cubit/stations_cubit.dart';
import '../cubit/stations_state.dart';
import '../widgets/station_list_tile.dart';

class StationsScreen extends StatelessWidget {
  const StationsScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final StationsCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<StationsCubit>.value(
        value: cubit!,
        child: const _StationsView(),
      );
    }

    return BlocProvider(
      create: (_) => StationsCubit(
        getLinesUseCase: GetLinesUseCase(Injection.instance.stationsRepository),
        getStationsUseCase:
            GetStationsUseCase(Injection.instance.stationsRepository),
      )..load(),
      child: const _StationsView(),
    );
  }
}

class _StationsView extends StatefulWidget {
  const _StationsView();

  @override
  State<_StationsView> createState() => _StationsViewState();
}

class _StationsViewState extends State<_StationsView> {
  final TextEditingController _searchController = TextEditingController();
  Position? _currentPosition;
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
        return;
      }

      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }

      if (permission == LocationPermission.denied ||
          permission == LocationPermission.deniedForever) {
        return;
      }

      final position = await Geolocator.getCurrentPosition();
      if (mounted) {
        setState(() => _currentPosition = position);
      }
    } catch (_) {
      // Distance is optional; hide when unavailable.
    }
  }

  void _onSearchChanged(String value) {
    _searchDebounce?.cancel();
    _searchDebounce = Timer(const Duration(milliseconds: 300), () {
      context.read<StationsCubit>().updateSearch(value);
    });
  }

  String? _distanceLabel(Station station) {
    final position = _currentPosition;
    final lat = station.latitude;
    final lng = station.longitude;
    if (position == null || lat == null || lng == null) {
      return null;
    }

    final meters = Geolocator.distanceBetween(
      position.latitude,
      position.longitude,
      lat,
      lng,
    );
    if (meters < 1000) {
      return '${meters.round()} m';
    }
    return '${(meters / 1000).toStringAsFixed(1)} km';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(
                AppSpacing.lg,
                AppSpacing.sm,
                AppSpacing.lg,
                AppSpacing.sm,
              ),
              child: Text(
                'Ga metro',
                style: AppTextStyles.headlineMedium.copyWith(
                  color: AppColors.primaryBlue,
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg),
              child: TextField(
                controller: _searchController,
                onChanged: _onSearchChanged,
                decoration: InputDecoration(
                  hintText: 'Tìm ga theo tên hoặc mã',
                  prefixIcon: const Icon(Icons.search),
                  filled: true,
                  fillColor: AppColors.surface,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                    borderSide: const BorderSide(color: AppColors.border),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                    borderSide: const BorderSide(color: AppColors.border),
                  ),
                ),
              ),
            ),
            const SizedBox(height: AppSpacing.sm),
            BlocBuilder<StationsCubit, StationsState>(
              buildWhen: (previous, current) =>
                  previous.lines != current.lines ||
                  previous.selectedLineId != current.selectedLineId,
              builder: (context, state) {
                return LineFilterChips(
                  lines: state.lines,
                  selectedLineId: state.selectedLineId,
                  onSelected: context.read<StationsCubit>().selectLine,
                );
              },
            ),
            const SizedBox(height: AppSpacing.sm),
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
                    case StationsStatus.loaded:
                      return _buildList(context, state);
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

  Widget _buildList(BuildContext context, StationsState state) {
    if (state.stations.isEmpty) {
      return AppEmptyState(
        title: 'Không có ga',
        message: state.searchQuery.isNotEmpty
            ? 'Không tìm thấy ga phù hợp với từ khóa.'
            : 'Chưa có ga nào cho tuyến đã chọn.',
        icon: Icons.train_outlined,
        actionLabel: state.searchQuery.isNotEmpty ? 'Xóa tìm kiếm' : null,
        onAction: state.searchQuery.isNotEmpty
            ? () {
                _searchController.clear();
                context.read<StationsCubit>().updateSearch('');
              }
            : null,
      );
    }

    final stations = [...state.stations];
    if (_currentPosition != null) {
      stations.sort((a, b) {
        final da = _distanceMeters(a) ?? double.infinity;
        final db = _distanceMeters(b) ?? double.infinity;
        return da.compareTo(db);
      });
    }

    return RefreshIndicator(
      color: AppColors.primaryBlue,
      onRefresh: () => context.read<StationsCubit>().load(),
      child: ListView.separated(
        physics: const AlwaysScrollableScrollPhysics(),
        itemCount: stations.length,
        separatorBuilder: (_, __) => const Divider(height: 1),
        itemBuilder: (context, index) {
          final station = stations[index];
          return StationListTile(
            station: station,
            distanceLabel: _distanceLabel(station),
            onTap: () => context.push(RouteNames.stationDetail(station.id)),
          );
        },
      ),
    );
  }

  double? _distanceMeters(Station station) {
    final position = _currentPosition;
    final lat = station.latitude;
    final lng = station.longitude;
    if (position == null || lat == null || lng == null) {
      return null;
    }
    return Geolocator.distanceBetween(
      position.latitude,
      position.longitude,
      lat,
      lng,
    );
  }
}
