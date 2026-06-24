import '../repositories/auth_repository.dart';

class RegisterParams {
  const RegisterParams({
    required this.firstname,
    required this.lastname,
    required this.username,
    required this.email,
    required this.phoneNumber,
    required this.password,
  });

  final String firstname;
  final String lastname;
  final String username;
  final String email;
  final String phoneNumber;
  final String password;
}

class RegisterUseCase {
  const RegisterUseCase(this._repository);

  final AuthRepository _repository;

  Future<String> call(RegisterParams params) {
    return _repository.register(
      firstname: params.firstname,
      lastname: params.lastname,
      username: params.username,
      email: params.email,
      phoneNumber: params.phoneNumber,
      password: params.password,
    );
  }
}
