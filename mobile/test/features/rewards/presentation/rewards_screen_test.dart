import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/milestone.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/rewards_overview.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_cubit.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_state.dart';
import 'package:metro_stamp_app/features/rewards/presentation/screens/rewards_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockRewardsCubit extends Mock implements RewardsCubit {}

void main() {
  late MockRewardsCubit cubit;

  const overviewNoRewards = RewardsOverview(
    campaignId: 'campaign-1',
    rankTitle: 'Explorer Rank',
    progress: RewardsProgress(
      lineId: 'line-1',
      collected: 3,
      total: 14,
      percentage: 21,
    ),
    milestones: [
      Milestone(
        id: 'milestone-1',
        campaignId: 'campaign-1',
        code: 'M3',
        name: 'Metro Cadet',
        requiredStampCount: 3,
        rewardTitle: 'Silver Digital Sticker Pack',
        claimStatus: MilestoneClaimStatus.claimed,
      ),
    ],
    rewards: [],
  );

  setUp(() {
    cubit = MockRewardsCubit();
    when(() => cubit.state).thenReturn(
      const RewardsState(
        status: RewardsStatus.noRewardsYet,
        overview: overviewNoRewards,
      ),
    );
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.refresh()).thenAnswer((_) async {});
  });

  testWidgets('shows milestone progress without fake reward cards',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: RewardsScreen(cubit: cubit),
      ),
    );
    await tester.pump();

    expect(find.text('Road to 14'), findsOneWidget);
    expect(find.text('Metro Cadet'), findsOneWidget);
    expect(find.text('No vouchers yet'), findsOneWidget);
    expect(find.text('Silver Digital Sticker Pack'), findsWidgets);
    expect(find.text('Redeem'), findsNothing);
    expect(find.text('Claim'), findsNothing);
  });
}
