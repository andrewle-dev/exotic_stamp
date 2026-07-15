import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../../stations/domain/entities/station.dart';
import '../../data/models/station_scan_key_models.dart';

enum NfcTagWriterPhase {
  idle,
  loadingStations,
  ready,
  creatingKey,
  keyReady,
  writingTag,
  verifyingInstall,
  success,
  error,
}

class NfcTagWriterState extends Equatable {
  const NfcTagWriterState({
    this.phase = NfcTagWriterPhase.idle,
    this.stations = const [],
    this.selectedStation,
    this.createdKey,
    this.readBackPayload,
    this.failure,
    this.statusMessage,
  });

  final NfcTagWriterPhase phase;
  final List<Station> stations;
  final Station? selectedStation;
  final StationScanKeyCreatedModel? createdKey;
  final String? readBackPayload;
  final Failure? failure;
  final String? statusMessage;

  NfcTagWriterState copyWith({
    NfcTagWriterPhase? phase,
    List<Station>? stations,
    Station? selectedStation,
    StationScanKeyCreatedModel? createdKey,
    String? readBackPayload,
    Failure? failure,
    String? statusMessage,
    bool clearFailure = false,
    bool clearCreatedKey = false,
    bool clearReadBack = false,
    bool clearStatusMessage = false,
    bool clearSelectedStation = false,
  }) {
    return NfcTagWriterState(
      phase: phase ?? this.phase,
      stations: stations ?? this.stations,
      selectedStation:
          clearSelectedStation ? null : (selectedStation ?? this.selectedStation),
      createdKey: clearCreatedKey ? null : (createdKey ?? this.createdKey),
      readBackPayload:
          clearReadBack ? null : (readBackPayload ?? this.readBackPayload),
      failure: clearFailure ? null : (failure ?? this.failure),
      statusMessage:
          clearStatusMessage ? null : (statusMessage ?? this.statusMessage),
    );
  }

  @override
  List<Object?> get props => [
        phase,
        stations,
        selectedStation,
        createdKey,
        readBackPayload,
        failure,
        statusMessage,
      ];
}
