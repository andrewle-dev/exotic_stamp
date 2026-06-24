import 'package:equatable/equatable.dart';

import 'scan_type.dart';

class ScanPayload extends Equatable {
  const ScanPayload({
    required this.scanType,
    required this.payload,
  });

  final ScanType scanType;
  final String payload;

  @override
  List<Object?> get props => [scanType, payload];
}
