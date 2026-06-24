import 'package:equatable/equatable.dart';

enum UserRewardStatus {
  available,
  used,
  expired,
  pending,
  unavailable,
}

class UserRewardVoucher extends Equatable {
  const UserRewardVoucher({
    required this.id,
    this.code,
  });

  final String id;
  final String? code;

  @override
  List<Object?> get props => [id, code];
}

class UserReward extends Equatable {
  const UserReward({
    required this.id,
    required this.campaignId,
    required this.milestoneId,
    required this.rewardTitle,
    required this.status,
    this.milestoneCode,
    this.milestoneName,
    this.rewardType,
    this.rewardDescription,
    this.rewardImageUrl,
    this.issuedAt,
    this.expiresAt,
    this.redeemedAt,
    this.voucher,
  });

  final String id;
  final String campaignId;
  final String milestoneId;
  final String? milestoneCode;
  final String? milestoneName;
  final String? rewardType;
  final String rewardTitle;
  final String? rewardDescription;
  final String? rewardImageUrl;
  final DateTime? issuedAt;
  final DateTime? expiresAt;
  final DateTime? redeemedAt;
  final UserRewardStatus status;
  final UserRewardVoucher? voucher;

  bool get isAvailable => status == UserRewardStatus.available;

  @override
  List<Object?> get props => [
        id,
        campaignId,
        milestoneId,
        milestoneCode,
        milestoneName,
        rewardType,
        rewardTitle,
        rewardDescription,
        rewardImageUrl,
        issuedAt,
        expiresAt,
        redeemedAt,
        status,
        voucher,
      ];
}
