import '../../domain/entities/user_reward.dart';
import '../../domain/entities/voucher_detail.dart';
import 'user_reward_model.dart';

class VoucherDetailModel {
  VoucherDetailModel({
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

  factory VoucherDetailModel.fromJson(Map<String, dynamic> json) {
    final voucherJson = json['voucher'];
    String? code;
    if (voucherJson is Map<String, dynamic>) {
      code = voucherJson['code'] as String?;
    }

    return VoucherDetailModel(
      id: json['id'] as String? ?? '',
      rewardTitle: json['rewardTitle'] as String? ?? 'Reward',
      milestoneName: json['milestoneName'] as String?,
      rewardDescription: json['rewardDescription'] as String?,
      rewardImageUrl: json['rewardImageUrl'] as String?,
      issuedAt: _parseDate(json['issuedAt']),
      expiresAt: _parseDate(json['expiresAt']),
      redeemedAt: _parseDate(json['redeemedAt']),
      status: mapUserRewardStatus(json['status'] as String?),
      voucherCode: code,
      partnerName: json['partnerName'] as String?,
    );
  }

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

  VoucherDetail toEntity() {
    return VoucherDetail(
      id: id,
      rewardTitle: rewardTitle,
      milestoneName: milestoneName,
      rewardDescription: rewardDescription,
      rewardImageUrl: rewardImageUrl,
      issuedAt: issuedAt,
      expiresAt: expiresAt,
      redeemedAt: redeemedAt,
      status: status,
      voucherCode: voucherCode,
      partnerName: partnerName,
    );
  }

  static DateTime? _parseDate(Object? value) {
    if (value is! String || value.isEmpty) {
      return null;
    }
    return DateTime.tryParse(value);
  }
}
