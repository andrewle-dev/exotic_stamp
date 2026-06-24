import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:mocktail/mocktail.dart';

class MockFlutterSecureStorage extends Mock implements FlutterSecureStorage {}

void main() {
  late MockFlutterSecureStorage mockStorage;
  late SecureTokenStorage tokenStorage;

  setUp(() {
    mockStorage = MockFlutterSecureStorage();
    tokenStorage = SecureTokenStorage(storage: mockStorage);
  });

  group('SecureTokenStorage', () {
    test('reads access token from secure storage', () async {
      when(
        () => mockStorage.read(key: any(named: 'key')),
      ).thenAnswer((_) async => 'token-123');

      final token = await tokenStorage.readAccessToken();

      expect(token, 'token-123');
      verify(() => mockStorage.read(key: 'access_token')).called(1);
    });

    test('writes access token to secure storage', () async {
      when(
        () => mockStorage.write(
          key: any(named: 'key'),
          value: any(named: 'value'),
        ),
      ).thenAnswer((_) async {});

      await tokenStorage.writeAccessToken('token-abc');

      verify(
        () => mockStorage.write(
          key: 'access_token',
          value: 'token-abc',
        ),
      ).called(1);
    });

    test('hasAccessToken returns false when token missing', () async {
      when(
        () => mockStorage.read(key: any(named: 'key')),
      ).thenAnswer((_) async => null);

      expect(await tokenStorage.hasAccessToken(), isFalse);
    });

    test('clear removes access token', () async {
      when(
        () => mockStorage.delete(key: any(named: 'key')),
      ).thenAnswer((_) async {});

      await tokenStorage.clear();

      verify(() => mockStorage.delete(key: 'access_token')).called(1);
    });
  });
}
