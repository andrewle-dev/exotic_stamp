import '../../domain/entities/user_reward.dart';

class UserRewardVoucherModel {
  UserRewardVoucherModel({
    required this.id,
    this.code,
  });

  factory UserRewardVoucherModel.fromJson(Map<String, dynamic> json) {
    return UserRewardVoucherModel(
      id: json['id'] as String? ?? '',
      code: json['code'] as String?,
    );
  }

  final String id;
  final String? code;

  UserRewardVoucher toEntity() {
    return UserRewardVoucher(
      id: id,
      code: code,
    );
  }
}

class UserRewardModel {
  UserRewardModel({
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

  factory UserRewardModel.fromJson(Map<String, dynamic> json) {
    final voucherJson = json['voucher'];
    return UserRewardModel(
      id: json['id'] as String? ?? '',
      campaignId: json['campaignId'] as String? ?? '',
      milestoneId: json['milestoneId'] as String? ?? '',
      milestoneCode: json['milestoneCode'] as String?,
      milestoneName: json['milestoneName'] as String?,
      rewardType: json['rewardType'] as String?,
      rewardTitle: json['rewardTitle'] as String? ?? 'Reward',
      rewardDescription: json['rewardDescription'] as String?,
      rewardImageUrl: json['rewardImageUrl'] as String?,
      issuedAt: _parseDate(json['issuedAt']),
      expiresAt: _parseDate(json['expiresAt']),
      redeemedAt: _parseDate(json['redeemedAt']),
      status: mapUserRewardStatus(json['status'] as String?),
      voucher: voucherJson is Map<String, dynamic>
          ? UserRewardVoucherModel.fromJson(voucherJson)
          : null,
    );
  }

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
  final UserRewardVoucherModel? voucher;

  UserReward toEntity() {
    return UserReward(
      id: id,
      campaignId: campaignId,
      milestoneId: milestoneId,
      milestoneCode: milestoneCode,
      milestoneName: milestoneName,
      rewardType: rewardType,
      rewardTitle: rewardTitle,
      rewardDescription: rewardDescription,
      rewardImageUrl: rewardImageUrl,
      issuedAt: issuedAt,
      expiresAt: expiresAt,
      redeemedAt: redeemedAt,
      status: status,
      voucher: voucher?.toEntity(),
    );
  }

  static DateTime? _parseDate(Object? value) {
    if (value is! String || value.isEmpty) {
      return null;
    }
    return DateTime.tryParse(value);
  }
}

UserRewardStatus mapUserRewardStatus(String? status) {
  switch (status?.toUpperCase()) {
    case 'ISSUED':
      return UserRewardStatus.available;
    case 'REDEEMED':
      return UserRewardStatus.used;
    case 'EXPIRED':
      return UserRewardStatus.expired;
    case 'PENDING_STOCK':
      return UserRewardStatus.pending;
    default:
      return UserRewardStatus.unavailable;
  }
}
