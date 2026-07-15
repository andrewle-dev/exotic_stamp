import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_detail.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_cubit.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_state.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/screens/stamp_detail_screen.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/widgets/stamp_detail_sections.dart';
import 'package:mocktail/mocktail.dart';

class MockStampDetailCubit extends Mock implements StampDetailCubit {}

void main() {
  late MockStampDetailCubit cubit;

  const lockedDetail = StampDetail(
    stationId: 'station-2',
    stationName: 'Suoi Tien',
    collected: false,
    lineName: 'Line 1',
    campaignName: 'Campaign: Metro Line 1',
    stampDesignName: 'Stamp — Suoi Tien',
    stampDesignDescription: 'Theme park terminus stamp.',
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

  testWidgets('locked stamp detail shows clean copy without admin clutter',
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
    expect(find.text('Chưa mở khóa'), findsOneWidget);
    expect(find.text('Chưa thu'), findsOneWidget);
    expect(find.text('Chia sẻ Stamp'), findsNothing);
    expect(find.text('Cách mở khóa'), findsOneWidget);
    expect(find.text('Placement'), findsNothing);
    expect(find.text('Campaign'), findsNothing);
    expect(find.text('Campaign: Campaign: Metro Line 1'), findsNothing);
    expect(find.text('Metro Line 1'), findsOneWidget);
    expect(find.text('Suoi Tien'), findsWidgets);
    expect(find.text('Line 1'), findsWidgets);
    expect(find.text('Về stamp này'), findsOneWidget);
    expect(find.text('Theme park terminus stamp.'), findsOneWidget);
  });

  test('cleanCampaignName strips duplicated Campaign prefixes', () {
    expect(
      cleanCampaignName('Campaign: Campaign: Metro Line Bến Thành'),
      'Metro Line Bến Thành',
    );
    expect(cleanCampaignName('Metro Stamp 2026'), 'Metro Stamp 2026');
  });

  test('stampDetailTitle avoids Stamp — Station redundancy', () {
    expect(
      stampDetailTitle(
        const StampDetail(
          stationId: 's1',
          stationName: 'Tan Cang',
          collected: false,
          stampDesignName: 'Stamp — Tan Cang',
        ),
      ),
      'Tan Cang',
    );
  });
}
