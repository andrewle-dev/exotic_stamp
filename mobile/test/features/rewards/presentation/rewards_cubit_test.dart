import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/milestone.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/rewards_overview.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';
import 'package:metro_stamp_app/features/rewards/domain/repositories/rewards_repository.dart';
import 'package:metro_stamp_app/features/rewards/domain/usecases/get_rewards_overview_usecase.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_cubit.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_state.dart';
import 'package:mocktail/mocktail.dart';

class MockRewardsRepository extends Mock implements RewardsRepository {}

void main() {
  late MockRewardsRepository repository;
  late RewardsCubit cubit;

  const overviewWithRewards = RewardsOverview(
    campaignId: 'campaign-1',
    progress: RewardsProgress(
      lineId: 'line-1',
      collected: 5,
      total: 10,
      percentage: 50,
    ),
    milestones: [
      Milestone(
        id: 'milestone-1',
        campaignId: 'campaign-1',
        code: 'M5',
        name: '5 Stamps',
        requiredStampCount: 5,
        rewardTitle: 'Coffee Voucher',
      ),
    ],
    rewards: [
      UserReward(
        id: 'reward-1',
        campaignId: 'campaign-1',
        milestoneId: 'milestone-1',
        rewardTitle: 'Coffee Voucher',
        status: UserRewardStatus.available,
      ),
    ],
  );

  const overviewNoRewards = RewardsOverview(
    campaignId: 'campaign-1',
    progress: RewardsProgress(
      lineId: 'line-1',
      collected: 3,
      total: 10,
      percentage: 30,
    ),
    milestones: [
      Milestone(
        id: 'milestone-1',
        campaignId: 'campaign-1',
        code: 'M5',
        name: '5 Stamps',
        requiredStampCount: 5,
        rewardTitle: 'Coffee Voucher',
      ),
    ],
    rewards: [],
  );

  setUp(() {
    repository = MockRewardsRepository();
    cubit = RewardsCubit(
      getRewardsOverviewUseCase: GetRewardsOverviewUseCase(repository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<RewardsCubit, RewardsState>(
    'emits loading then loaded when rewards exist',
    build: () {
      when(() => repository.getRewardsOverview())
          .thenAnswer((_) async => overviewWithRewards);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<RewardsState>().having(
        (state) => state.status,
        'status',
        RewardsStatus.loading,
      ),
      isA<RewardsState>()
          .having((state) => state.status, 'status', RewardsStatus.loaded)
          .having((state) => state.overview?.rewards.length, 'rewards', 1),
    ],
  );

  blocTest<RewardsCubit, RewardsState>(
    'emits noRewardsYet when milestones exist but rewards empty',
    build: () {
      when(() => repository.getRewardsOverview())
          .thenAnswer((_) async => overviewNoRewards);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.status, RewardsStatus.noRewardsYet);
      expect(cubit.state.overview?.rewards, isEmpty);
      expect(cubit.state.overview?.milestones, isNotEmpty);
    },
  );

  blocTest<RewardsCubit, RewardsState>(
    'emits failure when rewards/my fails',
    build: () {
      when(() => repository.getRewardsOverview()).thenThrow(
        const Failure(
          code: FailureCode.networkError,
          message: 'Network error',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.status, RewardsStatus.failure);
      expect(cubit.state.overview, isNull);
    },
  );

  blocTest<RewardsCubit, RewardsState>(
    'emits loaded with partial errors when rewards exist',
    build: () {
      when(() => repository.getRewardsOverview()).thenAnswer(
        (_) async => const RewardsOverview(
          rewards: [
            UserReward(
              id: 'reward-1',
              campaignId: 'campaign-1',
              milestoneId: 'milestone-1',
              rewardTitle: 'Coffee Voucher',
              status: UserRewardStatus.available,
            ),
          ],
          partialErrors: ['Milestones failed'],
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.status, RewardsStatus.loaded);
      expect(cubit.state.overview?.partialErrors, isNotEmpty);
    },
  );
}
