import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/rewards_overview.dart';

enum RewardsStatus {
  initial,
  loading,
  loaded,
  noRewardsYet,
  failure,
}

class RewardsState extends Equatable {
  const RewardsState({
    this.status = RewardsStatus.initial,
    this.overview,
    this.failure,
    this.isRefreshing = false,
  });

  final RewardsStatus status;
  final RewardsOverview? overview;
  final Failure? failure;
  final bool isRefreshing;

  RewardsState copyWith({
    RewardsStatus? status,
    RewardsOverview? overview,
    Failure? failure,
    bool? isRefreshing,
    bool clearFailure = false,
    bool clearOverview = false,
  }) {
    return RewardsState(
      status: status ?? this.status,
      overview: clearOverview ? null : overview ?? this.overview,
      failure: clearFailure ? null : failure ?? this.failure,
      isRefreshing: isRefreshing ?? this.isRefreshing,
    );
  }

  @override
  List<Object?> get props => [status, overview, failure, isRefreshing];
}
