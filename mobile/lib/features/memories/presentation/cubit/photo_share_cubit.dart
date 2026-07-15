import 'dart:typed_data';

import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../../stamp_book/domain/entities/stamp_book.dart';
import '../../../stamp_book/domain/usecases/get_stamp_book_usecase.dart';
import '../../domain/entities/photo_share_context.dart';
import '../../domain/entities/stamp_share_option.dart';
import '../../domain/repositories/memories_repository.dart';
import '../../domain/usecases/record_share_event_usecase.dart';
import '../services/clipboard_service.dart';
import '../services/flutter_clipboard_service.dart';
import '../services/native_share_service.dart';
import '../services/photo_picker_service.dart';
import '../services/share_temp_file_writer.dart';
import '../widgets/photo_share_stamp_platform.dart';
import 'photo_share_state.dart';

class PhotoShareCubit extends Cubit<PhotoShareState> {
  PhotoShareCubit({
    required RecordShareEventUseCase recordShareEventUseCase,
    required GetStampBookUseCase getStampBookUseCase,
    required PhotoPickerService photoPickerService,
    required NativeShareService nativeShareService,
    ClipboardService? clipboardService,
    ShareTempFileWriter? tempFileWriter,
    PhotoShareContext? initialContext,
  })  : _recordShareEventUseCase = recordShareEventUseCase,
        _getStampBookUseCase = getStampBookUseCase,
        _photoPickerService = photoPickerService,
        _nativeShareService = nativeShareService,
        _clipboardService = clipboardService ?? FlutterClipboardService(),
        _tempFileWriter = tempFileWriter ?? PathProviderShareTempFileWriter(),
        super(
          PhotoShareState(
            context: initialContext,
            caption: _defaultCaption(initialContext),
            selectedStationId: initialContext?.stationId,
          ),
        ) {
    loadStampOptions();
  }

  final RecordShareEventUseCase _recordShareEventUseCase;
  final GetStampBookUseCase _getStampBookUseCase;
  final PhotoPickerService _photoPickerService;
  final NativeShareService _nativeShareService;
  final ClipboardService _clipboardService;
  final ShareTempFileWriter _tempFileWriter;

  static String _defaultCaption(PhotoShareContext? context) {
    if (context == null) {
      return '';
    }
    return 'Mình vừa nhận stamp mới tại ${context.stationName}!';
  }

  Future<void> loadStampOptions() async {
    try {
      final book = await _getStampBookUseCase();
      if (isClosed) {
        return;
      }
      final options = _mapStampOptions(book);
      final status = state.status;
      if (status == PhotoShareStatus.sharing ||
          status == PhotoShareStatus.saving) {
        return;
      }
      emit(state.copyWith(stampOptions: options));
    } on Object {
      // Stamp picker is optional — do not block editing.
    }
  }

  List<StampShareOption> _mapStampOptions(StampBook book) {
    return book.stations
        .where((station) => station.collected)
        .map(
          (station) => StampShareOption(
            stationId: station.stationId,
            stationName: station.stationName,
            stampId: station.stampId,
            stampDesignUrl: station.stampDesignUrl,
            collectedAt: station.collectedAt,
            lineName: book.lineName,
          ),
        )
        .toList();
  }

  void selectStamp(StampShareOption option) {
    emit(
      state.copyWith(
        selectedStationId: option.stationId,
        context: PhotoShareContext(
          stationId: option.stationId,
          stationName: option.stationName,
          shareType: PhotoShareContext.shareTypeStampCollected,
          stampId: option.stampId,
          stampDesignUrl: option.stampDesignUrl,
          collectedAt: option.collectedAt,
          lineName: option.lineName,
        ),
        caption: 'Mình vừa nhận stamp mới tại ${option.stationName}!',
      ),
    );
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

  Future<void> shareWithPlatform({
    required PhotoSharePlatform platform,
    required Uint8List composedImageBytes,
  }) async {
    if (platform == PhotoSharePlatform.copy) {
      final text = state.caption.trim();
      if (text.isEmpty) {
        emit(
          state.copyWith(
            status: PhotoShareStatus.shareFailed,
            failure: const Failure(
              code: FailureCode.unknown,
              message: 'Không có nội dung để sao chép.',
            ),
          ),
        );
        return;
      }
      await _clipboardService.copyText(text);
      emit(
        state.copyWith(
          status: PhotoShareStatus.shared,
          clearFailure: true,
        ),
      );
      await _trackShareBestEffort(platform: platform.trackingKey);
      return;
    }

    await shareComposedImage(
      composedImageBytes,
      platform: platform.trackingKey,
    );
  }

  Future<void> shareComposedImage(
    Uint8List composedImageBytes, {
    String platform = 'native',
  }) async {
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
      await _trackShareBestEffort(platform: platform);
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

  Future<void> saveComposedImage(Uint8List composedImageBytes) async {
    emit(
      state.copyWith(
        status: PhotoShareStatus.saving,
        clearFailure: true,
      ),
    );

    try {
      await _tempFileWriter.writePng(composedImageBytes);
      emit(
        state.copyWith(
          status: PhotoShareStatus.saved,
          clearFailure: true,
        ),
      );
    } on Object {
      emit(
        state.copyWith(
          status: PhotoShareStatus.shareFailed,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể lưu ảnh.',
          ),
        ),
      );
    }
  }

  Future<void> _trackShareBestEffort({required String platform}) async {
    final context = state.context;
    if (context == null) {
      return;
    }

    try {
      await _recordShareEventUseCase.call(
        RecordShareEventParams(
          platform: platform,
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
