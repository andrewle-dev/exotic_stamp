import '../repositories/profile_repository.dart';

class LogoutAllProfileUseCase {
  const LogoutAllProfileUseCase(this._repository);

  final ProfileRepository _repository;

  Future<void> call() {
    return _repository.logoutAll();
  }
}
