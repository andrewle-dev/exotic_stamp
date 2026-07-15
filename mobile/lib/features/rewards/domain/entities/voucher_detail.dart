import 'package:equatable/equatable.dart';

import 'user_reward.dart';

class RelatedVoucherSummary extends Equatable {
  const RelatedVoucherSummary({
    required this.id,
    required this.title,
    required this.status,
    this.subtitle,
  });

  final String id;
  final String title;
  final String? subtitle;
  final UserRewardStatus status;

  @override
  List<Object?> get props => [id, title, subtitle, status];
}

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
    this.offerTitle,
    this.unlockCondition,
    this.terms = const [],
    this.relatedVouchers = const [],
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
  final String? offerTitle;
  final String? unlockCondition;
  final List<String> terms;
  final List<RelatedVoucherSummary> relatedVouchers;

  String get displayOfferTitle => offerTitle ?? rewardTitle;

  bool get showVoucherCode =>
      status == UserRewardStatus.available && voucherCode != null;

  bool get canRedeem => status == UserRewardStatus.available;

  bool get showRedeemCta => canRedeem;

  VoucherDetail copyWithStatus(UserRewardStatus status) {
    return VoucherDetail(
      id: id,
      rewardTitle: rewardTitle,
      milestoneName: milestoneName,
      rewardDescription: rewardDescription,
      rewardImageUrl: rewardImageUrl,
      issuedAt: issuedAt,
      expiresAt: expiresAt,
      redeemedAt: status == UserRewardStatus.used
          ? (redeemedAt ?? DateTime.now())
          : redeemedAt,
      status: status,
      voucherCode: status == UserRewardStatus.available ? voucherCode : null,
      partnerName: partnerName,
      offerTitle: offerTitle,
      unlockCondition: unlockCondition,
      terms: terms,
      relatedVouchers: relatedVouchers,
    );
  }

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
        offerTitle,
        unlockCondition,
        terms,
        relatedVouchers,
      ];
}
