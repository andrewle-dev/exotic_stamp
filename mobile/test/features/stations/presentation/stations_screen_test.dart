import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/line.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station_collected_status.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/stations_cubit.dart';
import 'package:metro_stamp_app/features/stations/presentation/cubit/stations_state.dart';
import 'package:metro_stamp_app/features/stations/presentation/screens/stations_screen.dart';
import 'package:metro_stamp_app/features/stations/presentation/widgets/station_list_tile.dart';
import 'package:metro_stamp_app/shared/widgets/app_empty_state.dart';
import 'package:metro_stamp_app/shared/widgets/app_error_view.dart';
import 'package:metro_stamp_app/shared/widgets/app_loading_view.dart';
import 'package:mocktail/mocktail.dart';

class MockStationsCubit extends MockCubit<StationsState>
    implements StationsCubit {}

void main() {
  late MockStationsCubit cubit;

  setUp(() {
    cubit = MockStationsCubit();
  });

  Widget buildSubject() {
    return MaterialApp(
      home: StationsScreen(cubit: cubit),
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

  testWidgets('shows loaded station list', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(
        status: StationsStatus.loaded,
        lines: [Line(id: 'line-1', name: 'Line 1', status: 'ACTIVE')],
        selectedLineId: 'line-1',
        stations: [
          Station(
            id: 's1',
            lineId: 'line-1',
            code: 'S01',
            name: 'Ben Thanh',
            collectedStatus: StationCollectedStatus.collected,
          ),
        ],
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.selectLine(any())).thenAnswer((_) async {});
    when(() => cubit.updateSearch(any())).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(StationListTile), findsOneWidget);
    expect(find.text('Ben Thanh'), findsOneWidget);
    expect(find.text('Đã thu'), findsOneWidget);
  });

  testWidgets('hides collected badge when status is unknown', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(
        status: StationsStatus.loaded,
        lines: [Line(id: 'line-1', name: 'Line 1', status: 'ACTIVE')],
        selectedLineId: 'line-1',
        stations: [
          Station(
            id: 's1',
            lineId: 'line-1',
            code: 'S01',
            name: 'Ben Thanh',
            collectedStatus: StationCollectedStatus.unknown,
          ),
        ],
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.selectLine(any())).thenAnswer((_) async {});
    when(() => cubit.updateSearch(any())).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.text('Ben Thanh'), findsOneWidget);
    expect(find.text('Đã thu'), findsNothing);
    expect(find.text('Chưa thu'), findsNothing);
  });

  testWidgets('shows empty state', (tester) async {
    when(() => cubit.state).thenReturn(
      const StationsState(
        status: StationsStatus.loaded,
        lines: [Line(id: 'line-1', name: 'Line 1')],
        selectedLineId: 'line-1',
        stations: [],
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.selectLine(any())).thenAnswer((_) async {});
    when(() => cubit.updateSearch(any())).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(AppEmptyState), findsOneWidget);
    expect(find.text('Không có ga'), findsOneWidget);
  });
}
