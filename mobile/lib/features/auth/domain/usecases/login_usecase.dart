import '../entities/session.dart';
import '../repositories/auth_repository.dart';

class LoginUseCase {
  const LoginUseCase(this._repository);

  final AuthRepository _repository;

  Future<Session> call({
    required String identifier,
    required String password,
  }) {
    return _repository.login(
      identifier: identifier,
      password: password,
    );
  }
}
