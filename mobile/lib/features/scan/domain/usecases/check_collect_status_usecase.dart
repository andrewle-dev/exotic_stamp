import '../entities/collect_status_result.dart';
import '../repositories/scan_repository.dart';

class CheckCollectStatusUseCase {
  const CheckCollectStatusUseCase(this._repository);

  final ScanRepository _repository;

  Future<CollectStatusResult> call({required String idempotencyKey}) {
    return _repository.getCollectStatus(idempotencyKey: idempotencyKey);
  }
}
