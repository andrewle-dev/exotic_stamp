import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/location/app_location_service.dart';
import '../../../../core/nfc/nfc_availability.dart';
import '../../../../core/nfc/nfc_writer.dart';
import '../../../stations/domain/entities/station.dart';
import '../../../stations/domain/repositories/stations_repository.dart';
import '../../data/datasources/admin_scan_key_remote_datasource.dart';
import '../../data/models/station_scan_key_models.dart';
import 'nfc_tag_writer_state.dart';

class NfcTagWriterCubit extends Cubit<NfcTagWriterState> {
  NfcTagWriterCubit({
    required StationsRepository stationsRepository,
    required AdminScanKeyRemoteDataSource adminScanKeyRemoteDataSource,
    required AppLocationService locationService,
    NfcWriter? nfcWriter,
  })  : _stationsRepository = stationsRepository,
        _adminScanKeyRemoteDataSource = adminScanKeyRemoteDataSource,
        _locationService = locationService,
        _nfcWriter = nfcWriter ?? NfcWriter(),
        super(const NfcTagWriterState());

  final StationsRepository _stationsRepository;
  final AdminScanKeyRemoteDataSource _adminScanKeyRemoteDataSource;
  final AppLocationService _locationService;
  final NfcWriter _nfcWriter;

  Future<void> loadStations() async {
    emit(
      state.copyWith(
        phase: NfcTagWriterPhase.loadingStations,
        clearFailure: true,
        clearStatusMessage: true,
      ),
    );
    try {
      final stations = await _stationsRepository.getStations();
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.ready,
          stations: stations,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.error,
          failure: failure,
          statusMessage: failure.message,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.error,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải danh sách ga.',
          ),
        ),
      );
    }
  }

  void selectStation(Station station) {
    emit(
      state.copyWith(
        selectedStation: station,
        clearCreatedKey: true,
        clearReadBack: true,
        clearFailure: true,
        phase: NfcTagWriterPhase.ready,
      ),
    );
  }

  Future<void> createKey({String? label, String? placementNote}) async {
    final station = state.selectedStation;
    if (station == null) {
      return;
    }

    emit(
      state.copyWith(
        phase: NfcTagWriterPhase.creatingKey,
        clearFailure: true,
        clearCreatedKey: true,
      ),
    );

    try {
      final created = await _adminScanKeyRemoteDataSource.createScanKey(
        stationId: station.id,
        scanType: 'NFC',
        label: label,
        placementNote: placementNote,
      );
      // Activate so the tag is usable after write+verify.
      await _adminScanKeyRemoteDataSource.activateScanKey(created.id);
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.keyReady,
          createdKey: StationScanKeyCreatedModel(
            id: created.id,
            stationId: created.stationId,
            scanType: created.scanType,
            payloadToWrite: created.payloadToWrite,
            keyPrefix: created.keyPrefix,
            status: 'ACTIVE',
            label: created.label,
            placementNote: created.placementNote,
          ),
          statusMessage: 'Key created. Write it to the NFC tag.',
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.error,
          failure: failure,
          statusMessage: failure.message,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.error,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tạo scan key.',
          ),
        ),
      );
    }
  }

  Future<void> writeTagAndVerifyInstallation() async {
    final created = state.createdKey;
    if (created == null || created.payloadToWrite.isEmpty) {
      return;
    }

    emit(
      state.copyWith(
        phase: NfcTagWriterPhase.writingTag,
        clearFailure: true,
        statusMessage: 'Hold phone near the NFC tag…',
      ),
    );

    try {
      final availability = await _nfcWriter.checkAvailability();
      if (availability != NfcAvailabilityStatus.enabled) {
        emit(
          state.copyWith(
            phase: NfcTagWriterPhase.error,
            statusMessage: 'NFC is not available on this device.',
            failure: const Failure(
              code: FailureCode.nfcInvalid,
              message: 'NFC is not available.',
            ),
          ),
        );
        return;
      }

      final writeResult = await _nfcWriter.writeAndVerify(
        payload: created.payloadToWrite,
      );
      if (!writeResult.matches) {
        emit(
          state.copyWith(
            phase: NfcTagWriterPhase.error,
            readBackPayload: writeResult.readBackPayload,
            statusMessage: 'Read-back payload does not match.',
            failure: const Failure(
              code: FailureCode.nfcInvalid,
              message: 'NFC read-back mismatch.',
            ),
          ),
        );
        return;
      }

      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.verifyingInstall,
          readBackPayload: writeResult.readBackPayload,
          statusMessage: 'Verifying installation with GPS…',
        ),
      );

      final location = await _locationService.getCurrentReading();
      if (!location.isSuccess || location.reading == null) {
        emit(
          state.copyWith(
            phase: NfcTagWriterPhase.error,
            statusMessage: location.message ?? 'GPS required for verification.',
            failure: const Failure(
              code: FailureCode.unknown,
              message: 'GPS required for installation verification.',
            ),
          ),
        );
        return;
      }

      final platform = Platform.isIOS
          ? 'IOS'
          : Platform.isAndroid
              ? 'ANDROID'
              : defaultTargetPlatform.name.toUpperCase();

      final verified = await _adminScanKeyRemoteDataSource.verifyInstallation(
        id: created.id,
        payloadReadBack: writeResult.readBackPayload,
        latitude: location.reading!.latitude,
        longitude: location.reading!.longitude,
        accuracyMeters: location.reading!.accuracyMeters,
        devicePlatform: platform,
        appVersion: AppConstants.appVersion,
      );

      if (!verified.verified) {
        emit(
          state.copyWith(
            phase: NfcTagWriterPhase.error,
            statusMessage: 'Installation verification failed.',
          ),
        );
        return;
      }

      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.success,
          statusMessage: 'Tag written and installation verified.',
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.error,
          failure: failure,
          statusMessage: failure.message,
        ),
      );
    } catch (error) {
      emit(
        state.copyWith(
          phase: NfcTagWriterPhase.error,
          failure: Failure(
            code: FailureCode.unknown,
            message: error.toString(),
          ),
          statusMessage: 'Write or verify failed.',
        ),
      );
    }
  }
}
