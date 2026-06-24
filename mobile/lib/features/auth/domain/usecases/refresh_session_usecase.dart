import '../entities/session.dart';
import '../repositories/auth_repository.dart';

class RefreshSessionUseCase {
  const RefreshSessionUseCase(this._repository);

  final AuthRepository _repository;

  Future<Session> call() {
    return _repository.refreshSession();
  }

  Future<Session?> restore() {
    return _repository.restoreSession();
  }
}
