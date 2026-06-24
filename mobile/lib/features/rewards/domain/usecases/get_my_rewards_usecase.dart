import '../entities/user_reward.dart';
import '../repositories/rewards_repository.dart';

class GetMyRewardsUseCase {
  const GetMyRewardsUseCase(this._repository);

  final RewardsRepository _repository;

  Future<List<UserReward>> call({
    String? status,
    int page = 0,
    int size = 50,
  }) {
    return _repository.getMyRewards(
      status: status,
      page: page,
      size: size,
    );
  }
}
