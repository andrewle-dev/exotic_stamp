import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/home_summary.dart';

enum HomeStatus {
  initial,
  loading,
  loaded,
  failure,
}

class HomeState extends Equatable {
  const HomeState({
    this.status = HomeStatus.initial,
    this.summary,
    this.failure,
    this.isRefreshing = false,
  });

  final HomeStatus status;
  final HomeSummary? summary;
  final Failure? failure;

  /// Soft refetch in progress; previous [summary] remains visible.
  final bool isRefreshing;

  HomeState copyWith({
    HomeStatus? status,
    HomeSummary? summary,
    Failure? failure,
    bool? isRefreshing,
    bool clearSummary = false,
    bool clearFailure = false,
  }) {
    return HomeState(
      status: status ?? this.status,
      summary: clearSummary ? null : (summary ?? this.summary),
      failure: clearFailure ? null : (failure ?? this.failure),
      isRefreshing: isRefreshing ?? this.isRefreshing,
    );
  }

  @override
  List<Object?> get props => [status, summary, failure, isRefreshing];
}
