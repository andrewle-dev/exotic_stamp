import 'package:equatable/equatable.dart';

/// Payload for [RewardUnlockedShareScreen] after milestone unlock.
class RewardUnlockedSharePayload extends Equatable {
  const RewardUnlockedSharePayload({
    required this.rewardId,
    required this.rewardTitle,
    this.partnerName,
    this.offerTitle,
    this.milestoneName,
    this.unlockCondition,
    this.rewardImageUrl,
    this.voucherCode,
    this.pendingFulfillment = false,
  });

  final String rewardId;
  final String rewardTitle;
  final String? partnerName;
  final String? offerTitle;
  final String? milestoneName;
  final String? unlockCondition;
  final String? rewardImageUrl;
  final String? voucherCode;
  final bool pendingFulfillment;

  String get displayTitle => offerTitle ?? rewardTitle;

  @override
  List<Object?> get props => [
        rewardId,
        rewardTitle,
        partnerName,
        offerTitle,
        milestoneName,
        unlockCondition,
        rewardImageUrl,
        voucherCode,
        pendingFulfillment,
      ];
}
