import '../repositories/auth_repository.dart';

class VerifyAccountUseCase {
  const VerifyAccountUseCase(this._repository);

  final AuthRepository _repository;

  Future<void> call({
    required String email,
    required String otp,
  }) {
    return _repository.verifyAccount(email: email, otp: otp);
  }
}
