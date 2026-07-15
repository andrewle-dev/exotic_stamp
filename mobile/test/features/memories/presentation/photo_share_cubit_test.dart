import 'dart:io';
import 'dart:typed_data';

import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/memories/domain/entities/photo_share_context.dart';
import 'package:metro_stamp_app/features/memories/domain/entities/share_event.dart';
import 'package:metro_stamp_app/features/memories/domain/repositories/memories_repository.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_book.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_item.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/repositories/stamp_book_repository.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/usecases/get_stamp_book_usecase.dart';
import 'package:metro_stamp_app/features/memories/domain/usecases/record_share_event_usecase.dart';
import 'package:metro_stamp_app/features/memories/presentation/cubit/photo_share_cubit.dart';
import 'package:metro_stamp_app/features/memories/presentation/cubit/photo_share_state.dart';
import 'package:metro_stamp_app/features/memories/presentation/services/native_share_service.dart';
import 'package:metro_stamp_app/features/memories/presentation/services/photo_picker_service.dart';
import 'package:metro_stamp_app/features/memories/presentation/services/share_temp_file_writer.dart';
import 'package:mocktail/mocktail.dart';

class MockMemoriesRepository extends Mock implements MemoriesRepository {}

class MockPhotoPickerService extends Mock implements PhotoPickerService {}

class MockNativeShareService extends Mock implements NativeShareService {}

class MockShareTempFileWriter extends Mock implements ShareTempFileWriter {}

class MockStampBookRepository extends Mock implements StampBookRepository {}

void main() {
  late MockMemoriesRepository repository;
  late MockPhotoPickerService photoPickerService;
  late MockNativeShareService nativeShareService;
  late MockShareTempFileWriter tempFileWriter;
  late MockStampBookRepository stampBookRepository;
  late PhotoShareCubit cubit;

  const shareContext = PhotoShareContext(
    stationId: 'station-1',
    stationName: 'Ben Thanh',
    shareType: PhotoShareContext.shareTypeStampCollected,
    stampId: 'stamp-uuid',
    collectedAt: null,
  );

  final pickedPhoto = PickedPhoto(
    bytes: Uint8List.fromList([1, 2, 3]),
    path: '/tmp/photo.jpg',
  );

  setUpAll(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    registerFallbackValue(Uint8List(0));
    registerFallbackValue(
      const RecordShareEventParams(
        platform: 'native',
        shareType: PhotoShareContext.shareTypeStampCollected,
      ),
    );
  });

  PhotoShareCubit buildCubit({PhotoShareContext? initialContext}) {
    return PhotoShareCubit(
      recordShareEventUseCase: RecordShareEventUseCase(repository),
      getStampBookUseCase: GetStampBookUseCase(stampBookRepository),
      photoPickerService: photoPickerService,
      nativeShareService: nativeShareService,
      tempFileWriter: tempFileWriter,
      initialContext: initialContext ?? shareContext,
    );
  }

  setUp(() {
    repository = MockMemoriesRepository();
    photoPickerService = MockPhotoPickerService();
    nativeShareService = MockNativeShareService();
    tempFileWriter = MockShareTempFileWriter();
    when(() => tempFileWriter.writePng(any())).thenAnswer((invocation) async {
      final file = File('${Directory.systemTemp.path}/test_share.png');
      await file.writeAsBytes(
        invocation.positionalArguments[0] as Uint8List,
      );
      return file;
    });
    stampBookRepository = MockStampBookRepository();
    when(() => stampBookRepository.getStampBook(lineId: any(named: 'lineId')))
        .thenAnswer(
      (_) async => const StampBook(
        lineId: 'line-1',
        lineName: 'Line 1',
        stations: [
          StampItem(
            stationId: 'station-1',
            stationName: 'Ben Thanh',
            sequence: 1,
            collected: true,
            stampId: 'stamp-uuid',
          ),
        ],
      ),
    );
    cubit = buildCubit();
  });

  tearDown(() => cubit.close());

  test('initial state has stamp context and default caption', () {
    expect(cubit.state.status, PhotoShareStatus.initial);
    expect(cubit.state.hasPhoto, isFalse);
    expect(cubit.state.context, shareContext);
    expect(
      cubit.state.caption,
      'Mình vừa nhận stamp mới tại Ben Thanh!',
    );
  });

  blocTest<PhotoShareCubit, PhotoShareState>(
    'pickFromGallery moves to editing when photo selected',
    build: () {
      when(() => photoPickerService.pickFromGallery())
          .thenAnswer((_) async => pickedPhoto);
      return cubit;
    },
    act: (cubit) => cubit.pickFromGallery(),
    expect: () => [
      isA<PhotoShareState>().having(
        (s) => s.status,
        'status',
        PhotoShareStatus.pickingPhoto,
      ),
      isA<PhotoShareState>()
          .having((s) => s.status, 'status', PhotoShareStatus.editing)
          .having((s) => s.photoPath, 'photoPath', pickedPhoto.path),
    ],
  );

  blocTest<PhotoShareCubit, PhotoShareState>(
    'pickFromGallery stays initial when user cancels',
    build: () {
      when(() => photoPickerService.pickFromGallery())
          .thenAnswer((_) async => null);
      return cubit;
    },
    act: (cubit) => cubit.pickFromGallery(),
    expect: () => [
      isA<PhotoShareState>().having(
        (s) => s.status,
        'status',
        PhotoShareStatus.pickingPhoto,
      ),
      isA<PhotoShareState>().having(
        (s) => s.status,
        'status',
        PhotoShareStatus.initial,
      ),
    ],
  );

  blocTest<PhotoShareCubit, PhotoShareState>(
    'toggles and caption update while editing',
    build: () => cubit,
    seed: () => const PhotoShareState(
      status: PhotoShareStatus.editing,
      context: shareContext,
      photoPath: '/tmp/photo.jpg',
      caption: 'Hello',
    ),
    act: (cubit) {
      cubit.setCaption('Updated caption');
      cubit.toggleShowStationName(false);
      cubit.toggleShowCollectionDate(false);
    },
    expect: () => [
      isA<PhotoShareState>()
          .having((s) => s.caption, 'caption', 'Updated caption'),
      isA<PhotoShareState>()
          .having((s) => s.showStationName, 'showStationName', false),
      isA<PhotoShareState>()
          .having((s) => s.showCollectionDate, 'showCollectionDate', false),
    ],
  );

  blocTest<PhotoShareCubit, PhotoShareState>(
    'shareComposedImage marks shared and records tracking on success',
    build: () {
      when(
        () => nativeShareService.shareImage(
          filePath: any(named: 'filePath'),
          text: any(named: 'text'),
        ),
      ).thenAnswer(
        (_) async => const NativeShareResult(NativeShareOutcome.completed),
      );
      when(() => repository.recordShareEvent(any())).thenAnswer(
        (_) async => ShareEvent(
          id: 'share-1',
          platform: 'native',
          shareType: PhotoShareContext.shareTypeStampCollected,
          sharedAt: DateTime.utc(2026, 6, 24),
        ),
      );
      return buildCubit();
    },
    seed: () => const PhotoShareState(
      status: PhotoShareStatus.editing,
      context: shareContext,
      photoPath: '/tmp/photo.jpg',
      caption: 'Caption',
    ),
    act: (cubit) =>
        cubit.shareComposedImage(Uint8List.fromList([137, 80, 78, 71])),
    expect: () => [
      isA<PhotoShareState>().having(
        (s) => s.status,
        'status',
        PhotoShareStatus.sharing,
      ),
      isA<PhotoShareState>()
          .having((s) => s.status, 'status', PhotoShareStatus.shared)
          .having((s) => s.trackingRecorded, 'trackingRecorded', false),
      isA<PhotoShareState>().having(
        (s) => s.trackingRecorded,
        'trackingRecorded',
        true,
      ),
    ],
    verify: (_) {
      verify(() => repository.recordShareEvent(any())).called(1);
    },
  );

  blocTest<PhotoShareCubit, PhotoShareState>(
    'tracking failure is non-blocking after native share success',
    build: () {
      when(
        () => nativeShareService.shareImage(
          filePath: any(named: 'filePath'),
          text: any(named: 'text'),
        ),
      ).thenAnswer(
        (_) async => const NativeShareResult(NativeShareOutcome.dismissed),
      );
      when(() => repository.recordShareEvent(any())).thenThrow(
        const Failure(code: FailureCode.networkError, message: 'offline'),
      );
      return buildCubit();
    },
    seed: () => const PhotoShareState(
      status: PhotoShareStatus.editing,
      context: shareContext,
      photoPath: '/tmp/photo.jpg',
    ),
    act: (cubit) =>
        cubit.shareComposedImage(Uint8List.fromList([137, 80, 78, 71])),
    expect: () => [
      isA<PhotoShareState>().having(
        (s) => s.status,
        'status',
        PhotoShareStatus.sharing,
      ),
      isA<PhotoShareState>()
          .having((s) => s.status, 'status', PhotoShareStatus.shared)
          .having((s) => s.trackingFailed, 'trackingFailed', false),
      isA<PhotoShareState>()
          .having((s) => s.status, 'status', PhotoShareStatus.shared)
          .having((s) => s.trackingFailed, 'trackingFailed', true),
    ],
  );

  blocTest<PhotoShareCubit, PhotoShareState>(
    'native share unavailable marks shareFailed without tracking',
    build: () {
      when(
        () => nativeShareService.shareImage(
          filePath: any(named: 'filePath'),
          text: any(named: 'text'),
        ),
      ).thenAnswer(
        (_) async => const NativeShareResult(NativeShareOutcome.unavailable),
      );
      return buildCubit();
    },
    seed: () => const PhotoShareState(
      status: PhotoShareStatus.editing,
      context: shareContext,
      photoPath: '/tmp/photo.jpg',
    ),
    act: (cubit) =>
        cubit.shareComposedImage(Uint8List.fromList([137, 80, 78, 71])),
    expect: () => [
      isA<PhotoShareState>().having(
        (s) => s.status,
        'status',
        PhotoShareStatus.sharing,
      ),
      isA<PhotoShareState>().having(
        (s) => s.status,
        'status',
        PhotoShareStatus.shareFailed,
      ),
    ],
    verify: (_) {
      verifyNever(() => repository.recordShareEvent(any()));
    },
  );
}
