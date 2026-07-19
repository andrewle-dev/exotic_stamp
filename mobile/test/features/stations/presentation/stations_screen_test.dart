import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/line.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station_collected_status.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/stations_cubit.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/stations_state.dart';
import 'package:metro_stamp_app/features/stations/presentation/screens/stations_list_screen.dart';
import 'package:metro_stamp_app/features/stations/presentation/utils/stations_line_filter.dart';
import 'package:metro_stamp_app/features/stations/presentation/widgets/station_directory_row.dart';
import 'package:metro_stamp_app/shared/widgets/app_empty_state.dart';
import 'package:metro_stamp_app/shared/widgets/app_error_view.dart';
import 'package:metro_stamp_app/shared/widgets/app_loading_view.dart';
import 'package:mocktail/mocktail.dart';

class MockStationsCubit extends MockCubit<StationsState> implements StationsCubit {}

void main() {
  late MockStationsCubit cubit;

  setUp(() {
    cubit = MockStationsCubit();
  });

  Widget buildSubject() {
    return MaterialApp(
      home: StationsListScreen(cubit: cubit),
    );
  }

  testWidgets('shows loading view', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(status: StationsStatus.loading),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(AppLoadingView), findsOneWidget);
  });

  testWidgets('shows error view', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(
        status: StationsStatus.failure,
        failure: Failure(
          code: FailureCode.networkError,
          message: 'Network down',
        ),
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(AppErrorView), findsOneWidget);
  });

  testWidgets('shows loaded station directory', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(
        status: StationsStatus.loaded,
        lines: [Line(id: 'line-1', name: 'Line 1', status: 'ACTIVE')],
        selectedLineId: StationsLineFilter.allLines,
        userLatitude: 10.77,
        userLongitude: 106.69,
        stations: [
          Station(
            id: 's1',
            lineId: 'line-1',
            code: 'S01',
            name: 'Ben Thanh',
            lineName: 'Line 1',
            latitude: 10.772,
            longitude: 106.695,
            collectedStatus: StationCollectedStatus.collected,
          ),
          Station(
            id: 's2',
            lineId: 'line-1',
            code: 'S02',
            name: 'Ba Son',
            lineName: 'Line 1',
            latitude: 10.79,
            longitude: 106.71,
            collectedStatus: StationCollectedStatus.uncollected,
          ),
        ],
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.selectLine(any())).thenAnswer((_) async {});
    when(() => cubit.updateSearch(any())).thenAnswer((_) async {});
    when(() => cubit.updateUserLocation(
          latitude: any(named: 'latitude'),
          longitude: any(named: 'longitude'),
        )).thenReturn(null);
    when(() => cubit.markGpsDisabled()).thenReturn(null);

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.text('Stations'), findsOneWidget);
    expect(find.text('Station Directory'), findsOneWidget);
    expect(find.text('Sorted by distance'), findsNothing);
    expect(find.byIcon(Icons.filter_alt_outlined), findsOneWidget);
    expect(find.byType(StationDirectoryRow), findsOneWidget);
    expect(find.text('Ben Thanh'), findsWidgets);
    expect(find.text('View Map'), findsOneWidget);
  });

  testWidgets('shows empty search state', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(
        status: StationsStatus.emptySearch,
        searchQuery: 'missing',
        lines: [Line(id: 'line-1', name: 'Line 1')],
        selectedLineId: StationsLineFilter.allLines,
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.selectLine(any())).thenAnswer((_) async {});
    when(() => cubit.updateSearch(any())).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(AppEmptyState), findsOneWidget);
    expect(find.text('Không tìm thấy ga'), findsOneWidget);
  });

  testWidgets('shows gps banner when gps disabled', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(
        status: StationsStatus.gpsDisabled,
        lines: [Line(id: 'line-1', name: 'Line 1', status: 'ACTIVE')],
        selectedLineId: StationsLineFilter.allLines,
        stations: [
          Station(
            id: 's1',
            lineId: 'line-1',
            code: 'S01',
            name: 'Ben Thanh',
            lineName: 'Line 1',
          ),
        ],
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.selectLine(any())).thenAnswer((_) async {});
    when(() => cubit.updateSearch(any())).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.textContaining('Bật GPS'), findsOneWidget);
    expect(find.byType(StationDirectoryRow), findsOneWidget);
  });
}
