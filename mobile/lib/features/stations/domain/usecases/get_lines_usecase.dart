import '../entities/line.dart';
import '../repositories/stations_repository.dart';

class GetLinesUseCase {
  const GetLinesUseCase(this._repository);

  final StationsRepository _repository;

  Future<List<Line>> call() => _repository.getLines();
}
