import '../repositories/auth_repository.dart';

class ChangePasswordParams {
  const ChangePasswordParams({
    required this.currentPassword,
    required this.newPassword,
    required this.confirmNewPassword,
  });

  final String currentPassword;
  final String newPassword;
  final String confirmNewPassword;
}

class ChangePasswordUseCase {
  const ChangePasswordUseCase(this._repository);

  final AuthRepository _repository;

  Future<void> call(ChangePasswordParams params) {
    return _repository.changePassword(
      currentPassword: params.currentPassword,
      newPassword: params.newPassword,
      confirmNewPassword: params.confirmNewPassword,
    );
  }
}
