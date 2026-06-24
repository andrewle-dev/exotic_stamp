import '../entities/profile.dart';

abstract class ProfileRepository {
  Future<Profile> getProfile();

  Future<Profile> updateProfile({
    String? firstname,
    String? lastname,
    String? bio,
    String? avatarUrl,
  });

  Future<void> logout();
}
