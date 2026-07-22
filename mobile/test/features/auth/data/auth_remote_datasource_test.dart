import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/core/network/api_client.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:metro_stamp_app/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:metro_stamp_app/features/auth/data/models/auth_response_model.dart';
import 'package:metro_stamp_app/features/auth/data/models/register_request.dart';
import 'package:mocktail/mocktail.dart';

class MockApiClient extends Mock implements ApiClient {}

class MockSecureTokenStorage extends Mock implements SecureTokenStorage {}

void main() {
  late MockApiClient apiClient;
  late MockSecureTokenStorage tokenStorage;
  late AuthRemoteDataSource dataSource;

  setUp(() {
    apiClient = MockApiClient();
    tokenStorage = MockSecureTokenStorage();
    when(() => tokenStorage.getOrCreateDeviceId())
        .thenAnswer((_) async => 'test-device');
    when(() => tokenStorage.readRefreshToken())
        .thenAnswer((_) async => 'refresh-1');
    dataSource = AuthRemoteDataSource(
      apiClient: apiClient,
      tokenStorage: tokenStorage,
    );
  });

  group('AuthRemoteDataSource', () {
    test('login parses auth response including refresh token', () async {
      when(
        () => apiClient.post<Map<String, dynamic>>(
          '/auth/login',
          data: any(named: 'data'),
          options: any(named: 'options'),
        ),
      ).thenAnswer(
        (_) async => Response<Map<String, dynamic>>(
          data: {
            'accessToken': 'token-123',
            'refreshToken': 'refresh-xyz',
            'tokenType': 'Bearer',
            'userInfo': {
              'id': 'user-1',
              'email': 'an@example.com',
              'username': 'an.nguyen',
              'roles': ['USER'],
            },
          },
          requestOptions: RequestOptions(path: '/auth/login'),
          statusCode: 200,
        ),
      );

      final result = await dataSource.login(
        identifier: 'an@example.com',
        password: 'secret',
      );

      expect(result.accessToken, 'token-123');
      expect(result.refreshToken, 'refresh-xyz');
      expect(result.user.email, 'an@example.com');
    });

    test('register returns success message string', () async {
      when(
        () => apiClient.post<dynamic>(
          '/auth/register',
          data: any(named: 'data'),
          options: any(named: 'options'),
        ),
      ).thenAnswer(
        (_) async => Response<dynamic>(
          data: 'Registered successfully! Please check your email.',
          requestOptions: RequestOptions(path: '/auth/register'),
          statusCode: 200,
        ),
      );

      final message = await dataSource.register(
        const RegisterRequest(
          firstname: 'An',
          lastname: 'Nguyen',
          username: 'an.nguyen',
          email: 'an@example.com',
          phoneNumber: '+84901234567',
          password: 'SecurePass123',
        ),
      );

      expect(message, contains('Registered successfully'));
    });

    test('login maps invalid credentials failure', () async {
      when(
        () => apiClient.post<Map<String, dynamic>>(
          any(),
          data: any(named: 'data'),
          options: any(named: 'options'),
        ),
      ).thenThrow(
        DioException(
          requestOptions: RequestOptions(path: '/auth/login'),
          response: Response(
            requestOptions: RequestOptions(path: '/auth/login'),
            statusCode: 401,
            data: {
              'code': 'INVALID_CREDENTIALS',
              'message': 'Invalid email or password',
              'status': 401,
            },
          ),
          type: DioExceptionType.badResponse,
        ),
      );

      expect(
        () => dataSource.login(
          identifier: 'an@example.com',
          password: 'wrong',
        ),
        throwsA(
          isA<Failure>().having(
            (failure) => failure.code,
            'code',
            FailureCode.invalidCredentials,
          ),
        ),
      );
    });
  });

  group('AuthResponseModel', () {
    test('fromJson maps session fields', () {
      final model = AuthResponseModel.fromJson({
        'accessToken': 'abc',
        'refreshToken': 'r1',
        'tokenType': 'Bearer',
        'userInfo': {
          'id': 'id-1',
          'email': 'test@example.com',
          'username': 'tester',
          'roles': ['USER'],
        },
      });

      final session = model.toSession();
      expect(session.accessToken, 'abc');
      expect(model.refreshToken, 'r1');
      expect(session.user.username, 'tester');
    });
  });
}
