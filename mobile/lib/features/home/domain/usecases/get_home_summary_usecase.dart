import '../entities/home_summary.dart';
import '../repositories/home_repository.dart';

class GetHomeSummaryUseCase {
  const GetHomeSummaryUseCase(this._repository);

  final HomeRepository _repository;

  Future<HomeSummary> call() => _repository.getHomeSummary();
}
