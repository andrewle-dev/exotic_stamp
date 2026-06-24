import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/memories/domain/entities/photo_share_context.dart';
import 'package:metro_stamp_app/features/memories/presentation/cubit/photo_share_cubit.dart';
import 'package:metro_stamp_app/features/memories/presentation/cubit/photo_share_state.dart';
import 'package:metro_stamp_app/features/memories/presentation/screens/photo_share_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockPhotoShareCubit extends Mock implements PhotoShareCubit {}

void main() {
  late MockPhotoShareCubit cubit;

  setUp(() {
    cubit = MockPhotoShareCubit();
    when(() => cubit.state).thenReturn(
      const PhotoShareState(
        status: PhotoShareStatus.initial,
        context: PhotoShareContext(
          stationId: 'station-1',
          stationName: 'Ben Thanh',
          shareType: PhotoShareContext.shareTypeStampCollected,
        ),
        caption: 'Mình vừa nhận stamp mới tại Ben Thanh!',
      ),
    );
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.setCaption(any())).thenReturn(null);
    when(() => cubit.toggleShowStationName(any())).thenReturn(null);
    when(() => cubit.toggleShowCollectionDate(any())).thenReturn(null);
    when(() => cubit.pickFromGallery()).thenAnswer((_) async {});
    when(() => cubit.pickFromCamera()).thenAnswer((_) async {});
  });

  testWidgets('PhotoShareScreen renders without selected photo',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: PhotoShareScreen(
          cubit: cubit,
          initialContext: const PhotoShareContext(
            stationId: 'station-1',
            stationName: 'Ben Thanh',
            shareType: PhotoShareContext.shareTypeStampCollected,
          ),
        ),
      ),
    );
    await tester.pump();

    expect(find.text('Chia sẻ kỷ niệm'), findsOneWidget);
    expect(find.text('Chọn hoặc chụp ảnh kỷ niệm'), findsOneWidget);
    expect(find.text('Thư viện'), findsOneWidget);
    expect(find.text('Chụp ảnh'), findsOneWidget);
    expect(find.text('Chia sẻ'), findsOneWidget);
    expect(
      find.textContaining('không tải ảnh lên máy chủ'),
      findsOneWidget,
    );
  });

  testWidgets('does not show fake persisted memories gallery', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: PhotoShareScreen(
          cubit: cubit,
          initialContext: const PhotoShareContext(
            stationId: 'station-1',
            stationName: 'Ben Thanh',
            shareType: PhotoShareContext.shareTypeStampCollected,
          ),
        ),
      ),
    );
    await tester.pump();

    expect(find.text('Chưa có kỷ niệm'), findsNothing);
    expect(find.text('Memories'), findsNothing);
    expect(find.byType(GridView), findsNothing);
  });
}
