import '../../domain/entities/stamp_book.dart';
import '../../domain/entities/stamp_detail.dart';
import '../../domain/repositories/stamp_book_repository.dart';
import '../datasources/stamp_book_remote_datasource.dart';
import '../models/stamp_detail_model.dart';
import '../models/stamp_item_model.dart';

class StampBookRepositoryImpl implements StampBookRepository {
  StampBookRepositoryImpl({required StampBookRemoteDataSource remoteDataSource})
      : _remoteDataSource = remoteDataSource;

  final StampBookRemoteDataSource _remoteDataSource;

  @override
  Future<StampBook> getStampBook({String? lineId}) async {
    final response = await _remoteDataSource.getStampBook(lineId: lineId);
    return response.toEntity();
  }

  @override
  Future<StampDetail> getStampDetail({
    required String stationId,
    String? lineId,
  }) async {
    final stampBook = await _remoteDataSource.getStampBook(lineId: lineId);
    StampItemModel? station;
    for (final item in stampBook.stations) {
      if (item.stationId == stationId) {
        station = item;
        break;
      }
    }

    if (station == null) {
      return StampDetailModel(
        stationId: stationId,
        stationName: '',
        collected: false,
        availability: StampDetailAvailability.notFound,
      ).toEntity();
    }

    if (!station.collected) {
      return StampDetailModel.fromStampItem(
        item: station,
        lineId: stampBook.lineId,
        lineName: stampBook.lineName,
        campaignName: stampBook.campaignName,
      ).toEntity();
    }

    try {
      final myStamps = await _remoteDataSource.getMyStamps(
        lineId: stampBook.lineId,
      );
      var merged = station;
      for (final stamp in myStamps) {
        if (stamp.stationId == stationId) {
          merged = station.mergeCollectedMetadata(stamp);
          break;
        }
      }

      return StampDetailModel.fromStampItem(
        item: merged,
        lineId: stampBook.lineId,
        lineName: stampBook.lineName,
        campaignName: stampBook.campaignName,
        availability: merged.collectMethod == null && merged.stampId == null
            ? StampDetailAvailability.limited
            : StampDetailAvailability.full,
      ).toEntity();
    } catch (_) {
      return StampDetailModel.fromStampItem(
        item: station,
        lineId: stampBook.lineId,
        lineName: stampBook.lineName,
        campaignName: stampBook.campaignName,
        availability: StampDetailAvailability.limited,
      ).toEntity();
    }
  }
}
