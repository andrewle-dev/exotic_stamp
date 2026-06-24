import '../entities/stamp_book.dart';
import '../entities/stamp_detail.dart';

abstract class StampBookRepository {
  Future<StampBook> getStampBook({String? lineId});

  Future<StampDetail> getStampDetail({
    required String stationId,
    String? lineId,
  });
}
