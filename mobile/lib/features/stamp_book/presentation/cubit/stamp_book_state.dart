import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../../stations/domain/entities/line.dart';
import '../../domain/entities/stamp_book.dart';

enum StampBookStatus {
  initial,
  loading,
  loaded,
  empty,
  failure,
}

class StampBookState extends Equatable {
  const StampBookState({
    this.status = StampBookStatus.initial,
    this.lines = const [],
    this.selectedLineId,
    this.stampBook,
    this.failure,
    this.isRefreshing = false,
  });

  final StampBookStatus status;
  final List<Line> lines;
  final String? selectedLineId;
  final StampBook? stampBook;
  final Failure? failure;
  final bool isRefreshing;

  StampBookState copyWith({
    StampBookStatus? status,
    List<Line>? lines,
    String? selectedLineId,
    StampBook? stampBook,
    Failure? failure,
    bool? isRefreshing,
    bool clearFailure = false,
    bool clearStampBook = false,
  }) {
    return StampBookState(
      status: status ?? this.status,
      lines: lines ?? this.lines,
      selectedLineId: selectedLineId ?? this.selectedLineId,
      stampBook: clearStampBook ? null : (stampBook ?? this.stampBook),
      failure: clearFailure ? null : (failure ?? this.failure),
      isRefreshing: isRefreshing ?? this.isRefreshing,
    );
  }

  @override
  List<Object?> get props => [
        status,
        lines,
        selectedLineId,
        stampBook,
        failure,
        isRefreshing,
      ];
}
