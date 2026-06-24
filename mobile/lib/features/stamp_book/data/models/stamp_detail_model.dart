import '../../domain/entities/stamp_detail.dart';
import 'stamp_item_model.dart';

class StampDetailModel {
  StampDetailModel({
    required this.stationId,
    required this.stationName,
    required this.collected,
    this.lineId,
    this.lineName,
    this.campaignName,
    this.stampDesignUrl,
    this.collectedAt,
    this.stampId,
    this.collectMethod,
    this.availability = StampDetailAvailability.full,
  });

  final String stationId;
  final String stationName;
  final bool collected;
  final String? lineId;
  final String? lineName;
  final String? campaignName;
  final String? stampDesignUrl;
  final DateTime? collectedAt;
  final String? stampId;
  final String? collectMethod;
  final StampDetailAvailability availability;

  factory StampDetailModel.fromStampItem({
    required StampItemModel item,
    String? lineId,
    String? lineName,
    String? campaignName,
    StampDetailAvailability availability = StampDetailAvailability.full,
  }) {
    return StampDetailModel(
      stationId: item.stationId,
      stationName: item.stationName,
      collected: item.collected,
      lineId: lineId,
      lineName: lineName,
      campaignName: campaignName,
      stampDesignUrl: item.stampDesignUrl,
      collectedAt: item.collectedAt,
      stampId: item.stampId,
      collectMethod: item.collectMethod,
      availability: availability,
    );
  }

  StampDetail toEntity() {
    return StampDetail(
      stationId: stationId,
      stationName: stationName,
      collected: collected,
      lineId: lineId,
      lineName: lineName,
      campaignName: campaignName,
      stampDesignUrl: stampDesignUrl,
      collectedAt: collectedAt,
      stampId: stampId,
      collectMethod: collectMethod,
      availability: availability,
    );
  }
}
