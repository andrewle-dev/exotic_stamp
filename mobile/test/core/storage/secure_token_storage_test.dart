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
    test('keeps access token in memory only', () async {
      await tokenStorage.writeAccessToken('token-123');
      expect(tokenStorage.readAccessToken(), 'token-123');
      verifyNever(() => mockStorage.write(
            key: any(named: 'key'),
            value: any(named: 'value'),
          ));
    });

    test('stores refresh token in secure storage', () async {
      when(
        () => mockStorage.write(
          key: any(named: 'key'),
          value: any(named: 'value'),
        ),
      ).thenAnswer((_) async {});

      await tokenStorage.writeRefreshToken('refresh-abc');

      verify(
        () => mockStorage.write(
          key: 'refresh_token',
          value: 'refresh-abc',
        ),
      ).called(1);
    });

    test('hasAccessToken returns false when memory empty', () async {
      expect(await tokenStorage.hasAccessToken(), isFalse);
    });

    test('clear removes refresh token and memory access', () async {
      when(
        () => mockStorage.delete(key: any(named: 'key')),
      ).thenAnswer((_) async {});

      await tokenStorage.writeAccessToken('a');
      await tokenStorage.clear();

      expect(tokenStorage.readAccessToken(), isNull);
      verify(() => mockStorage.delete(key: 'refresh_token')).called(1);
    });

    test('device id is stable across reads', () async {
      when(() => mockStorage.read(key: 'device_id'))
          .thenAnswer((_) async => 'device-1');

      final first = await tokenStorage.getOrCreateDeviceId();
      final second = await tokenStorage.getOrCreateDeviceId();
      expect(first, 'device-1');
      expect(second, 'device-1');
    });
  });
}
