import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../../../core/location/app_location_service.dart';
import '../../../../core/nfc/nfc_availability.dart';
import '../../domain/entities/collect_stamp_result.dart';
import '../../domain/entities/resolved_station.dart';
import '../../domain/entities/scan_payload.dart';

enum ScanFlowPhase {
  initial,
  checkingNfcAvailability,
  waitingForNfc,
  readingNfc,
  qrFallbackReady,
  checkingLocation,
  resolvingStation,
  collecting,
  checkingCollectStatus,
  preStampAd,
  success,
  duplicate,
  invalidTag,
  qrExpired,
  gpsOutsideRange,
  stationInactive,
  campaignInactive,
  networkError,
  locationPermissionDenied,
  locationServiceDisabled,
  locationLowAccuracy,
  locationTimeout,
  unknownError,
}

class ScanFlowState extends Equatable {
  const ScanFlowState({
    this.phase = ScanFlowPhase.initial,
    this.scanPayload,
    this.resolvedStation,
    this.gpsReading,
    this.collectResult,
    this.failure,
    this.statusMessage,
    this.idempotencyKey,
    this.nfcAvailability,
    this.qrFallbackAvailable = false,
    this.awaitingCollectConfirmation = false,
    this.isUncertainOutcome = false,
  });

  final ScanFlowPhase phase;
  final ScanPayload? scanPayload;
  final ResolvedStation? resolvedStation;
  final GpsReading? gpsReading;
  final CollectStampResult? collectResult;
  final Failure? failure;
  final String? statusMessage;
  final String? idempotencyKey;
  final NfcAvailabilityStatus? nfcAvailability;
  final bool qrFallbackAvailable;
  final bool awaitingCollectConfirmation;
  final bool isUncertainOutcome;

  ScanFlowState copyWith({
    ScanFlowPhase? phase,
    ScanPayload? scanPayload,
    ResolvedStation? resolvedStation,
    GpsReading? gpsReading,
    CollectStampResult? collectResult,
    Failure? failure,
    String? statusMessage,
    String? idempotencyKey,
    NfcAvailabilityStatus? nfcAvailability,
    bool? qrFallbackAvailable,
    bool? awaitingCollectConfirmation,
    bool? isUncertainOutcome,
    bool clearScanPayload = false,
    bool clearResolvedStation = false,
    bool clearGpsReading = false,
    bool clearCollectResult = false,
    bool clearFailure = false,
    bool clearStatusMessage = false,
    bool clearIdempotencyKey = false,
  }) {
    return ScanFlowState(
      phase: phase ?? this.phase,
      scanPayload: clearScanPayload ? null : (scanPayload ?? this.scanPayload),
      resolvedStation: clearResolvedStation
          ? null
          : (resolvedStation ?? this.resolvedStation),
      gpsReading: clearGpsReading ? null : (gpsReading ?? this.gpsReading),
      collectResult:
          clearCollectResult ? null : (collectResult ?? this.collectResult),
      failure: clearFailure ? null : (failure ?? this.failure),
      statusMessage:
          clearStatusMessage ? null : (statusMessage ?? this.statusMessage),
      idempotencyKey:
          clearIdempotencyKey ? null : (idempotencyKey ?? this.idempotencyKey),
      nfcAvailability: nfcAvailability ?? this.nfcAvailability,
      qrFallbackAvailable: qrFallbackAvailable ?? this.qrFallbackAvailable,
      awaitingCollectConfirmation:
          awaitingCollectConfirmation ?? this.awaitingCollectConfirmation,
      isUncertainOutcome: isUncertainOutcome ?? this.isUncertainOutcome,
    );
  }

  @override
  List<Object?> get props => [
        phase,
        scanPayload,
        resolvedStation,
        gpsReading,
        collectResult,
        failure,
        statusMessage,
        idempotencyKey,
        nfcAvailability,
        qrFallbackAvailable,
        awaitingCollectConfirmation,
        isUncertainOutcome,
      ];
}
