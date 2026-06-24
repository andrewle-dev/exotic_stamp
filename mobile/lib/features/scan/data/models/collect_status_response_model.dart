import '../../domain/entities/collect_status_outcome.dart';
import '../../domain/entities/collect_status_result.dart';
import 'collect_stamp_response_model.dart';

class CollectStatusResponseModel {
  CollectStatusResponseModel({
    required this.status,
    this.stamp,
    this.progress,
    this.errorCode,
  });

  factory CollectStatusResponseModel.fromJson(Map<String, dynamic> json) {
    final stampJson = json['stamp'] as Map<String, dynamic>?;
    final progressJson = json['progress'] as Map<String, dynamic>?;

    return CollectStatusResponseModel(
      status: CollectStatusOutcome.fromApi(json['status'] as String?),
      stamp: stampJson == null ? null : CollectedStampModel.fromJson(stampJson),
      progress: progressJson == null
          ? null
          : CollectStampProgressModel.fromJson(progressJson),
      errorCode: json['errorCode'] as String?,
    );
  }

  final CollectStatusOutcome status;
  final CollectedStampModel? stamp;
  final CollectStampProgressModel? progress;
  final String? errorCode;

  CollectStatusResult toEntity() {
    final stampModel = stamp;
    final collectResult = stampModel == null
        ? null
        : CollectStampResponseModel(
            stamp: stampModel,
            progress: progress,
            isNew: status == CollectStatusOutcome.success,
          ).toEntity();

    return CollectStatusResult(
      outcome: status,
      collectResult: collectResult,
      errorCode: errorCode,
    );
  }
}
