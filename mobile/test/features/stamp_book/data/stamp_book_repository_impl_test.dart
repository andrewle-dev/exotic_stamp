import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stamp_book/data/datasources/stamp_book_remote_datasource.dart';
import 'package:metro_stamp_app/features/stamp_book/data/models/stamp_book_model.dart';
import 'package:metro_stamp_app/features/stamp_book/data/models/stamp_item_model.dart';
import 'package:metro_stamp_app/features/stamp_book/data/repositories/stamp_book_repository_impl.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_detail.dart';
import 'package:mocktail/mocktail.dart';

class MockStampBookRemoteDataSource extends Mock
    implements StampBookRemoteDataSource {}

void main() {
  late MockStampBookRemoteDataSource remoteDataSource;
  late StampBookRepositoryImpl repository;

  final stampBookModel = StampBookModel(
    lineId: 'line-1',
    lineName: 'Line 1',
    campaignName: 'Metro 2026',
    progress: StampBookProgressModel(
      lineId: 'line-1',
      collected: 1,
      total: 2,
      percentage: 50,
    ),
    stations: [
      StampItemModel(
        stationId: 'station-1',
        stationName: 'Ben Thanh',
        sequence: 1,
        collected: true,
        collectedAt: DateTime(2026, 6, 20, 14, 22),
        stampDesignUrl: '/uploads/stamp.png',
        stampDesignName: 'Stamp Ben Thanh',
        stampDesignDescription: 'Heart of the metro system.',
        rarity: 'RARE',
      ),
      StampItemModel(
        stationId: 'station-2',
        stationName: 'Suoi Tien',
        sequence: 2,
        collected: false,
        stampDesignName: 'Stamp Suoi Tien',
        stampDesignDescription: 'Theme park terminus stamp.',
        rarity: 'COMMON',
      ),
    ],
  );

  setUp(() {
    remoteDataSource = MockStampBookRemoteDataSource();
    repository = StampBookRepositoryImpl(remoteDataSource: remoteDataSource);
  });

  test('maps stamp book response to entity', () async {
    when(() => remoteDataSource.getStampBook(lineId: any(named: 'lineId')))
        .thenAnswer((_) async => stampBookModel);

    final stampBook = await repository.getStampBook(lineId: 'line-1');

    expect(stampBook.lineName, 'Line 1');
    expect(stampBook.stations, hasLength(2));
    expect(stampBook.stations.first.collected, isTrue);
    expect(stampBook.progress?.percentage, 50);
  });

  test('builds collected stamp detail from stamp-book and my-stamps', () async {
    when(() => remoteDataSource.getStampBook(lineId: any(named: 'lineId')))
        .thenAnswer((_) async => stampBookModel);
    when(
      () => remoteDataSource.getMyStamps(
        lineId: any(named: 'lineId'),
        page: any(named: 'page'),
        size: any(named: 'size'),
      ),
    ).thenAnswer(
      (_) async => [
        StampItemModel.fromUserStampJson({
          'stampId': 'stamp-1',
          'stationId': 'station-1',
          'stationName': 'Ben Thanh',
          'collectMethod': 'NFC',
          'collectedAt': '2026-06-20T14:22:00',
        }),
      ],
    );

    final detail = await repository.getStampDetail(
      stationId: 'station-1',
      lineId: 'line-1',
    );

    expect(detail.collected, isTrue);
    expect(detail.stampId, 'stamp-1');
    expect(detail.collectMethod, 'NFC');
  });

  test('returns locked detail for uncollected station', () async {
    when(() => remoteDataSource.getStampBook(lineId: any(named: 'lineId')))
        .thenAnswer((_) async => stampBookModel);

    final detail = await repository.getStampDetail(
      stationId: 'station-2',
      lineId: 'line-1',
    );

    expect(detail.collected, isFalse);
    expect(detail.stationName, 'Suoi Tien');
    expect(detail.stampDesignName, 'Stamp Suoi Tien');
    expect(detail.stampDesignDescription, 'Theme park terminus stamp.');
    expect(detail.rarity, 'COMMON');
    verifyNever(
      () => remoteDataSource.getMyStamps(
        lineId: any(named: 'lineId'),
        page: any(named: 'page'),
        size: any(named: 'size'),
      ),
    );
  });

  test('returns not found when station missing from stamp book', () async {
    when(() => remoteDataSource.getStampBook(lineId: any(named: 'lineId')))
        .thenAnswer((_) async => stampBookModel);

    final detail = await repository.getStampDetail(
      stationId: 'missing',
      lineId: 'line-1',
    );

    expect(detail.availability, StampDetailAvailability.notFound);
  });
}
