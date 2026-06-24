import '../../domain/entities/share_event.dart';
import '../../domain/repositories/memories_repository.dart';
import '../datasources/memories_remote_datasource.dart';
import '../models/record_share_event_request_model.dart';

class MemoriesRepositoryImpl implements MemoriesRepository {
  MemoriesRepositoryImpl({
    required MemoriesRemoteDataSource remoteDataSource,
  }) : _remoteDataSource = remoteDataSource;

  final MemoriesRemoteDataSource _remoteDataSource;

  @override
  Future<ShareEvent> recordShareEvent(RecordShareEventParams params) async {
    final model = await _remoteDataSource.recordShareEvent(
      RecordShareEventRequestModel(
        platform: params.platform,
        shareType: params.shareType,
        targetId: params.targetId,
        metadata: params.metadata,
      ),
    );
    return model.toEntity();
  }
}
