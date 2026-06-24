import 'dart:typed_data';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/photo_share_context.dart';
import '../../domain/repositories/memories_repository.dart';
import '../../domain/usecases/record_share_event_usecase.dart';
import '../services/native_share_service.dart';
import '../services/photo_picker_service.dart';
import '../services/share_temp_file_writer.dart';
import 'photo_share_state.dart';

class PhotoShareCubit extends Cubit<PhotoShareState> {
  PhotoShareCubit({
    required RecordShareEventUseCase recordShareEventUseCase,
    required PhotoPickerService photoPickerService,
    required NativeShareService nativeShareService,
    ShareTempFileWriter? tempFileWriter,
    PhotoShareContext? initialContext,
  })  : _recordShareEventUseCase = recordShareEventUseCase,
        _photoPickerService = photoPickerService,
        _nativeShareService = nativeShareService,
        _tempFileWriter = tempFileWriter ?? PathProviderShareTempFileWriter(),
        super(
          PhotoShareState(
            context: initialContext,
            caption: _defaultCaption(initialContext),
          ),
        );

  final RecordShareEventUseCase _recordShareEventUseCase;
  final PhotoPickerService _photoPickerService;
  final NativeShareService _nativeShareService;
  final ShareTempFileWriter _tempFileWriter;

  static String _defaultCaption(PhotoShareContext? context) {
    if (context == null) {
      return '';
    }
    return 'Mình vừa nhận stamp mới tại ${context.stationName}!';
  }

  void setCaption(String value) {
    if (state.caption == value) {
      return;
    }
    emit(state.copyWith(caption: value));
  }

  void toggleShowStationName(bool value) {
    emit(state.copyWith(showStationName: value));
  }

  void toggleShowCollectionDate(bool value) {
    emit(state.copyWith(showCollectionDate: value));
  }

  Future<void> pickFromGallery() => _pick(_photoPickerService.pickFromGallery);

  Future<void> pickFromCamera() => _pick(_photoPickerService.pickFromCamera);

  Future<void> _pick(Future<PickedPhoto?> Function() action) async {
    emit(
      state.copyWith(
        status: PhotoShareStatus.pickingPhoto,
        clearFailure: true,
      ),
    );
    try {
      final picked = await action();
      if (picked == null) {
        emit(
          state.copyWith(
            status: state.hasPhoto
                ? PhotoShareStatus.editing
                : PhotoShareStatus.initial,
          ),
        );
        return;
      }
      emit(
        state.copyWith(
          status: PhotoShareStatus.editing,
          photoPath: picked.path,
          clearFailure: true,
        ),
      );
    } on Object catch (error) {
      emit(
        state.copyWith(
          status: state.hasPhoto
              ? PhotoShareStatus.editing
              : PhotoShareStatus.initial,
          failure: Failure(
            code: FailureCode.unknown,
            message: 'Không thể chọn ảnh. ${error.runtimeType}',
          ),
        ),
      );
    }
  }

  void clearPhoto() {
    emit(
      state.copyWith(
        status: PhotoShareStatus.initial,
        clearPhoto: true,
        clearFailure: true,
      ),
    );
  }

  /// Shares the composed image via native share sheet.
  ///
  /// [composedImageBytes] is rendered locally; no upload occurs.
  Future<void> shareComposedImage(Uint8List composedImageBytes) async {
    emit(
      state.copyWith(
        status: PhotoShareStatus.sharing,
        clearFailure: true,
      ),
    );

    try {
      final file = await _tempFileWriter.writePng(composedImageBytes);
      final result = await _nativeShareService.shareImage(
        filePath: file.path,
        text: state.caption.trim().isEmpty ? null : state.caption.trim(),
      );

      if (!result.isUserSuccess) {
        emit(
          state.copyWith(
            status: PhotoShareStatus.shareFailed,
            failure: const Failure(
              code: FailureCode.unknown,
              message: 'Không thể mở bảng chia sẻ.',
            ),
          ),
        );
        return;
      }

      emit(
        state.copyWith(
          status: PhotoShareStatus.shared,
          clearFailure: true,
        ),
      );
      await _trackShareBestEffort();
    } on Object {
      emit(
        state.copyWith(
          status: PhotoShareStatus.shareFailed,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể chia sẻ ảnh.',
          ),
        ),
      );
    }
  }

  Future<void> _trackShareBestEffort() async {
    final context = state.context;
    if (context == null) {
      return;
    }

    try {
      await _recordShareEventUseCase.call(
        RecordShareEventParams(
          platform: 'native',
          shareType: context.shareType,
          targetId: context.targetId,
          metadata: {
            if (state.showStationName) 'stationName': context.stationName,
            if (state.showCollectionDate && context.collectedAt != null)
              'collectedAt': context.collectedAt!.toUtc().toIso8601String(),
            if (state.caption.trim().isNotEmpty)
              'caption': state.caption.trim(),
          },
        ),
      );
      emit(state.copyWith(trackingRecorded: true, trackingFailed: false));
    } on Failure {
      emit(state.copyWith(trackingFailed: true));
    } on Object {
      emit(state.copyWith(trackingFailed: true));
    }
  }
}
