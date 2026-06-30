import '../repositories/auth_repository.dart';

class ResendVerificationOtpUseCase {
  const ResendVerificationOtpUseCase(this._repository);

  final AuthRepository _repository;

  Future<void> call({required String email}) {
    return _repository.resendVerificationOtp(email: email);
  }
}
