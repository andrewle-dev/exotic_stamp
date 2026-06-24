import '../entities/station.dart';
import '../repositories/stations_repository.dart';

class GetStationsUseCase {
  const GetStationsUseCase(this._repository);

  final StationsRepository _repository;

  Future<List<Station>> call({
    String? lineId,
    String? searchQuery,
  }) {
    return _repository.getStations(
      lineId: lineId,
      searchQuery: searchQuery,
    );
  }
}
