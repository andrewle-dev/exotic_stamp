import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_detail.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/repositories/stamp_book_repository.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/usecases/get_stamp_detail_usecase.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_cubit.dart';
import 'package:metro_stamp_app/features/stamp_book/presentation/cubit/stamp_detail_state.dart';
import 'package:mocktail/mocktail.dart';

class MockStampBookRepository extends Mock implements StampBookRepository {}

void main() {
  late MockStampBookRepository repository;
  late StampDetailCubit cubit;

  const collectedDetail = StampDetail(
    stationId: 'station-1',
    stationName: 'Ben Thanh',
    collected: true,
    lineName: 'Line 1',
    collectMethod: 'NFC',
    collectedAt: null,
  );

  const lockedDetail = StampDetail(
    stationId: 'station-2',
    stationName: 'Suoi Tien',
    collected: false,
    lineName: 'Line 1',
  );

  setUp(() {
    repository = MockStampBookRepository();
    cubit = StampDetailCubit(
      getStampDetailUseCase: GetStampDetailUseCase(repository),
      stationId: 'station-1',
      lineId: 'line-1',
    );
  });

  tearDown(() => cubit.close());

  blocTest<StampDetailCubit, StampDetailState>(
    'loads collected stamp detail',
    build: () {
      when(
        () => repository.getStampDetail(
          stationId: 'station-1',
          lineId: 'line-1',
        ),
      ).thenAnswer((_) async => collectedDetail);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.status, StampDetailStatus.loaded);
      expect(cubit.state.detail?.collected, isTrue);
    },
  );

  blocTest<StampDetailCubit, StampDetailState>(
    'loads locked stamp detail',
    build: () {
      cubit = StampDetailCubit(
        getStampDetailUseCase: GetStampDetailUseCase(repository),
        stationId: 'station-2',
        lineId: 'line-1',
      );
      when(
        () => repository.getStampDetail(
          stationId: 'station-2',
          lineId: 'line-1',
        ),
      ).thenAnswer((_) async => lockedDetail);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.detail?.collected, isFalse);
    },
  );

  blocTest<StampDetailCubit, StampDetailState>(
    'limited data still loads detail',
    build: () {
      when(
        () => repository.getStampDetail(
          stationId: 'station-1',
          lineId: 'line-1',
        ),
      ).thenAnswer(
        (_) async => collectedDetail.copyWith(
          availability: StampDetailAvailability.limited,
          collectMethod: null,
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.status, StampDetailStatus.loaded);
      expect(cubit.state.detail?.availability, StampDetailAvailability.limited);
    },
  );

  blocTest<StampDetailCubit, StampDetailState>(
    'not found maps to failure',
    build: () {
      when(
        () => repository.getStampDetail(
          stationId: 'station-1',
          lineId: 'line-1',
        ),
      ).thenAnswer(
        (_) async => const StampDetail(
          stationId: 'station-1',
          stationName: '',
          collected: false,
          availability: StampDetailAvailability.notFound,
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.status, StampDetailStatus.failure);
    },
  );
}

extension on StampDetail {
  StampDetail copyWith({
    StampDetailAvailability? availability,
    String? collectMethod,
  }) {
    return StampDetail(
      stationId: stationId,
      stationName: stationName,
      collected: collected,
      lineId: lineId,
      lineName: lineName,
      campaignName: campaignName,
      stampDesignUrl: stampDesignUrl,
      collectedAt: collectedAt,
      stampId: stampId,
      collectMethod: collectMethod ?? this.collectMethod,
      availability: availability ?? this.availability,
    );
  }
}
