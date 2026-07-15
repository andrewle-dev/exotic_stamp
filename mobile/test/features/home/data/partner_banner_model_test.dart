import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/home/data/models/home_summary_model.dart';

void main() {
  test('PartnerBannerModel.fromJson parses promotional banner list items', () {
    final model = PartnerBannerModel.fromJson({
      'partnerId': 'p-1',
      'partnerName': 'Highland Coffee',
      'logoUrl': 'https://cdn.example/logo.png',
      'bannerImageUrl': 'https://cdn.example/banner.png',
      'contractStart': '2026-05-25',
      'contractEnd': '2027-06-25',
    });

    final entity = model.toEntity();

    expect(entity.partnerId, 'p-1');
    expect(entity.partnerName, 'Highland Coffee');
    expect(entity.logoUrl, 'https://cdn.example/logo.png');
    expect(entity.bannerImageUrl, 'https://cdn.example/banner.png');
    expect(entity.contractStart, '2026-05-25');
    expect(entity.contractEnd, '2027-06-25');
  });

  test('PartnerBannerModel tolerates missing optional fields', () {
    final model = PartnerBannerModel.fromJson({
      'partnerId': 'p-2',
      'partnerName': 'Phuc Long',
      'bannerImageUrl': 'https://cdn.example/b2.png',
    });

    expect(model.logoUrl, isNull);
    expect(model.contractStart, isNull);
    expect(model.toEntity().bannerImageUrl, 'https://cdn.example/b2.png');
  });
}
