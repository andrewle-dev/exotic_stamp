import 'package:equatable/equatable.dart';

import 'collect_stamp_result.dart';
import 'collect_status_outcome.dart';

class CollectStatusResult extends Equatable {
  const CollectStatusResult({
    required this.outcome,
    this.collectResult,
    this.errorCode,
  });

  final CollectStatusOutcome outcome;
  final CollectStampResult? collectResult;
  final String? errorCode;

  bool get isResolved =>
      outcome == CollectStatusOutcome.success ||
      outcome == CollectStatusOutcome.duplicate;

  bool get isStillUncertain =>
      outcome == CollectStatusOutcome.notFound ||
      outcome == CollectStatusOutcome.pending ||
      outcome == CollectStatusOutcome.failed ||
      outcome == CollectStatusOutcome.unknown;

  @override
  List<Object?> get props => [outcome, collectResult, errorCode];
}
