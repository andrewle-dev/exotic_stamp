import '../entities/collect_stamp_result.dart';
import '../entities/scan_payload.dart';
import '../repositories/scan_repository.dart';

class CollectStampUseCase {
  const CollectStampUseCase(this._repository);

  final ScanRepository _repository;

  Future<CollectStampResult> call({
    required ScanPayload payload,
    required double latitude,
    required double longitude,
    required double accuracyMeters,
    required String idempotencyKey,
  }) {
    return _repository.collectStamp(
      payload: payload,
      latitude: latitude,
      longitude: longitude,
      accuracyMeters: accuracyMeters,
      idempotencyKey: idempotencyKey,
    );
  }
}
