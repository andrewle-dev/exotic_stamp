import '../entities/stamp_detail.dart';
import '../repositories/stamp_book_repository.dart';

class GetStampDetailUseCase {
  const GetStampDetailUseCase(this._repository);

  final StampBookRepository _repository;

  Future<StampDetail> call({
    required String stationId,
    String? lineId,
  }) {
    return _repository.getStampDetail(
      stationId: stationId,
      lineId: lineId,
    );
  }
}
