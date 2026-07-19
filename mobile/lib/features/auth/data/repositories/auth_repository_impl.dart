import 'package:flutter/foundation.dart';

import '../../../../core/errors/failure.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/storage/secure_token_storage.dart';
import '../../domain/entities/session.dart';
import '../../domain/entities/user.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_datasource.dart';
import '../models/change_password_request.dart';
import '../models/register_request.dart';

class AuthRepositoryImpl implements AuthRepository {
  AuthRepositoryImpl({
    required AuthRemoteDataSource remoteDataSource,
    required SecureTokenStorage tokenStorage,
    required ApiClient apiClient,
  })  : _remoteDataSource = remoteDataSource,
        _tokenStorage = tokenStorage,
        _apiClient = apiClient;

  final AuthRemoteDataSource _remoteDataSource;
  final SecureTokenStorage _tokenStorage;
  final ApiClient _apiClient;

  @override
  Future<Session> login({
    required String identifier,
    required String password,
  }) async {
    final response = await _remoteDataSource.login(
      identifier: identifier,
      password: password,
    );
    await _tokenStorage.writeAccessToken(response.accessToken);
    return response.toSession();
  }

  @override
  Future<String> register({
    required String firstname,
    required String lastname,
    required String username,
    required String email,
    required String phoneNumber,
    required String password,
  }) {
    return _remoteDataSource.register(
      RegisterRequest(
        firstname: firstname,
        lastname: lastname,
        username: username,
        email: email,
        phoneNumber: phoneNumber,
        password: password,
      ),
    );
  }

  @override
  Future<void> forgotPassword({required String email}) {
    return _remoteDataSource.forgotPassword(email: email);
  }

  @override
  Future<void> verifyAccount({
    required String email,
    required String otp,
  }) {
    return _remoteDataSource.verifyAccount(email: email, otp: otp);
  }

  @override
  Future<void> resendVerificationOtp({required String email}) {
    return _remoteDataSource.resendVerificationOtp(email: email);
  }

  @override
  Future<Session> refreshSession() async {
    final response = await _remoteDataSource.refresh();
    await _tokenStorage.writeAccessToken(response.accessToken);
    return response.toSession();
  }

  @override
  Future<void> logout() async {
    try {
      await _remoteDataSource.logout();
    } catch (_) {
      // Best-effort backend logout; local session is always cleared below.
    } finally {
      await _apiClient.clearSession();
    }
  }

  @override
  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
    required String confirmNewPassword,
  }) {
    return _remoteDataSource.changePassword(
      ChangePasswordRequest(
        currentPassword: currentPassword,
        newPassword: newPassword,
        confirmNewPassword: confirmNewPassword,
      ),
    );
  }

  @override
  Future<User> getCurrentUser() async {
    final user = await _remoteDataSource.getMe();
    return user.toEntity();
  }

  @override
  Future<bool> hasStoredSession() {
    return _tokenStorage.hasAccessToken();
  }

  @override
  Future<Session?> restoreSession() async {
    final hasToken = await _tokenStorage.hasAccessToken();
    // TODO(debug-bug2): remove
    debugPrint('[auth-startup] hasStoredAccessToken=$hasToken');
    if (!hasToken) {
      return _tryRefreshSession();
    }

    try {
      return await _sessionFromCurrentUser();
    } on Failure catch (failure) {
      if (_shouldAttemptRefresh(failure)) {
        return _tryRefreshSession();
      }
      await _tokenStorage.clear();
      return null;
    }
  }

  Future<Session?> _tryRefreshSession() async {
    try {
      return await refreshSession();
    } on Failure {
      await _tokenStorage.clear();
      return null;
    }
  }

  Future<Session> _sessionFromCurrentUser() async {
    final token = await _tokenStorage.readAccessToken();
    if (token == null || token.isEmpty) {
      throw const Failure(
        code: FailureCode.unauthorized,
        message: 'Bạn cần đăng nhập để tiếp tục.',
      );
    }

    final user = await getCurrentUser();
    return Session(accessToken: token, user: user);
  }

  bool _shouldAttemptRefresh(Failure failure) {
    return failure.code == FailureCode.unauthorized ||
        failure.code == FailureCode.tokenExpired;
  }
}
