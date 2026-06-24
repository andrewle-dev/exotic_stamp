import '../entities/resolved_station.dart';
import '../entities/scan_payload.dart';
import '../repositories/scan_repository.dart';

class ResolveScanUseCase {
  const ResolveScanUseCase(this._repository);

  final ScanRepository _repository;

  Future<ResolvedStation> call(ScanPayload payload) {
    return _repository.resolveScan(payload);
  }
}
