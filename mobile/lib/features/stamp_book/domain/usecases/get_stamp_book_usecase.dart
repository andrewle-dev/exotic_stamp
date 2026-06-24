import '../entities/stamp_book.dart';
import '../repositories/stamp_book_repository.dart';

class GetStampBookUseCase {
  const GetStampBookUseCase(this._repository);

  final StampBookRepository _repository;

  Future<StampBook> call({String? lineId}) {
    return _repository.getStampBook(lineId: lineId);
  }
}
