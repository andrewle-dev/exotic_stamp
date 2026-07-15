import '../../../features/home/domain/entities/home_summary.dart';
import '../../../features/home/domain/repositories/home_repository.dart';
import '../mock_data_store.dart';
import '../mock_fixtures.dart';
/// Mock [HomeRepository] — UI development only.
class MockHomeRepository implements HomeRepository {
  MockHomeRepository({MockDataStore? store})
      : _store = store ?? MockDataStore.instance;

  final MockDataStore _store;

  @override
  Future<HomeSummary> getHomeSummary() async {
    await Future<void>.delayed(const Duration(milliseconds: 200));
    return MockFixtures.homeSummary(_store.collectedStationIds);
  }
}
