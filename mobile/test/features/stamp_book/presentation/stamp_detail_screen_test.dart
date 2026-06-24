import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_detail.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_cubit.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_state.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/screens/stamp_detail_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockStampDetailCubit extends Mock implements StampDetailCubit {}

void main() {
  late MockStampDetailCubit cubit;

  const lockedDetail = StampDetail(
    stationId: 'station-2',
    stationName: 'Suoi Tien',
    collected: false,
    lineName: 'Line 1',
    collectedAt: null,
    collectMethod: null,
  );

  setUp(() {
    cubit = MockStampDetailCubit();
    when(() => cubit.state).thenReturn(
      const StampDetailState(
        status: StampDetailStatus.loaded,
        detail: lockedDetail,
      ),
    );
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.load()).thenAnswer((_) async {});
  });

  testWidgets('locked stamp detail does not show fake collected data',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: StampDetailScreen(
          stationId: 'station-2',
          cubit: cubit,
        ),
      ),
    );
    await tester.pump();

    expect(find.text('Thời gian thu'), findsNothing);
    expect(find.text('Phương thức quét'), findsNothing);
    expect(find.text('Stamp chưa thu'), findsOneWidget);
    expect(find.text('Chia sẻ Stamp'), findsNothing);
    expect(
      find.text('Thu stamp tại ga này bằng NFC để mở khóa.'),
      findsOneWidget,
    );
  });
}
