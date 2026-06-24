import '../entities/rewards_overview.dart';
import '../repositories/rewards_repository.dart';

class GetRewardsOverviewUseCase {
  const GetRewardsOverviewUseCase(this._repository);

  final RewardsRepository _repository;

  Future<RewardsOverview> call() => _repository.getRewardsOverview();
}
