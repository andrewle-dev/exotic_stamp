import '../entities/station_detail.dart';
import '../repositories/stations_repository.dart';

class GetStationDetailUseCase {
  const GetStationDetailUseCase(this._repository);

  final StationsRepository _repository;

  Future<StationDetail> call(String stationId) {
    return _repository.getStationDetail(stationId);
  }
}
