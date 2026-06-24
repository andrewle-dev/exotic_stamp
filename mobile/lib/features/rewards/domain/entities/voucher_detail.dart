import 'package:equatable/equatable.dart';

import 'user_reward.dart';

/// Full reward/voucher detail for [VoucherDetailScreen].
class VoucherDetail extends Equatable {
  const VoucherDetail({
    required this.id,
    required this.rewardTitle,
    required this.status,
    this.milestoneName,
    this.rewardDescription,
    this.rewardImageUrl,
    this.issuedAt,
    this.expiresAt,
    this.redeemedAt,
    this.voucherCode,
    this.partnerName,
  });

  final String id;
  final String rewardTitle;
  final String? milestoneName;
  final String? rewardDescription;
  final String? rewardImageUrl;
  final DateTime? issuedAt;
  final DateTime? expiresAt;
  final DateTime? redeemedAt;
  final UserRewardStatus status;
  final String? voucherCode;
  final String? partnerName;

  bool get showVoucherCode =>
      status == UserRewardStatus.available && voucherCode != null;

  bool get showPresentAtCounterCta => status == UserRewardStatus.available;

  @override
  List<Object?> get props => [
        id,
        rewardTitle,
        milestoneName,
        rewardDescription,
        rewardImageUrl,
        issuedAt,
        expiresAt,
        redeemedAt,
        status,
        voucherCode,
        partnerName,
      ];
}
