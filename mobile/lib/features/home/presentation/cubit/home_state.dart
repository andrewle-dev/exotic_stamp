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
  });

  final HomeStatus status;
  final HomeSummary? summary;
  final Failure? failure;

  HomeState copyWith({
    HomeStatus? status,
    HomeSummary? summary,
    Failure? failure,
    bool clearSummary = false,
    bool clearFailure = false,
  }) {
    return HomeState(
      status: status ?? this.status,
      summary: clearSummary ? null : (summary ?? this.summary),
      failure: clearFailure ? null : (failure ?? this.failure),
    );
  }

  @override
  List<Object?> get props => [status, summary, failure];
}
