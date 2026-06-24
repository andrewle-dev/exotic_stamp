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

    expect(find.text('Cột mốc'), findsOneWidget);
    expect(find.text('5 Stamps'), findsOneWidget);
    expect(find.text('Chưa có phần thưởng'), findsOneWidget);
    expect(find.text('Coffee Voucher'), findsWidgets);
    expect(find.text('Redeem'), findsNothing);
    expect(find.text('Claim'), findsNothing);
  });
}
