import '../entities/collect_stamp_result.dart';
import '../entities/collect_status_result.dart';
import '../entities/resolved_station.dart';
import '../entities/scan_payload.dart';

abstract class ScanRepository {
  Future<ResolvedStation> resolveScan(ScanPayload payload);

  Future<CollectStampResult> collectStamp({
    required ScanPayload payload,
    required double latitude,
    required double longitude,
    required double accuracyMeters,
    required String idempotencyKey,
  });

  Future<CollectStatusResult> getCollectStatus({
    required String idempotencyKey,
  });
}
