import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../../stations/domain/entities/line.dart';
import '../../../stations/domain/usecases/get_lines_usecase.dart';
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

    try {
      final lines = await _getLinesUseCase();
      final selectedLineId = _resolveDefaultLineId(lines, state.selectedLineId);
      final stampBook = await _getStampBookUseCase(lineId: selectedLineId);
      emit(
        _loadedState(
          lines: lines,
          selectedLineId: selectedLineId ?? stampBook.lineId,
          stampBook: stampBook,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          failure: failure,
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

  Future<void> refresh() async {
    emit(state.copyWith(isRefreshing: true, clearFailure: true));
    try {
      final stampBook =
          await _getStampBookUseCase(lineId: state.selectedLineId);
      emit(
        _loadedState(
          lines: state.lines,
          selectedLineId: state.selectedLineId ?? stampBook.lineId,
          stampBook: stampBook,
          isRefreshing: false,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: StampBookStatus.failure,
          failure: failure,
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
          failure: failure,
        ),
      );
    }
  }

  StampBookState _loadedState({
    required List<Line> lines,
    required String? selectedLineId,
    required StampBook stampBook,
    bool isRefreshing = false,
  }) {
    final isEmpty = !stampBook.hasCollectedStamps;
    return StampBookState(
      status: isEmpty ? StampBookStatus.empty : StampBookStatus.loaded,
      lines: lines,
      selectedLineId: selectedLineId,
      stampBook: stampBook,
      isRefreshing: isRefreshing,
    );
  }

  String? _resolveDefaultLineId(List<Line> lines, String? currentLineId) {
    if (currentLineId != null &&
        lines.any((line) => line.id == currentLineId)) {
      return currentLineId;
    }
    if (lines.isEmpty) {
      return null;
    }
    for (final line in lines) {
      if (line.status == 'ACTIVE') {
        return line.id;
      }
    }
    return lines.first.id;
  }
}
