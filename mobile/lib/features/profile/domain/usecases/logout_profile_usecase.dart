import '../repositories/profile_repository.dart';

class LogoutProfileUseCase {
  const LogoutProfileUseCase(this._repository);

  final ProfileRepository _repository;

  Future<void> call() {
    return _repository.logout();
  }
}
