import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/rewards/data/datasources/rewards_remote_datasource.dart';
import 'package:metro_stamp_app/features/rewards/data/models/milestone_model.dart';
import 'package:metro_stamp_app/features/rewards/data/models/user_reward_model.dart';
import 'package:metro_stamp_app/features/rewards/data/models/voucher_detail_model.dart';
import 'package:metro_stamp_app/features/rewards/data/repositories/rewards_repository_impl.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';
import 'package:mocktail/mocktail.dart';

class MockRewardsRemoteDataSource extends Mock
    implements RewardsRemoteDataSource {}

void main() {
  late MockRewardsRemoteDataSource remoteDataSource;
  late RewardsRepositoryImpl repository;

  setUp(() {
    remoteDataSource = MockRewardsRemoteDataSource();
    repository = RewardsRepositoryImpl(remoteDataSource: remoteDataSource);
  });

  test('maps user rewards response to entities', () async {
    when(() => remoteDataSource.getActiveCampaign()).thenAnswer(
      (_) async => {'id': 'campaign-1', 'name': 'Metro 2026'},
    );
    when(() => remoteDataSource.getLines()).thenAnswer(
      (_) async => [
        {'id': 'line-1', 'status': 'ACTIVE'},
      ],
    );
    when(
      () => remoteDataSource.getCollectionProgress(lineId: 'line-1'),
    ).thenAnswer(
      (_) async => {
        'lineId': 'line-1',
        'collected': 5,
        'total': 10,
        'percentage': 50,
      },
    );
    when(
      () => remoteDataSource.getMilestones(campaignId: 'campaign-1'),
    ).thenAnswer(
      (_) async => [
        MilestoneModel(
          id: 'milestone-1',
          campaignId: 'campaign-1',
          code: 'M5',
          name: '5 Stamps',
          requiredStampCount: 5,
          rewardTitle: 'Coffee Voucher',
        ),
      ],
    );
    when(() => remoteDataSource.getMyRewards()).thenAnswer(
      (_) async => [
        UserRewardModel(
          id: 'reward-1',
          campaignId: 'campaign-1',
          milestoneId: 'milestone-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.available,
          voucher: UserRewardVoucherModel(id: 'voucher-1', code: 'ABC-123'),
        ),
      ],
    );

    final rewards = await repository.getMyRewards();

    expect(rewards, hasLength(1));
    expect(rewards.first.id, 'reward-1');
    expect(rewards.first.status, UserRewardStatus.available);
    expect(rewards.first.voucher?.code, 'ABC-123');
  });

  test('maps milestones response to entities', () async {
    when(
      () => remoteDataSource.getMilestones(campaignId: 'campaign-1'),
    ).thenAnswer(
      (_) async => [
        MilestoneModel(
          id: 'milestone-1',
          campaignId: 'campaign-1',
          code: 'M5',
          name: '5 Stamps',
          requiredStampCount: 5,
          rewardTitle: 'Coffee Voucher',
        ),
        MilestoneModel(
          id: 'milestone-2',
          campaignId: 'campaign-1',
          code: 'M10',
          name: '10 Stamps',
          requiredStampCount: 10,
          rewardTitle: 'Pin Badge',
        ),
      ],
    );

    final milestones = await repository.getMilestones(campaignId: 'campaign-1');

    expect(milestones, hasLength(2));
    expect(milestones.first.requiredStampCount, 5);
    expect(milestones.last.rewardTitle, 'Pin Badge');
  });

  test('overview uses backend progress without inventing rewards', () async {
    when(() => remoteDataSource.getActiveCampaign()).thenAnswer(
      (_) async => {'id': 'campaign-1', 'name': 'Metro 2026'},
    );
    when(() => remoteDataSource.getLines()).thenAnswer(
      (_) async => [
        {'id': 'line-1', 'status': 'ACTIVE'},
      ],
    );
    when(
      () => remoteDataSource.getCollectionProgress(lineId: 'line-1'),
    ).thenAnswer(
      (_) async => {
        'lineId': 'line-1',
        'collected': 3,
        'total': 10,
        'percentage': 30,
      },
    );
    when(
      () => remoteDataSource.getMilestones(campaignId: 'campaign-1'),
    ).thenAnswer(
      (_) async => [
        MilestoneModel(
          id: 'milestone-1',
          campaignId: 'campaign-1',
          code: 'M5',
          name: '5 Stamps',
          requiredStampCount: 5,
          rewardTitle: 'Coffee Voucher',
        ),
      ],
    );
    when(() => remoteDataSource.getMyRewards()).thenAnswer((_) async => []);

    final overview = await repository.getRewardsOverview();

    expect(overview.rewards, isEmpty);
    expect(overview.progress?.collected, 3);
    expect(overview.milestones, hasLength(1));
    expect(overview.nextMilestone?.stampsRemaining, 2);
  });

  test('maps voucher detail response', () async {
    when(() => remoteDataSource.getVoucherDetail(id: 'reward-1')).thenAnswer(
      (_) async => VoucherDetailModel(
        id: 'reward-1',
        rewardTitle: 'Coffee Voucher',
        status: UserRewardStatus.available,
        voucherCode: 'ABC-123',
        expiresAt: DateTime(2026, 7, 20),
      ),
    );

    final detail = await repository.getVoucherDetail(id: 'reward-1');

    expect(detail.rewardTitle, 'Coffee Voucher');
    expect(detail.voucherCode, 'ABC-123');
    expect(detail.status, UserRewardStatus.available);
  });

  test('rethrows when rewards/my fails', () async {
    when(() => remoteDataSource.getActiveCampaign())
        .thenAnswer((_) async => null);
    when(() => remoteDataSource.getLines()).thenAnswer((_) async => []);
    when(() => remoteDataSource.getMyRewards()).thenThrow(
      const Failure(code: FailureCode.networkError, message: 'Network error'),
    );

    expect(
      () => repository.getRewardsOverview(),
      throwsA(
        isA<Failure>().having(
          (failure) => failure.code,
          'code',
          FailureCode.networkError,
        ),
      ),
    );
  });

  test('returns rewards with partial error when milestones fail', () async {
    when(() => remoteDataSource.getActiveCampaign()).thenAnswer(
      (_) async => {'id': 'campaign-1', 'name': 'Metro 2026'},
    );
    when(() => remoteDataSource.getLines()).thenAnswer((_) async => []);
    when(() => remoteDataSource.getMyRewards()).thenAnswer(
      (_) async => [
        UserRewardModel(
          id: 'reward-1',
          campaignId: 'campaign-1',
          milestoneId: 'milestone-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.available,
        ),
      ],
    );
    when(
      () => remoteDataSource.getMilestones(campaignId: 'campaign-1'),
    ).thenThrow(
      const Failure(code: FailureCode.unknown, message: 'Milestones failed'),
    );

    final overview = await repository.getRewardsOverview();

    expect(overview.rewards, hasLength(1));
    expect(overview.milestones, isEmpty);
    expect(overview.partialErrors, contains('Milestones failed'));
  });

  test('leaves progress null when collection progress fails', () async {
    when(() => remoteDataSource.getActiveCampaign()).thenAnswer(
      (_) async => {'id': 'campaign-1', 'name': 'Metro 2026'},
    );
    when(() => remoteDataSource.getLines()).thenAnswer(
      (_) async => [
        {'id': 'line-1', 'status': 'ACTIVE'},
      ],
    );
    when(
      () => remoteDataSource.getCollectionProgress(lineId: 'line-1'),
    ).thenThrow(
      const Failure(code: FailureCode.unknown, message: 'Progress failed'),
    );
    when(
      () => remoteDataSource.getMilestones(campaignId: 'campaign-1'),
    ).thenAnswer((_) async => []);
    when(() => remoteDataSource.getMyRewards()).thenAnswer((_) async => []);

    final overview = await repository.getRewardsOverview();

    expect(overview.progress, isNull);
    expect(overview.partialErrors, contains('Progress failed'));
  });
}
