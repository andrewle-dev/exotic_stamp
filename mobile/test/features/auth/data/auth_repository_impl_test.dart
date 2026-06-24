import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/core/network/api_client.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:metro_stamp_app/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:metro_stamp_app/features/auth/data/models/auth_response_model.dart';
import 'package:metro_stamp_app/features/auth/data/models/user_model.dart';
import 'package:metro_stamp_app/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:mocktail/mocktail.dart';

class MockAuthRemoteDataSource extends Mock implements AuthRemoteDataSource {}

class MockApiClient extends Mock implements ApiClient {}

class MockFlutterSecureStorage extends Mock implements FlutterSecureStorage {}

void main() {
  late MockAuthRemoteDataSource remoteDataSource;
  late MockApiClient apiClient;
  late MockFlutterSecureStorage secureStorage;
  late SecureTokenStorage tokenStorage;
  late AuthRepositoryImpl repository;

  setUp(() {
    remoteDataSource = MockAuthRemoteDataSource();
    apiClient = MockApiClient();
    secureStorage = MockFlutterSecureStorage();
    tokenStorage = SecureTokenStorage(storage: secureStorage);
    repository = AuthRepositoryImpl(
      remoteDataSource: remoteDataSource,
      tokenStorage: tokenStorage,
      apiClient: apiClient,
    );

    when(
      () => secureStorage.write(
        key: any(named: 'key'),
        value: any(named: 'value'),
      ),
    ).thenAnswer((_) async {});
    when(() => secureStorage.delete(key: any(named: 'key')))
        .thenAnswer((_) async {});
    when(() => apiClient.clearSession()).thenAnswer((_) async {});
  });

  test('login stores access token on success', () async {
    const response = AuthResponseModel(
      accessToken: 'token-abc',
      tokenType: 'Bearer',
      user: UserModel(
        id: 'user-1',
        email: 'an@example.com',
        username: 'an.nguyen',
      ),
    );

    when(
      () => remoteDataSource.login(
        identifier: any(named: 'identifier'),
        password: any(named: 'password'),
      ),
    ).thenAnswer((_) async => response);

    final session = await repository.login(
      identifier: 'an@example.com',
      password: 'secret',
    );

    expect(session.accessToken, 'token-abc');
    verify(
      () => secureStorage.write(
        key: 'access_token',
        value: 'token-abc',
      ),
    ).called(1);
  });

  test('logout clears token and cookies via api client', () async {
    when(() => remoteDataSource.logout()).thenAnswer((_) async {});

    await repository.logout();

    verify(() => remoteDataSource.logout()).called(1);
    verify(() => apiClient.clearSession()).called(1);
  });

  test('logout clears local session when backend logout fails', () async {
    when(() => remoteDataSource.logout()).thenThrow(
      const Failure(
        code: FailureCode.networkError,
        message: 'Network unreachable',
      ),
    );

    await repository.logout();

    verify(() => remoteDataSource.logout()).called(1);
    verify(() => apiClient.clearSession()).called(1);
  });

  test('restoreSession clears token when refresh fails after expired token',
      () async {
    when(() => secureStorage.read(key: 'access_token'))
        .thenAnswer((_) async => 'expired-token');
    when(() => remoteDataSource.getMe()).thenThrow(
      const Failure(
        code: FailureCode.tokenExpired,
        message: 'Access token expired',
      ),
    );
    when(() => remoteDataSource.refresh()).thenThrow(
      const Failure(
        code: FailureCode.tokenExpired,
        message: 'Refresh token expired',
      ),
    );

    final session = await repository.restoreSession();

    expect(session, isNull);
    verify(() => secureStorage.delete(key: 'access_token')).called(1);
  });
}
