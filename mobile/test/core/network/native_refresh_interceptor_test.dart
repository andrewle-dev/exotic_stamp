import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/config/api_config.dart';
import 'package:metro_stamp_app/core/network/native_refresh_interceptor.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:mocktail/mocktail.dart';

class MockFlutterSecureStorage extends Mock implements FlutterSecureStorage {}

void main() {
  late MockFlutterSecureStorage secureStorage;
  late SecureTokenStorage tokenStorage;
  late Dio dio;
  late int sessionInvalidations;

  setUp(() {
    secureStorage = MockFlutterSecureStorage();
    tokenStorage = SecureTokenStorage(storage: secureStorage);
    sessionInvalidations = 0;
    dio = Dio(BaseOptions(baseUrl: 'http://localhost'));
    dio.interceptors.add(
      NativeRefreshInterceptor(
        dio: dio,
        tokenStorage: tokenStorage,
        onSessionInvalidated: () async {
          sessionInvalidations += 1;
        },
      ),
    );

    when(() => secureStorage.read(key: any(named: 'key')))
        .thenAnswer((_) async => 'refresh-old');
    when(() => secureStorage.delete(key: any(named: 'key')))
        .thenAnswer((_) async {});
  });

  test('storage write failure after refresh clears session and invalidates once', () async {
    when(
      () => secureStorage.write(
        key: any(named: 'key'),
        value: any(named: 'value'),
      ),
    ).thenThrow(Exception('keystore write failed'));

    dio.httpClientAdapter = _ScriptedAdapter((options) async {
      if (options.path.contains(ApiConfig.refreshPath)) {
        return ResponseBody.fromString(
          '{"accessToken":"access-new","refreshToken":"refresh-new"}',
          200,
          headers: {
            Headers.contentTypeHeader: [Headers.jsonContentType],
          },
        );
      }
      return ResponseBody.fromString('{"error":"unauthorized"}', 401);
    });

    await tokenStorage.writeAccessToken('stale-access');

    await expectLater(
      dio.get<dynamic>('/api/v1/users/me'),
      throwsA(isA<DioException>()),
    );

    expect(tokenStorage.readAccessToken(), isNull);
    expect(sessionInvalidations, 1);
    verify(() => secureStorage.delete(key: 'refresh_token')).called(greaterThanOrEqualTo(1));
  });
}

typedef _Respond = Future<ResponseBody> Function(RequestOptions options);

class _ScriptedAdapter implements HttpClientAdapter {
  _ScriptedAdapter(this._respond);

  final _Respond _respond;

  @override
  void close({bool force = false}) {}

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<List<int>>? requestStream,
    Future<void>? cancelFuture,
  ) {
    return _respond(options);
  }
}
