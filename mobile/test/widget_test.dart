import 'package:cookie_jar/cookie_jar.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/app/app.dart';
import 'package:metro_stamp_app/core/di/injection.dart';
import 'package:metro_stamp_app/core/network/api_client.dart';
import 'package:metro_stamp_app/core/storage/local_preferences.dart';
import 'package:metro_stamp_app/core/storage/secure_token_storage.dart';
import 'package:mocktail/mocktail.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MockFlutterSecureStorage extends Mock implements FlutterSecureStorage {}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late MockFlutterSecureStorage mockStorage;

  setUp(() async {
    Injection.instance.reset();
    SharedPreferences.setMockInitialValues({});
    mockStorage = MockFlutterSecureStorage();

    when(
      () => mockStorage.read(key: any(named: 'key')),
    ).thenAnswer((_) async => null);
    when(
      () => mockStorage.write(
        key: any(named: 'key'),
        value: any(named: 'value'),
      ),
    ).thenAnswer((_) async {});
    when(
      () => mockStorage.delete(key: any(named: 'key')),
    ).thenAnswer((_) async {});

    final localPreferences = LocalPreferences();
    await localPreferences.init();

    final tokenStorage = SecureTokenStorage(storage: mockStorage);
    final apiClient = await ApiClient.create(
      tokenStorage: tokenStorage,
      cookieJar: CookieJar(),
    );

    await Injection.instance.init(
      tokenStorageOverride: tokenStorage,
      localPreferencesOverride: localPreferences,
      apiClientOverride: apiClient,
      restoreSession: false,
    );
  });

  testWidgets('shows welcome screen on first launch',
      (WidgetTester tester) async {
    await tester.pumpWidget(const MetroStampApp());
    await tester.pumpAndSettle();

    expect(find.text('Exotic Stamp'), findsOneWidget);
    expect(find.textContaining('Chạm NFC'), findsOneWidget);
    expect(find.text('Bắt đầu'), findsOneWidget);
  });
}
