import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_book.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_item.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_book_cubit.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_book_state.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/screens/stamp_book_screen.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/line.dart';
import 'package:metro_stamp_app/shared/widgets/stamp_tile.dart';
import 'package:mocktail/mocktail.dart';

class MockStampBookCubit extends Mock implements StampBookCubit {}

void main() {
  late MockStampBookCubit cubit;

  const loadedState = StampBookState(
    status: StampBookStatus.loaded,
    lines: [
      Line(
        id: 'line-1',
        name: 'Line 1',
        displayName: 'Line 1',
        status: 'ACTIVE',
      ),
    ],
    selectedLineId: 'line-1',
    stampBook: StampBook(
      lineId: 'line-1',
      lineName: 'Line 1',
      progress: StampBookProgress(
        lineId: 'line-1',
        collected: 1,
        total: 2,
        percentage: 50,
      ),
      stations: [
        StampItem(
          stationId: 'station-1',
          stationName: 'Ben Thanh',
          sequence: 1,
          collected: true,
        ),
        StampItem(
          stationId: 'station-2',
          stationName: 'Suoi Tien',
          sequence: 2,
          collected: false,
        ),
      ],
    ),
  );

  setUp(() {
    cubit = MockStampBookCubit();
    when(() => cubit.state).thenReturn(loadedState);
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.refresh()).thenAnswer((_) async {});
    when(() => cubit.selectLine(any())).thenAnswer((_) async {});
  });

  testWidgets('renders collected and locked stamps from backend state',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: StampBookScreen(cubit: cubit),
      ),
    );
    await tester.pump();

    expect(find.text('Stamp Book'), findsOneWidget);
    expect(find.text('Ben Thanh'), findsOneWidget);
    expect(find.text('Suoi Tien'), findsOneWidget);
    expect(find.byType(StampTile), findsNWidgets(2));
    expect(find.byIcon(Icons.check_rounded), findsOneWidget);
    expect(find.byIcon(Icons.lock_rounded), findsWidgets);
  });
}
