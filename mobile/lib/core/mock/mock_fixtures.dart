import '../../features/home/domain/entities/home_summary.dart';
import '../../features/profile/domain/entities/profile.dart';
import '../../features/rewards/domain/entities/milestone.dart';
import '../../features/rewards/domain/entities/rewards_overview.dart';
import '../../features/rewards/domain/entities/user_reward.dart';
import '../../features/rewards/domain/entities/voucher_detail.dart';
import '../../features/stamp_book/domain/entities/stamp_book.dart';
import '../../features/stamp_book/domain/entities/stamp_detail.dart';
import '../../features/stamp_book/domain/entities/stamp_item.dart';
import '../../features/stations/domain/entities/line.dart';
import '../../features/stations/domain/entities/station.dart';
import '../../features/stations/domain/entities/station_collected_status.dart';
import '../../features/stations/domain/entities/station_detail.dart';
import '../../features/stations/domain/entities/station_extras.dart';

/// Static seed data for mock repositories. Not used in production mode.
abstract final class MockFixtures {
  static const lineId = 'line-1';
  static const lineName = 'Tuyến 1';
  static const campaignId = 'campaign-mock-1';
  static const campaignName = 'Metro Stamp 2026';

  static const initialCollectedStationIds = {
    'station-ben-thanh',
    'station-sai-gon',
    'station-ba-son',
    'station-van-thanh',
    'station-phu-dong',
  };

  static List<Line> lines() => const [
        Line(
          id: lineId,
          name: 'Line 1',
          displayName: 'Line 1',
          colorHex: '#01599D',
          status: 'ACTIVE',
          totalStations: 14,
        ),
        Line(
          id: 'line-2',
          name: 'Line 2',
          displayName: 'Line 2',
          colorHex: '#E83B28',
          status: 'ACTIVE',
          totalStations: 11,
        ),
        Line(
          id: 'line-5',
          name: 'Line 5',
          displayName: 'Line 5',
          colorHex: '#01599D',
          status: 'ACTIVE',
          totalStations: 8,
        ),
      ];

  static List<({String id, String name, String code, int sequence})>
      stationCatalog() {
    return [
      (id: 'station-ben-thanh', name: 'Ga Bến Thành', code: 'L1-01', sequence: 1),
      (id: 'station-sai-gon', name: 'Ga Sài Gòn', code: 'L1-02', sequence: 2),
      (id: 'station-ba-son', name: 'Ga Ba Son', code: 'L1-03', sequence: 3),
      (id: 'station-van-thanh', name: 'Ga Văn Thánh', code: 'L1-04', sequence: 4),
      (id: 'station-phu-dong', name: 'Ga Phú Đông', code: 'L1-05', sequence: 5),
      (id: 'station-thao-dien', name: 'Ga Thảo Điền', code: 'L1-06', sequence: 6),
      (id: 'station-an-phu', name: 'Ga An Phú', code: 'L1-07', sequence: 7),
      (id: 'station-binh-thanh', name: 'Ga Bình Thạnh', code: 'L1-08', sequence: 8),
      (id: 'station-thu-duc', name: 'Ga Thủ Đức', code: 'L1-09', sequence: 9),
      (id: 'station-suoi-tien', name: 'Ga Suối Tiên', code: 'L1-10', sequence: 10),
      (id: 'station-bien-hoa', name: 'Ga Biên Hòa', code: 'L1-11', sequence: 11),
      (id: 'station-di-an', name: 'Ga Dĩ An', code: 'L1-12', sequence: 12),
      (id: 'station-tan-bien', name: 'Ga Tân Biên', code: 'L1-13', sequence: 13),
      (id: 'station-depot', name: 'Ga Depot', code: 'L1-14', sequence: 14),
    ];
  }

  static Station stationFromCatalog(
    ({String id, String name, String code, int sequence}) entry,
    Set<String> collectedIds,
  ) {
    final collected = collectedIds.contains(entry.id);
    return Station(
      id: entry.id,
      lineId: lineId,
      code: entry.code,
      name: entry.name,
      displayName: entry.name,
      lineName: lineName,
      latitude: 10.77 + entry.sequence * 0.01,
      longitude: 106.69 + entry.sequence * 0.005,
      status: 'ACTIVE',
      collectedStatus: collected
          ? StationCollectedStatus.collected
          : StationCollectedStatus.uncollected,
    );
  }

  static StationDetail stationDetailFromCatalog(
    ({String id, String name, String code, int sequence}) entry,
    Set<String> collectedIds,
  ) {
    final collected = collectedIds.contains(entry.id);
    final isBenThanh = entry.id == 'station-ben-thanh';
    return StationDetail(
      id: entry.id,
      lineId: lineId,
      lineName: lineName,
      lineHubLabel: isBenThanh ? 'Central Hub' : null,
      name: entry.name,
      displayName: entry.name,
      description: isBenThanh
          ? 'Ga Bến Thành là trung tâm kết nối của hệ thống metro, hoàn thành năm 2023 với kiến trúc hiện đại kết hợp nét truyền thống.'
          : 'Ga ${entry.name} thuộc hệ thống metro — mock UI data cho phát triển giao diện.',
      address: isBenThanh
          ? 'District 1, Ho Chi Minh City'
          : 'Quận ${entry.sequence}, TP. Hồ Chí Minh',
      districtLabel: isBenThanh ? 'District 1, Ho Chi Minh City' : null,
      latitude: 10.77 + entry.sequence * 0.01,
      longitude: 106.69 + entry.sequence * 0.005,
      zoneRadiusMeters: 120,
      status: 'ACTIVE',
      collectedStatus: collected
          ? StationCollectedStatus.collected
          : StationCollectedStatus.uncollected,
      socialProof: isBenThanh
          ? const StationSocialProof(
              message: '1,248 collectors stamped here this week',
              collectorCount: 1248,
              overflowLabel: '+9k',
            )
          : null,
      nearbyPlaces: isBenThanh ? nearbyPlacesForBenThanh() : const [],
      openingHoursLabel: isBenThanh ? 'Opens 05:00 AM' : null,
      accessibilityLabel: isBenThanh ? '4 Accessible Levels' : null,
      virtualTourUrl: isBenThanh ? 'https://example.com/virtual-tour' : null,
    );
  }

  static List<NearbyPlace> nearbyPlacesForBenThanh() => const [
        NearbyPlace(
          id: 'place-market',
          name: 'Bến Thành Market',
          category: 'LANDMARK',
          distanceMeters: 120,
        ),
        NearbyPlace(
          id: 'place-park',
          name: 'September 23rd Park',
          category: 'PARK',
          distanceMeters: 350,
        ),
        NearbyPlace(
          id: 'place-museum',
          name: 'Fine Arts Museum',
          category: 'MUSEUM',
          distanceMeters: 480,
        ),
      ];

  static StampItem stampItemFromCatalog(
    ({String id, String name, String code, int sequence}) entry,
    Set<String> collectedIds,
  ) {
    final collected = collectedIds.contains(entry.id);
    return StampItem(
      stationId: entry.id,
      stationName: entry.name,
      sequence: entry.sequence,
      collected: collected,
      stampDesignName: 'Stamp ${entry.name}',
      stampDesignDescription:
          'Collectible stamp artwork for ${entry.name} station.',
      rarity: 'COMMON',
      collectedAt: collected
          ? DateTime.now().subtract(Duration(days: 15 - entry.sequence))
          : null,
      stampId: collected ? 'stamp-${entry.id}' : null,
      collectMethod: collected ? 'NFC' : null,
    );
  }

  static StampBook stampBook(Set<String> collectedIds, {String? filterLineId}) {
    final catalog = stationCatalog();
    final collectedCount = catalog.where((s) => collectedIds.contains(s.id)).length;
    final total = catalog.length;
    final percentage =
        total == 0 ? 0 : ((collectedCount / total) * 100).round();
    final isAllLines = filterLineId == null;

    return StampBook(
      lineId: filterLineId ?? lineId,
      lineName: isAllLines ? 'System Wide' : lineName,
      campaignId: campaignId,
      campaignName: campaignName,
      progress: StampBookProgress(
        lineId: filterLineId ?? lineId,
        collected: collectedCount,
        total: total,
        percentage: percentage,
      ),
      stations: catalog
          .map((entry) => stampItemFromCatalog(entry, collectedIds))
          .toList(),
    );
  }

  static HomeSummary homeSummary(Set<String> collectedIds) {
    final book = stampBook(collectedIds);
    final progress = book.progress!;
    final recent = book.stations
        .where((s) => s.collected)
        .take(3)
        .map(
          (s) => RecentStamp(
            stationId: s.stationId,
            stationName: s.stationName,
            collectedAt: s.collectedAt ?? DateTime.now(),
            collectMethod: s.collectMethod,
          ),
        )
        .toList();
    final remaining = (7 - progress.collected).clamp(0, 99);

    return HomeSummary(
      displayName: 'Người dùng mock',
      lineId: lineId,
      lineName: lineName,
      progress: CollectionProgress(
        lineId: progress.lineId,
        collected: progress.collected,
        total: progress.total,
        percentage: progress.percentage,
      ),
      recentStamps: recent,
      nextReward: NextReward(
        milestoneId: 'milestone-7',
        requiredStampCount: 7,
        rewardTitle: 'Free Coffee Voucher',
        stampsRemaining: remaining,
      ),
      activeBanner: const ActiveBanner(
        campaignId: campaignId,
        campaignName: 'Morning Brew Reward',
        promoLabel: 'LIMITED OFFER',
        subtitle:
            'Quét 3 ga tuần này để nhận Latte miễn phí tại ga trung tâm.',
      ),
      promotionalBanners: const [
        PartnerBanner(
          partnerId: 'partner-highland',
          partnerName: 'Highland Coffee',
          logoUrl: '/uploads/public/partners/demo-coffee.png',
          bannerImageUrl: '/uploads/public/partners/demo-coffee.png',
        ),
        PartnerBanner(
          partnerId: 'partner-phuclong',
          partnerName: 'Phuc Long',
          bannerImageUrl: '/uploads/public/partners/demo-coffee.png',
        ),
      ],
      milestones: [
        HomeMilestonePreview(
          id: 'milestone-3',
          requiredStampCount: 3,
          label: '3 Stamps',
          rewardTitle: 'Badge đồng',
          achieved: progress.collected >= 3,
        ),
        HomeMilestonePreview(
          id: 'milestone-7',
          requiredStampCount: 7,
          label: '7 Stamps',
          rewardTitle: 'Free Coffee Voucher',
          achieved: progress.collected >= 7,
        ),
        HomeMilestonePreview(
          id: 'milestone-14',
          requiredStampCount: 14,
          label: '14 Stamps',
          rewardTitle: 'Metro Explorer Badge',
          achieved: progress.collected >= 14,
        ),
      ],
      rankTitle: 'Global Explorer',
      rankSubtitle:
          'Còn ${(14 - progress.collected).clamp(0, 99)} ga nữa để mở khóa Metro Explorer Badge',
      socialProof: const HomeSocialProof(
        message: '1.240 hành khách đã quét ga trong giờ qua!',
        highlightCount: 1240,
      ),
    );
  }

  static RewardsOverview rewardsOverview(Set<String> collectedIds) {
    final progress = stampBook(collectedIds).progress!;
    final collected = progress.collected;
    final milestonesWithStatus = milestones().map((milestone) {
      return Milestone(
        id: milestone.id,
        campaignId: milestone.campaignId,
        code: milestone.code,
        name: milestone.name,
        requiredStampCount: milestone.requiredStampCount,
        rewardTitle: milestone.rewardTitle,
        description: milestone.description,
        rewardType: milestone.rewardType,
        rewardDescription: milestone.rewardDescription,
        rewardImageUrl: milestone.rewardImageUrl,
        claimStatus: _milestoneClaimStatus(
          requiredCount: milestone.requiredStampCount,
          collected: collected,
          milestoneId: milestone.id,
        ),
      );
    }).toList();

    return RewardsOverview(
      campaignId: campaignId,
      campaignName: campaignName,
      rankTitle: 'Explorer Rank',
      progress: RewardsProgress(
        lineId: progress.lineId,
        collected: progress.collected,
        total: progress.total,
        percentage: progress.percentage,
      ),
      milestones: milestonesWithStatus,
      rewards: userRewards(),
      nextMilestone: NextMilestoneHint(
        milestoneId: 'milestone-7',
        requiredStampCount: 7,
        rewardTitle: 'Free Coffee',
        stampsRemaining: (7 - progress.collected).clamp(0, 99),
      ),
    );
  }

  static MilestoneClaimStatus _milestoneClaimStatus({
    required int requiredCount,
    required int collected,
    required String milestoneId,
  }) {
    if (collected >= requiredCount) {
      if (requiredCount <= 3) {
        return MilestoneClaimStatus.claimed;
      }
      if (requiredCount == 7) {
        return MilestoneClaimStatus.claimable;
      }
      return MilestoneClaimStatus.claimed;
    }
    if (collected >= requiredCount - 2 && requiredCount > 3) {
      return MilestoneClaimStatus.inProgress;
    }
    return MilestoneClaimStatus.locked;
  }

  static List<Milestone> milestones() => const [
        Milestone(
          id: 'milestone-3',
          campaignId: campaignId,
          code: 'M3',
          name: 'Metro Cadet',
          requiredStampCount: 3,
          rewardTitle: 'Silver Digital Sticker Pack',
        ),
        Milestone(
          id: 'milestone-7',
          campaignId: campaignId,
          code: 'M7',
          name: 'Frequent Rider',
          requiredStampCount: 7,
          rewardTitle: 'Free Espresso at Partner Cafe',
        ),
        Milestone(
          id: 'milestone-14',
          campaignId: campaignId,
          code: 'M14',
          name: 'Metro Legend',
          requiredStampCount: 14,
          rewardTitle: 'Exclusive Gold Pin & 50% Brand Voucher',
        ),
      ];

  static List<UserReward> userRewards() => [
        UserReward(
          id: 'reward-voucher-1',
          campaignId: campaignId,
          milestoneId: 'milestone-7',
          partnerName: 'Metro BrewStop',
          offerTitle: 'Buy 1 Get 1 Coffee',
          rewardTitle: 'Buy 1 Get 1 Coffee',
          status: UserRewardStatus.available,
          issuedAt: DateTime(2026, 2, 1),
          expiresAt: DateTime(2026, 12, 24),
          voucher: const UserRewardVoucher(id: 'voucher-1', code: 'BREW-BOGO'),
        ),
        UserReward(
          id: 'reward-voucher-2',
          campaignId: campaignId,
          milestoneId: 'milestone-7',
          partnerName: 'Station Snacks',
          offerTitle: '20% Off All Pastries',
          rewardTitle: '20% Off All Pastries',
          status: UserRewardStatus.available,
          issuedAt: DateTime(2026, 2, 5),
          expiresAt: DateTime(2026, 12, 24),
          voucher: const UserRewardVoucher(id: 'voucher-2', code: 'SNACK-20'),
        ),
        UserReward(
          id: 'reward-voucher-3',
          campaignId: campaignId,
          milestoneId: 'milestone-14',
          partnerName: 'TechHub Metro',
          offerTitle: r'$10 Gift Voucher',
          rewardTitle: r'$10 Gift Voucher',
          status: UserRewardStatus.available,
          issuedAt: DateTime(2026, 3, 1),
          expiresAt: DateTime(2026, 12, 24),
          voucher: const UserRewardVoucher(id: 'voucher-3', code: 'TECH-10'),
        ),
        UserReward(
          id: 'reward-1',
          campaignId: campaignId,
          milestoneId: 'milestone-3',
          milestoneName: 'Metro Cadet',
          rewardTitle: 'Silver Digital Sticker Pack',
          status: UserRewardStatus.used,
          issuedAt: DateTime(2026, 1, 10),
          redeemedAt: DateTime(2026, 1, 12),
        ),
      ];

  static VoucherDetail voucherDetail(String id) {
    return VoucherDetail(
      id: id,
      partnerName: 'Metro BrewStop',
      offerTitle: 'MUA 1 TẶNG 1 CAFE TƯƠI',
      rewardTitle: 'Buy 1 Get 1 Fresh Coffee',
      milestoneName: '7 Stamps',
      unlockCondition: '7 Stamps',
      rewardDescription: 'Một ly cà phê miễn phí tại Metro BrewStop.',
      status: UserRewardStatus.available,
      issuedAt: DateTime(2026, 2, 1),
      expiresAt: DateTime(2026, 12, 24),
      voucherCode: 'METRO-COFFEE-8421',
      terms: const [
        'Áp dụng cho tất cả các loại cà phê tươi.',
        'Giới hạn 1 voucher mỗi ngày.',
        'Không quy đổi thành tiền mặt.',
        'Xuất trình mã trước khi thanh toán.',
      ],
      relatedVouchers: const [
        RelatedVoucherSummary(
          id: 'reward-voucher-used',
          title: 'Bánh Croissant',
          subtitle: '20/06/2026',
          status: UserRewardStatus.used,
        ),
        RelatedVoucherSummary(
          id: 'reward-voucher-expired',
          title: 'Giảm 20% Combo',
          subtitle: '15/05/2026',
          status: UserRewardStatus.expired,
        ),
      ],
    );
  }

  static Profile profile(Set<String> collectedIds) {
    final collected = collectedIds.length;
    return Profile(
      id: 'user-mock-1',
      email: 'mock@exoticstamp.dev',
      username: 'mock_user',
      firstname: 'Alex',
      lastname: 'Chen',
      subtitle: 'Expert Commuter',
      createdAt: DateTime(2023, 6, 1),
      avatarUrl: null,
      stats: ProfileStats(
        collectedStampsCount: collected == 0 ? 42 : collected,
        linesCount: 3,
        rankPosition: 152,
        memoriesCount: 2,
        level: 12,
      ),
      invite: const ProfileInvite(
        referralCode: 'METRO50',
        description:
            'Get 50 bonus points when they scan their first stamp!',
      ),
      memories: const [
        ProfileMemory(
          id: 'memory-1',
          title: 'Central Terminal',
          capturedAtLabel: 'Today, 08:45',
        ),
        ProfileMemory(
          id: 'memory-2',
          title: 'Avenue Square',
          capturedAtLabel: '2 days ago',
        ),
      ],
      achievements: const [
        ProfileAchievement(
          id: 'ach-1',
          title: 'First Scan',
          earnedAtLabel: 'Jun 15, 2023',
        ),
        ProfileAchievement(
          id: 'ach-2',
          title: 'Early Bird',
          earnedAtLabel: 'Jul 02, 2023',
        ),
        ProfileAchievement(
          id: 'ach-3',
          title: 'Line 1 Hero',
          earnedAtLabel: 'Aug 19, 2023',
        ),
        ProfileAchievement(
          id: 'ach-4',
          title: 'Globetrotter',
          locked: true,
        ),
      ],
      appVersionLabel: 'METRO STAMP V0.1.0',
    );
  }

  static StampDetail stampDetail(
    String stationId,
    Set<String> collectedIds,
  ) {
    final entry = stationCatalog().firstWhere(
      (s) => s.id == stationId,
      orElse: () => stationCatalog().first,
    );
    final collected = collectedIds.contains(stationId);
    if (!collected) {
      return StampDetail(
        stationId: stationId,
        stationName: entry.name,
        collected: false,
        lineId: lineId,
        lineName: lineName,
        campaignName: campaignName,
        stampDesignName: 'Stamp ${entry.name}',
        stampDesignDescription:
            'Collectible stamp artwork for ${entry.name} station.',
        rarity: 'COMMON',
        availability: StampDetailAvailability.full,
      );
    }
    return StampDetail(
      stationId: stationId,
      stationName: entry.name,
      collected: true,
      lineId: lineId,
      lineName: lineName,
      campaignName: campaignName,
      stampDesignName: 'Stamp ${entry.name}',
      stampDesignDescription:
          'Collectible stamp artwork for ${entry.name} station.',
      rarity: 'COMMON',
      stampId: 'stamp-$stationId',
      collectMethod: 'NFC',
      serialNumber: 'MS-${entry.sequence.toString().padLeft(2, '0')}-BT-2026',
      stationStory:
          'Trái tim của hệ thống Metro TP.HCM. Ga ${entry.name} là điểm '
          'kết nối trung tâm với các tuyến metro, mang ý nghĩa văn hóa và '
          'kiến trúc hiện đại.',
      collectedAt: DateTime(2026, 6, 22, 8, 45),
      collectionProgress: StampCollectionProgress(
        collectionName: '$lineName Collection',
        collected: collectedIds.length.clamp(0, 14),
        total: 14,
        nextRewardHint:
            'Cần thêm ${(14 - collectedIds.length).clamp(0, 14)} stamps nữa '
            'để nhận Voucher đặc biệt!',
      ),
      availability: StampDetailAvailability.full,
    );
  }
}
