import '../entities/share_event.dart';
import '../repositories/memories_repository.dart';

class RecordShareEventUseCase {
  const RecordShareEventUseCase(this._repository);

  final MemoriesRepository _repository;

  Future<ShareEvent> call(RecordShareEventParams params) {
    return _repository.recordShareEvent(params);
  }
}
