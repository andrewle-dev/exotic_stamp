import '../entities/session.dart';
import '../entities/user.dart';

abstract class AuthRepository {
  Future<Session> login({
    required String identifier,
    required String password,
  });

  Future<String> register({
    required String firstname,
    required String lastname,
    required String username,
    required String email,
    required String phoneNumber,
    required String password,
  });

  Future<void> forgotPassword({required String email});

  Future<void> verifyAccount({
    required String email,
    required String otp,
  });

  Future<void> resendVerificationOtp({required String email});

  Future<Session> refreshSession();

  Future<void> logout();

  Future<User> getCurrentUser();

  Future<Session?> restoreSession();

  Future<bool> hasStoredSession();
}
