import '../../domain/entities/collect_stamp_result.dart';
import '../../domain/entities/collect_status_result.dart';
import '../../domain/entities/resolved_station.dart';
import '../../domain/entities/scan_payload.dart';
import '../../domain/repositories/scan_repository.dart';
import '../datasources/scan_remote_datasource.dart';

class ScanRepositoryImpl implements ScanRepository {
  ScanRepositoryImpl({required ScanRemoteDataSource remoteDataSource})
      : _remoteDataSource = remoteDataSource;

  final ScanRemoteDataSource _remoteDataSource;

  @override
  Future<ResolvedStation> resolveScan(ScanPayload payload) async {
    final response = await _remoteDataSource.resolveScan(payload);
    return response.toEntity();
  }

  @override
  Future<CollectStampResult> collectStamp({
    required ScanPayload payload,
    required double latitude,
    required double longitude,
    required double accuracyMeters,
    required String idempotencyKey,
  }) async {
    final response = await _remoteDataSource.collectStamp(
      payload: payload,
      latitude: latitude,
      longitude: longitude,
      accuracyMeters: accuracyMeters,
      idempotencyKey: idempotencyKey,
    );
    return response.toEntity();
  }

  @override
  Future<CollectStatusResult> getCollectStatus({
    required String idempotencyKey,
  }) async {
    final response = await _remoteDataSource.getCollectStatus(
      idempotencyKey: idempotencyKey,
    );
    return response.toEntity();
  }
}
