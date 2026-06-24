import '../../../../core/network/api_client.dart';
import '../../domain/entities/profile.dart';
import '../../domain/repositories/profile_repository.dart';
import '../datasources/profile_remote_datasource.dart';
import '../models/update_profile_request_model.dart';

class ProfileRepositoryImpl implements ProfileRepository {
  ProfileRepositoryImpl({
    required ProfileRemoteDataSource remoteDataSource,
    required ApiClient apiClient,
  })  : _remoteDataSource = remoteDataSource,
        _apiClient = apiClient;

  final ProfileRemoteDataSource _remoteDataSource;
  final ApiClient _apiClient;

  @override
  Future<Profile> getProfile() async {
    final profile = await _remoteDataSource.getMe();

    final collected = await _remoteDataSource.getCollectedStampsCount();
    final memories = await _remoteDataSource.getMemoriesCount();

    ProfileStats? stats;
    if (collected != null || memories != null) {
      stats = ProfileStats(
        collectedStampsCount: collected,
        memoriesCount: memories,
      );
    }

    return profile.copyWithStats(stats: stats).toEntity();
  }

  @override
  Future<Profile> updateProfile({
    String? firstname,
    String? lastname,
    String? bio,
    String? avatarUrl,
  }) async {
    final updated = await _remoteDataSource.updateMe(
      UpdateProfileRequestModel(
        firstname: firstname,
        lastname: lastname,
        bio: bio,
        avatarUrl: avatarUrl,
      ),
    );
    return updated.toEntity();
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
}
