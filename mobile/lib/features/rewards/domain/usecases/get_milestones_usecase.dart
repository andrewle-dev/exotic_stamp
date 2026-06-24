import '../entities/milestone.dart';
import '../repositories/rewards_repository.dart';

class GetMilestonesUseCase {
  const GetMilestonesUseCase(this._repository);

  final RewardsRepository _repository;

  Future<List<Milestone>> call({
    required String campaignId,
    int page = 0,
    int size = 50,
  }) {
    return _repository.getMilestones(
      campaignId: campaignId,
      page: page,
      size: size,
    );
  }
}
