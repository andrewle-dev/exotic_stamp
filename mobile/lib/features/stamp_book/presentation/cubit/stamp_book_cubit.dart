import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/error_mapper.dart';
import '../../../../core/errors/failure.dart';
import '../../../stations/domain/entities/line.dart';
import '../../../stations/domain/usecases/get_lines_usecase.dart';
import '../utils/stamp_book_line_filter.dart';
import '../../domain/entities/stamp_book.dart';
import '../../domain/usecases/get_stamp_book_usecase.dart';
import 'stamp_book_state.dart';

class StampBookCubit extends Cubit<StampBookState> {
  StampBookCubit({
    required GetStampBookUseCase getStampBookUseCase,
    required GetLinesUseCase getLinesUseCase,
  })  : _getStampBookUseCase = getStampBookUseCase,
        _getLinesUseCase = getLinesUseCase,
        super(const StampBookState());

  final GetStampBookUseCase _getStampBookUseCase;
  final GetLinesUseCase _getLinesUseCase;

  Future<void> load() async {
    emit(
      state.copyWith(
        status: StampBookStatus.loading,
        clearFailure: true,
        isRefreshing: false,
      ),
    );

    List<Line> lines = state.lines;
    String? selectedLineId = state.selectedLineId;

    try {
      lines = await _getLinesUseCase();
      selectedLineId = _resolveDefaultLineId(lines, selectedLineId);

      if (selectedLineId == null) {
        emit(
          state.copyWith(
            status: StampBookStatus.failure,
            lines: lines,
            clearStampBook: true,
            failure: const Failure(
              code: FailureCode.validationError,
              message:
                  'Không có tuyến metro khả dụng. Vui lòng chọn tuyến khi có dữ liệu.',
            ),
            isRefreshing: false,
          ),
        );
        return;
      }

      emit(
        state.copyWith(
          lines: lines,
          selectedLineId: selectedLineId,
        ),
      );

      final stampBook = await _getStampBookUseCase(lineId: selectedLineId);
      emit(
        _loadedState(
          lines: lines,
          selectedLineId: selectedLineId,
          stampBook: stampBook,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          lines: lines,
          selectedLineId: selectedLineId,
          failure: _mapStampBookFailure(failure),
          isRefreshing: false,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          lines: lines,
          selectedLineId: selectedLineId,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải Sổ stamp.',
          ),
          isRefreshing: false,
        ),
      );
    }
  }

  Future<void> refresh() async {
    final lineId = _apiLineId(state.selectedLineId);
    if (lineId == null) {
      await load();
      return;
    }

    emit(state.copyWith(isRefreshing: true, clearFailure: true));
    try {
      final stampBook = await _getStampBookUseCase(lineId: lineId);
      emit(
        _loadedState(
          lines: state.lines,
          selectedLineId: lineId,
          stampBook: stampBook,
          isRefreshing: false,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          failure: _mapStampBookFailure(failure),
          isRefreshing: false,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải Sổ stamp.',
          ),
          isRefreshing: false,
        ),
      );
    }
  }

  Future<void> selectLine(String lineId) async {
    // Backend stamp-book cannot resolve an all-lines default when multiple
    // active default campaigns exist — never call without a concrete lineId.
    if (lineId == StampBookLineFilter.allLines) {
      return;
    }

    emit(
      state.copyWith(
        status: StampBookStatus.loading,
        selectedLineId: lineId,
        clearFailure: true,
        isRefreshing: false,
      ),
    );

    try {
      final stampBook = await _getStampBookUseCase(lineId: lineId);
      emit(
        _loadedState(
          lines: state.lines,
          selectedLineId: lineId,
          stampBook: stampBook,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          failure: _mapStampBookFailure(failure),
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải Sổ stamp.',
          ),
        ),
      );
    }
  }

  StampBookState _loadedState({
    required List<Line> lines,
    required String selectedLineId,
    required StampBook stampBook,
    bool isRefreshing = false,
  }) {
    // Empty only when the catalogue has no stations — 0 collected still shows
    // the full locked grid.
    final isEmpty = stampBook.stations.isEmpty;
    return StampBookState(
      status: isEmpty ? StampBookStatus.empty : StampBookStatus.loaded,
      lines: lines,
      selectedLineId: selectedLineId,
      stampBook: stampBook,
      isRefreshing: isRefreshing,
    );
  }

  /// Prefer an already-selected concrete line; otherwise first active line.
  String? _resolveDefaultLineId(List<Line> lines, String? currentLineId) {
    if (currentLineId != null &&
        currentLineId != StampBookLineFilter.allLines &&
        lines.any((line) => line.id == currentLineId)) {
      return currentLineId;
    }

    final activeLines = lines
        .where((line) => (line.status ?? 'ACTIVE').toUpperCase() == 'ACTIVE')
        .toList();
    final pool = activeLines.isNotEmpty ? activeLines : lines;
    if (pool.isEmpty) {
      return null;
    }
    return pool.first.id;
  }

  /// Stamp-book requests must always disambiguate by line when possible.
  String? _apiLineId(String? selectedLineId) {
    if (selectedLineId == null ||
        selectedLineId == StampBookLineFilter.allLines) {
      return null;
    }
    return selectedLineId;
  }

  Failure _mapStampBookFailure(Failure failure) {
    if (failure.code == FailureCode.defaultCampaignAmbiguous ||
        failure.backendCode == 'DEFAULT_CAMPAIGN_AMBIGUOUS' ||
        _looksLikeAmbiguousCampaign(failure.message)) {
      return Failure(
        code: FailureCode.defaultCampaignAmbiguous,
        message: ErrorMapper.defaultCampaignAmbiguousMessage,
        statusCode: failure.statusCode,
        backendCode: failure.backendCode ?? 'DEFAULT_CAMPAIGN_AMBIGUOUS',
      );
    }
    return failure;
  }

  bool _looksLikeAmbiguousCampaign(String message) {
    final normalized = message.toLowerCase();
    return normalized.contains('multiple active default campaigns') ||
        normalized.contains('provide lineid to disambiguate');
  }
}
