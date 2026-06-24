import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/milestone.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/rewards_overview.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_cubit.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_state.dart';
import 'package:metro_stamp_app/features/rewards/presentation/screens/rewards_screen.dart';
import 'package:metro_stamp_app/features/rewards/presentation/widgets/milestone_timeline_item.dart';
import 'package:mocktail/mocktail.dart';

class MockRewardsCubit extends Mock implements RewardsCubit {}

void main() {
  late MockRewardsCubit cubit;

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
    partialErrors: ['Milestones sync delayed'],
  );

  setUp(() {
    cubit = MockRewardsCubit();
    when(() => cubit.state).thenReturn(
      const RewardsState(
        status: RewardsStatus.loaded,
        overview: overviewWithRewards,
      ),
    );
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.refresh()).thenAnswer((_) async {});
  });

  testWidgets('shows rewards and partial error banner when milestones fail',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: RewardsScreen(cubit: cubit),
      ),
    );
    await tester.pump();

    expect(find.text('Coffee Voucher'), findsWidgets);
    expect(find.text('Milestones sync delayed'), findsOneWidget);
    expect(find.text('Chưa có phần thưởng'), findsNothing);
  });

  testWidgets('milestone timeline does not fake progress when progress missing',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: MilestoneTimelineItem(
          milestone: overviewWithRewards.milestones.first,
          collectedStampCount: null,
          isFirst: true,
          isLast: true,
        ),
      ),
    );
    await tester.pump();

    expect(find.byIcon(Icons.check_rounded), findsNothing);
    expect(find.text('5'), findsOneWidget);
  });
}
