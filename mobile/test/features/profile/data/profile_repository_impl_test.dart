import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/network/api_client.dart';
import 'package:metro_stamp_app/features/profile/data/datasources/profile_remote_datasource.dart';
import 'package:metro_stamp_app/features/profile/data/models/profile_model.dart';
import 'package:metro_stamp_app/features/profile/data/models/update_profile_request_model.dart';
import 'package:metro_stamp_app/features/profile/data/repositories/profile_repository_impl.dart';
import 'package:mocktail/mocktail.dart';

class MockProfileRemoteDataSource extends Mock
    implements ProfileRemoteDataSource {}

class MockApiClient extends Mock implements ApiClient {}

void main() {
  late MockProfileRemoteDataSource remoteDataSource;
  late MockApiClient apiClient;
  late ProfileRepositoryImpl repository;

  setUpAll(() {
    registerFallbackValue(const UpdateProfileRequestModel());
  });

  const userJson = {
    'id': '550e8400-e29b-41d4-a716-446655440000',
    'firstname': 'An',
    'lastname': 'Nguyen',
    'username': 'an.nguyen',
    'email': 'an@example.com',
    'phoneNumber': '+84901234567',
    'avatarUrl': null,
    'bio': null,
    'created_at': '2026-01-15T08:30:00',
  };

  setUp(() {
    remoteDataSource = MockProfileRemoteDataSource();
    apiClient = MockApiClient();
    repository = ProfileRepositoryImpl(
      remoteDataSource: remoteDataSource,
      apiClient: apiClient,
    );

    when(() => remoteDataSource.getCollectedStampsCount())
        .thenAnswer((_) async => null);
    when(() => remoteDataSource.getMemoriesCount())
        .thenAnswer((_) async => null);
    when(() => apiClient.clearSession()).thenAnswer((_) async {});
  });

  test('getProfile maps GET /users/me response', () async {
    when(() => remoteDataSource.getMe()).thenAnswer(
      (_) async => ProfileModel.fromUserResponse(userJson),
    );

    final profile = await repository.getProfile();

    expect(profile.id, userJson['id']);
    expect(profile.firstname, 'An');
    expect(profile.lastname, 'Nguyen');
    expect(profile.email, 'an@example.com');
    expect(profile.phoneNumber, '+84901234567');
    expect(profile.displayName, 'An Nguyen');
    verify(() => remoteDataSource.getMe()).called(1);
  });

  test('getProfile omits stats when optional APIs fail', () async {
    when(() => remoteDataSource.getMe()).thenAnswer(
      (_) async => ProfileModel.fromUserResponse(userJson),
    );
    when(() => remoteDataSource.getCollectedStampsCount())
        .thenAnswer((_) async => null);
    when(() => remoteDataSource.getMemoriesCount())
        .thenAnswer((_) async => null);

    final profile = await repository.getProfile();

    expect(profile.stats, isNull);
  });

  test('updateProfile maps PUT /users/me response', () async {
    const updatedJson = {
      'id': '550e8400-e29b-41d4-a716-446655440000',
      'firstname': 'Alex',
      'lastname': 'Chen',
      'username': 'an.nguyen',
      'email': 'an@example.com',
      'phoneNumber': '+84901234567',
    };

    when(
      () => remoteDataSource.updateMe(any()),
    ).thenAnswer((_) async => ProfileModel.fromUserResponse(updatedJson));

    final profile = await repository.updateProfile(
      firstname: 'Alex',
      lastname: 'Chen',
    );

    expect(profile.firstname, 'Alex');
    expect(profile.lastname, 'Chen');
    expect(profile.displayName, 'Alex Chen');
    verify(() => remoteDataSource.updateMe(any())).called(1);
  });

  test('logout clears token and cookies even if backend logout fails',
      () async {
    when(() => remoteDataSource.logout()).thenThrow(Exception('network'));
    when(() => apiClient.clearSession()).thenAnswer((_) async {});

    await repository.logout();

    verify(() => remoteDataSource.logout()).called(1);
    verify(() => apiClient.clearSession()).called(1);
  });
}
