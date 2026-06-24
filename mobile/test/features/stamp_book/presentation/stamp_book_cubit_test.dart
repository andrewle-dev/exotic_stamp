import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_book.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_item.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/repositories/stamp_book_repository.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/usecases/get_stamp_book_usecase.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_book_cubit.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_book_state.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/line.dart';
import 'package:metro_stamp_app/features/stations/domain/repositories/stations_repository.dart';
import 'package:metro_stamp_app/features/stations/domain/usecases/get_lines_usecase.dart';
import 'package:mocktail/mocktail.dart';

class MockStampBookRepository extends Mock implements StampBookRepository {}

class MockStationsRepository extends Mock implements StationsRepository {}

void main() {
  late MockStampBookRepository stampBookRepository;
  late MockStationsRepository stationsRepository;
  late StampBookCubit cubit;

  const lines = [
    Line(id: 'line-1', name: 'Line 1', status: 'ACTIVE'),
    Line(id: 'line-2', name: 'Line 2', status: 'ACTIVE'),
  ];

  const emptyStampBook = StampBook(
    lineId: 'line-1',
    lineName: 'Line 1',
    progress: StampBookProgress(
      lineId: 'line-1',
      collected: 0,
      total: 2,
      percentage: 0,
    ),
    stations: [
      StampItem(
        stationId: 'station-1',
        stationName: 'Ben Thanh',
        sequence: 1,
        collected: false,
      ),
    ],
  );

  const loadedStampBook = StampBook(
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
  );

  setUp(() {
    stampBookRepository = MockStampBookRepository();
    stationsRepository = MockStationsRepository();
    cubit = StampBookCubit(
      getStampBookUseCase: GetStampBookUseCase(stampBookRepository),
      getLinesUseCase: GetLinesUseCase(stationsRepository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<StampBookCubit, StampBookState>(
    'emits loading then loaded on success',
    build: () {
      when(() => stationsRepository.getLines()).thenAnswer((_) async => lines);
      when(() => stampBookRepository.getStampBook(lineId: 'line-1'))
          .thenAnswer((_) async => loadedStampBook);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<StampBookState>().having(
        (s) => s.status,
        'status',
        StampBookStatus.loading,
      ),
      isA<StampBookState>()
          .having((s) => s.status, 'status', StampBookStatus.loaded)
          .having((s) => s.stampBook?.stations.length, 'stations', 2),
    ],
  );

  blocTest<StampBookCubit, StampBookState>(
    'emits empty when no collected stamps',
    build: () {
      when(() => stationsRepository.getLines()).thenAnswer((_) async => lines);
      when(() => stampBookRepository.getStampBook(lineId: 'line-1'))
          .thenAnswer((_) async => emptyStampBook);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.status, StampBookStatus.empty);
    },
  );

  blocTest<StampBookCubit, StampBookState>(
    'selectLine refetches stamp book for line',
    build: () {
      when(() => stationsRepository.getLines()).thenAnswer((_) async => lines);
      when(() => stampBookRepository.getStampBook(lineId: 'line-1'))
          .thenAnswer((_) async => loadedStampBook);
      when(() => stampBookRepository.getStampBook(lineId: 'line-2')).thenAnswer(
        (_) async => loadedStampBook.copyWith(
          lineId: 'line-2',
          lineName: 'Line 2',
        ),
      );
      return cubit;
    },
    act: (cubit) async {
      await cubit.load();
      await cubit.selectLine('line-2');
    },
    verify: (_) {
      verify(() => stampBookRepository.getStampBook(lineId: 'line-2'))
          .called(1);
      expect(cubit.state.selectedLineId, 'line-2');
    },
  );

  blocTest<StampBookCubit, StampBookState>(
    'refresh triggers backend fetch',
    build: () {
      when(() => stationsRepository.getLines()).thenAnswer((_) async => lines);
      when(() => stampBookRepository.getStampBook(lineId: any(named: 'lineId')))
          .thenAnswer((_) async => loadedStampBook);
      return cubit;
    },
    act: (cubit) async {
      await cubit.load();
      await cubit.refresh();
    },
    verify: (_) {
      verify(
        () => stampBookRepository.getStampBook(lineId: any(named: 'lineId')),
      ).called(2);
    },
  );

  blocTest<StampBookCubit, StampBookState>(
    'emits failure on error',
    build: () {
      when(() => stationsRepository.getLines()).thenThrow(
        const Failure(code: FailureCode.networkError, message: 'Network down'),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<StampBookState>().having(
        (s) => s.status,
        'status',
        StampBookStatus.loading,
      ),
      isA<StampBookState>().having(
        (s) => s.status,
        'status',
        StampBookStatus.failure,
      ),
    ],
  );
}

extension on StampBook {
  StampBook copyWith({
    String? lineId,
    String? lineName,
    StampBookProgress? progress,
    List<StampItem>? stations,
  }) {
    return StampBook(
      lineId: lineId ?? this.lineId,
      lineName: lineName ?? this.lineName,
      progress: progress ?? this.progress,
      stations: stations ?? this.stations,
    );
  }
}
