import '../../../features/profile/domain/entities/profile.dart';
import '../../../features/profile/domain/repositories/profile_repository.dart';
import '../mock_data_store.dart';
import '../mock_fixtures.dart';

/// Mock [ProfileRepository] — UI development only.
class MockProfileRepository implements ProfileRepository {
  MockProfileRepository({MockDataStore? store})
      : _store = store ?? MockDataStore.instance;

  final MockDataStore _store;

  Profile _profile = MockFixtures.profile(MockFixtures.initialCollectedStationIds);

  @override
  Future<Profile> getProfile() async {
    await Future<void>.delayed(const Duration(milliseconds: 200));
    _profile = MockFixtures.profile(_store.collectedStationIds);
    return _profile;
  }

  @override
  Future<Profile> updateProfile({
    String? firstname,
    String? lastname,
    String? bio,
    String? avatarUrl,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 200));
    _profile = Profile(
      id: _profile.id,
      email: _profile.email,
      username: _profile.username,
      firstname: firstname ?? _profile.firstname,
      lastname: lastname ?? _profile.lastname,
      phoneNumber: _profile.phoneNumber,
      avatarUrl: avatarUrl ?? _profile.avatarUrl,
      bio: bio ?? _profile.bio,
      createdAt: _profile.createdAt,
      subtitle: _profile.subtitle,
      stats: _profile.stats,
      invite: _profile.invite,
      memories: _profile.memories,
      achievements: _profile.achievements,
      appVersionLabel: _profile.appVersionLabel,
    );
    return _profile;
  }

  @override
  Future<void> logout() async {
    await Future<void>.delayed(const Duration(milliseconds: 100));
  }
}
