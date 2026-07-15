import '../../../features/stamp_book/domain/entities/stamp_book.dart';
import '../../../features/stamp_book/domain/entities/stamp_detail.dart';
import '../../../features/stamp_book/domain/repositories/stamp_book_repository.dart';
import '../mock_data_store.dart';
import '../mock_fixtures.dart';

/// Mock [StampBookRepository] — UI development only.
class MockStampBookRepository implements StampBookRepository {
  MockStampBookRepository({MockDataStore? store})
      : _store = store ?? MockDataStore.instance;

  final MockDataStore _store;

  @override
  Future<StampBook> getStampBook({String? lineId}) async {
    await Future<void>.delayed(const Duration(milliseconds: 200));
    return MockFixtures.stampBook(
      _store.collectedStationIds,
      filterLineId: lineId,
    );
  }

  @override
  Future<StampDetail> getStampDetail({
    required String stationId,
    String? lineId,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 150));
    return MockFixtures.stampDetail(stationId, _store.collectedStationIds);
  }
}
