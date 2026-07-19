import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/config/scan_capabilities.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/location/app_location_service.dart';
import '../../../../core/nfc/nfc_availability.dart';
import '../../../../core/nfc/nfc_reader.dart';
import '../../../../core/utils/idempotency_key_generator.dart';
import '../../../home/presentation/home_reload_signal.dart';
import '../../domain/entities/collect_status_outcome.dart';
import '../../domain/entities/scan_payload.dart';
import '../../domain/entities/scan_type.dart';
import '../../domain/usecases/check_collect_status_usecase.dart';
import '../../domain/usecases/collect_stamp_usecase.dart';
import '../../domain/usecases/resolve_scan_usecase.dart';
import 'scan_flow_state.dart';

class ScanFlowCubit extends Cubit<ScanFlowState> {
  ScanFlowCubit({
    required ResolveScanUseCase resolveScanUseCase,
    required CollectStampUseCase collectStampUseCase,
    required CheckCollectStatusUseCase checkCollectStatusUseCase,
    required AppLocationService locationService,
    required IdempotencyKeyGenerator idempotencyKeyGenerator,
    NfcReader? nfcReader,
    HomeReloadSignal? homeReloadSignal,
  })  : _resolveScanUseCase = resolveScanUseCase,
        _collectStampUseCase = collectStampUseCase,
        _checkCollectStatusUseCase = checkCollectStatusUseCase,
        _locationService = locationService,
        _idempotencyKeyGenerator = idempotencyKeyGenerator,
        _nfcReader = nfcReader ?? NfcReader(),
        _homeReloadSignal = homeReloadSignal,
        super(const ScanFlowState());

  final ResolveScanUseCase _resolveScanUseCase;
  final CollectStampUseCase _collectStampUseCase;
  final CheckCollectStatusUseCase _checkCollectStatusUseCase;
  final AppLocationService _locationService;
  final IdempotencyKeyGenerator _idempotencyKeyGenerator;
  final NfcReader _nfcReader;
  final HomeReloadSignal? _homeReloadSignal;

  void _notifyHomeCollectionChanged() {
    _homeReloadSignal?.requestReload();
  }

  Future<void> initialize() async {
    emit(
      state.copyWith(
        phase: ScanFlowPhase.checkingNfcAvailability,
        clearFailure: true,
        clearStatusMessage: true,
        isUncertainOutcome: false,
        awaitingCollectConfirmation: false,
      ),
    );

    final availability = await _nfcReader.checkAvailability();
    // QR UI temporarily hidden — keep flag false unless ScanCapabilities.enableQrFlow.
    final qrFallback = ScanCapabilities.enableQrFlow &&
        availability != NfcAvailabilityStatus.enabled;

    emit(
      state.copyWith(
        nfcAvailability: availability,
        qrFallbackAvailable: qrFallback,
        phase: ScanFlowPhase.waitingForNfc,
      ),
    );
  }

  void enableQrFallback() {
    if (!ScanCapabilities.enableQrFlow) {
      return;
    }
    emit(
      state.copyWith(
        phase: ScanFlowPhase.qrFallbackReady,
        qrFallbackAvailable: true,
      ),
    );
  }

  Future<void> onNfcPayloadRead(String payload) async {
    await _handlePayload(
      ScanPayload(scanType: ScanType.nfc, payload: payload.trim()),
      readingPhase: ScanFlowPhase.readingNfc,
    );
  }

  Future<void> onQrPayloadRead(String payload) async {
    if (!ScanCapabilities.enableQrFlow) {
      return;
    }
    await _handlePayload(
      ScanPayload(scanType: ScanType.qr, payload: payload.trim()),
      readingPhase: ScanFlowPhase.qrFallbackReady,
    );
  }

  Future<void> _handlePayload(
    ScanPayload payload, {
    required ScanFlowPhase readingPhase,
  }) async {
    if (payload.payload.isEmpty) {
      return;
    }

    emit(
      state.copyWith(
        phase: readingPhase,
        scanPayload: payload,
        clearFailure: true,
        clearStatusMessage: true,
        clearCollectResult: true,
        isUncertainOutcome: false,
        awaitingCollectConfirmation: false,
      ),
    );

    emit(state.copyWith(phase: ScanFlowPhase.checkingLocation));
    final location = await _locationService.getCurrentReading();
    if (!location.isSuccess) {
      final issue = location.issue;
      emit(
        state.copyWith(
          phase: issue == null
              ? ScanFlowPhase.unknownError
              : _phaseForLocationIssue(issue),
          statusMessage: location.message,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể xác minh vị trí.',
          ),
        ),
      );
      return;
    }

    emit(state.copyWith(phase: ScanFlowPhase.resolvingStation));
    try {
      final station = await _resolveScanUseCase(payload);
      final idempotencyKey = _idempotencyKeyGenerator.generate();

      emit(
        state.copyWith(
          phase: ScanFlowPhase.checkingLocation,
          scanPayload: payload,
          resolvedStation: station,
          gpsReading: location.reading,
          idempotencyKey: idempotencyKey,
          awaitingCollectConfirmation: true,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(_failureState(failure));
    } catch (_) {
      emit(
        state.copyWith(
          phase: ScanFlowPhase.unknownError,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể xác minh ga từ mã quét.',
          ),
        ),
      );
    }
  }

  Future<void> confirmCollect() async {
    final payload = state.scanPayload;
    final gps = state.gpsReading;
    final idempotencyKey = state.idempotencyKey;

    if (payload == null || gps == null || idempotencyKey == null) {
      return;
    }

    emit(
      state.copyWith(
        phase: ScanFlowPhase.collecting,
        awaitingCollectConfirmation: false,
        clearFailure: true,
        isUncertainOutcome: false,
      ),
    );

    try {
      final result = await _collectStampUseCase(
        payload: payload,
        latitude: gps.latitude,
        longitude: gps.longitude,
        accuracyMeters: gps.accuracyMeters,
        idempotencyKey: idempotencyKey,
      );

      if (!result.isNew) {
        emit(
          state.copyWith(
            phase: ScanFlowPhase.duplicate,
            collectResult: result,
            clearFailure: true,
          ),
        );
        return;
      }

      _notifyHomeCollectionChanged();

      if (result.sponsorAd != null) {
        emit(
          state.copyWith(
            phase: ScanFlowPhase.preStampAd,
            collectResult: result,
            clearFailure: true,
          ),
        );
        return;
      }

      emit(
        state.copyWith(
          phase: ScanFlowPhase.success,
          collectResult: result,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      if (failure.code == FailureCode.stampDuplicate) {
        emit(
          state.copyWith(
            phase: ScanFlowPhase.duplicate,
            failure: failure,
          ),
        );
        return;
      }

      final uncertain = _isUncertainCollectFailure(failure);
      emit(
        _failureState(failure).copyWith(
          isUncertainOutcome: uncertain,
          statusMessage: uncertain
              ? 'Kết quả thu thập chưa xác định. Kiểm tra Sổ stamp hoặc thử lại.'
              : failure.message,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          phase: ScanFlowPhase.unknownError,
          isUncertainOutcome: true,
          statusMessage:
              'Kết quả thu thập chưa xác định. Kiểm tra Sổ stamp hoặc thử lại.',
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể hoàn tất thu thập stamp.',
          ),
        ),
      );
    }
  }

  Future<void> checkCollectStatus() async {
    final idempotencyKey = state.idempotencyKey;
    if (idempotencyKey == null) {
      return;
    }

    emit(
      state.copyWith(
        phase: ScanFlowPhase.checkingCollectStatus,
        clearFailure: true,
      ),
    );

    try {
      final statusResult = await _checkCollectStatusUseCase(
        idempotencyKey: idempotencyKey,
      );

      switch (statusResult.outcome) {
        case CollectStatusOutcome.success:
          final result = statusResult.collectResult;
          if (result == null) {
            _emitStillUncertain(
              'Kết quả thu thập chưa xác định. Kiểm tra Sổ stamp hoặc thử lại.',
            );
            return;
          }
          _notifyHomeCollectionChanged();
          emit(
            state.copyWith(
              phase: ScanFlowPhase.success,
              collectResult: result,
              isUncertainOutcome: false,
              clearFailure: true,
              statusMessage: null,
            ),
          );
        case CollectStatusOutcome.duplicate:
          final result = statusResult.collectResult;
          emit(
            state.copyWith(
              phase: ScanFlowPhase.duplicate,
              collectResult: result,
              isUncertainOutcome: false,
              clearFailure: true,
              statusMessage: 'Stamp đã được thu trước đó.',
            ),
          );
        case CollectStatusOutcome.notFound:
        case CollectStatusOutcome.pending:
          _emitStillUncertain(
            'Chưa tìm thấy stamp cho lần quét này. Hệ thống có thể vẫn đang '
            'xử lý — thử lại sau hoặc mở Sổ stamp.',
          );
        case CollectStatusOutcome.failed:
          _emitStillUncertain(
            'Thu thập không thành công. Kiểm tra Sổ stamp hoặc thử lại.',
          );
        case CollectStatusOutcome.unknown:
          _emitStillUncertain(
            'Kết quả thu thập chưa xác định. Kiểm tra Sổ stamp hoặc thử lại.',
          );
      }
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          phase: ScanFlowPhase.networkError,
          failure: failure,
          isUncertainOutcome: true,
          statusMessage: failure.message,
        ),
      );
    } catch (_) {
      _emitStillUncertain(
        'Kết quả thu thập chưa xác định. Kiểm tra Sổ stamp hoặc thử lại.',
      );
    }
  }

  void _emitStillUncertain(String message) {
    emit(
      state.copyWith(
        phase: ScanFlowPhase.networkError,
        isUncertainOutcome: true,
        statusMessage: message,
        clearFailure: true,
      ),
    );
  }

  void resetFlow() {
    emit(const ScanFlowState());
  }

  Future<void> resumeWaitingForNfc() async {
    emit(state.copyWith(phase: ScanFlowPhase.waitingForNfc));
  }

  void acknowledgePreStampAd() {
    if (state.phase != ScanFlowPhase.preStampAd || state.collectResult == null) {
      return;
    }
    emit(
      state.copyWith(
        phase: ScanFlowPhase.success,
        clearFailure: true,
      ),
    );
  }

  Future<void> refreshLocation() async {
    if (state.scanPayload == null) {
      return;
    }

    emit(state.copyWith(phase: ScanFlowPhase.checkingLocation, clearFailure: true));
    final location = await _locationService.getCurrentReading();
    if (!location.isSuccess) {
      final issue = location.issue;
      emit(
        state.copyWith(
          phase: issue == null
              ? ScanFlowPhase.unknownError
              : _phaseForLocationIssue(issue),
          statusMessage: location.message,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể xác minh vị trí.',
          ),
        ),
      );
      return;
    }

    emit(
      state.copyWith(
        gpsReading: location.reading,
        awaitingCollectConfirmation: state.resolvedStation != null,
        phase: state.resolvedStation == null
            ? ScanFlowPhase.resolvingStation
            : ScanFlowPhase.checkingLocation,
        clearFailure: true,
      ),
    );

    if (state.resolvedStation == null && state.scanPayload != null) {
      try {
        final station = await _resolveScanUseCase(state.scanPayload!);
        emit(
          state.copyWith(
            resolvedStation: station,
            awaitingCollectConfirmation: true,
            phase: ScanFlowPhase.checkingLocation,
          ),
        );
      } on Failure catch (failure) {
        emit(_failureState(failure));
      }
    }
  }

  NfcReader get nfcReader => _nfcReader;

  ScanFlowState _failureState(Failure failure) {
    return state.copyWith(
      phase: _phaseForFailure(failure),
      failure: failure,
      statusMessage: failure.message,
      awaitingCollectConfirmation: false,
    );
  }

  ScanFlowPhase _phaseForFailure(Failure failure) {
    return switch (failure.code) {
      FailureCode.stampDuplicate => ScanFlowPhase.duplicate,
      FailureCode.nfcInvalid => ScanFlowPhase.invalidTag,
      FailureCode.qrExpired => ScanFlowPhase.qrExpired,
      FailureCode.gpsOutsideRange => ScanFlowPhase.gpsOutsideRange,
      FailureCode.stationInactive => ScanFlowPhase.stationInactive,
      FailureCode.campaignInactive => ScanFlowPhase.campaignInactive,
      FailureCode.networkError => ScanFlowPhase.networkError,
      FailureCode.unauthorized ||
      FailureCode.tokenExpired =>
        ScanFlowPhase.unknownError,
      _ => ScanFlowPhase.unknownError,
    };
  }

  bool _isUncertainCollectFailure(Failure failure) {
    return failure.code == FailureCode.networkError ||
        failure.statusCode == null && failure.code == FailureCode.unknown;
  }

  ScanFlowPhase _phaseForLocationIssue(LocationIssue issue) {
    return switch (issue) {
      LocationIssue.permissionDenied => ScanFlowPhase.locationPermissionDenied,
      LocationIssue.serviceDisabled => ScanFlowPhase.locationServiceDisabled,
      LocationIssue.lowAccuracy => ScanFlowPhase.locationLowAccuracy,
      LocationIssue.timeout => ScanFlowPhase.locationTimeout,
    };
  }
}
