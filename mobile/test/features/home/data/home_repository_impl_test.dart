import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/home/data/datasources/home_remote_datasource.dart';
import 'package:metro_stamp_app/features/home/data/models/home_summary_model.dart';
import 'package:metro_stamp_app/features/home/data/repositories/home_repository_impl.dart';
import 'package:mocktail/mocktail.dart';

class MockHomeRemoteDataSource extends Mock implements HomeRemoteDataSource {}

void main() {
  late MockHomeRemoteDataSource remoteDataSource;
  late HomeRepositoryImpl repository;

  setUp(() {
    remoteDataSource = MockHomeRemoteDataSource();
    repository = HomeRepositoryImpl(remoteDataSource: remoteDataSource);
  });

  test('maps composed API responses into HomeSummary', () async {
    when(() => remoteDataSource.getMe()).thenAnswer(
      (_) async => UserProfileModel(firstname: 'An', lastname: 'Nguyen'),
    );
    when(() => remoteDataSource.getLines()).thenAnswer(
      (_) async => [
        MetroLineModel(
          id: 'line-1',
          name: 'Line 1',
          displayName: 'Metro Line 1',
          status: 'ACTIVE',
        ),
      ],
    );
    when(() => remoteDataSource.getProgress(lineId: 'line-1')).thenAnswer(
      (_) async => CollectionProgressModel(
        lineId: 'line-1',
        collected: 2,
        total: 5,
        percentage: 40,
      ),
    );
    when(() => remoteDataSource.getRecentStamps(lineId: 'line-1')).thenAnswer(
      (_) async => [
        RecentStampModel(
          stationId: 's1',
          stationName: 'Ben Thanh',
          collectedAt: DateTime(2026, 6, 20),
        ),
      ],
    );
    when(() => remoteDataSource.getActiveCampaign()).thenAnswer(
      (_) async => ActiveBannerModel(
        campaignId: 'camp-1',
        campaignName: 'Metro 2026',
      ),
    );
    when(() => remoteDataSource.getPromotionalBanners()).thenAnswer(
      (_) async => [
        PartnerBannerModel(
          partnerId: 'p1',
          partnerName: 'Highland Coffee',
          bannerImageUrl: 'https://cdn.example/banner.png',
        ),
      ],
    );
    when(
      () => remoteDataSource.getMilestones(campaignId: 'camp-1'),
    ).thenAnswer(
      (_) async => [
        MilestoneModel(
          id: 'm1',
          requiredStampCount: 5,
          rewardTitle: 'Coffee Voucher',
        ),
      ],
    );

    final summary = await repository.getHomeSummary();

    expect(summary.displayName, 'An Nguyen');
    expect(summary.lineId, 'line-1');
    expect(summary.progress?.collected, 2);
    expect(summary.recentStamps, hasLength(1));
    expect(summary.nextReward?.rewardTitle, 'Coffee Voucher');
    expect(summary.nextReward?.stampsRemaining, 3);
    expect(summary.milestones, hasLength(1));
    expect(summary.milestones.first.achieved, isFalse);
    expect(summary.activeBanner?.campaignName, 'Metro 2026');
    expect(summary.promotionalBanners, hasLength(1));
    expect(summary.promotionalBanners.first.partnerName, 'Highland Coffee');
  });

  test('keeps successful sections when other home APIs partially fail',
      () async {
    when(() => remoteDataSource.getMe()).thenAnswer(
      (_) async => UserProfileModel(firstname: 'An', lastname: 'Nguyen'),
    );
    when(() => remoteDataSource.getLines()).thenAnswer(
      (_) async => [
        MetroLineModel(
          id: 'line-1',
          name: 'Line 1',
          status: 'ACTIVE',
        ),
      ],
    );
    when(() => remoteDataSource.getProgress(lineId: 'line-1')).thenAnswer(
      (_) async => CollectionProgressModel(
        lineId: 'line-1',
        collected: 1,
        total: 4,
        percentage: 25,
      ),
    );
    when(() => remoteDataSource.getRecentStamps(lineId: 'line-1')).thenThrow(
      const Failure(
        code: FailureCode.networkError,
        message: 'Recent stamps unavailable',
      ),
    );
    when(() => remoteDataSource.getActiveCampaign()).thenThrow(
      const Failure(
        code: FailureCode.networkError,
        message: 'Campaign unavailable',
      ),
    );
    when(() => remoteDataSource.getPromotionalBanners()).thenThrow(
      const Failure(
        code: FailureCode.networkError,
        message: 'Partner banners unavailable',
      ),
    );

    final summary = await repository.getHomeSummary();

    expect(summary.displayName, 'An Nguyen');
    expect(summary.progress?.collected, 1);
    expect(summary.recentStamps, isEmpty);
    expect(summary.activeBanner, isNull);
    expect(summary.promotionalBanners, isEmpty);
    expect(summary.nextReward, isNull);
    expect(summary.partialErrors, hasLength(3));
    expect(summary.partialErrors, contains('Recent stamps unavailable'));
    expect(summary.partialErrors, contains('Partner banners unavailable'));
  });
}
