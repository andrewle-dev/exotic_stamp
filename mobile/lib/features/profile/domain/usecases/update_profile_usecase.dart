import '../entities/profile.dart';
import '../repositories/profile_repository.dart';

class UpdateProfileParams {
  const UpdateProfileParams({
    this.firstname,
    this.lastname,
    this.bio,
    this.avatarUrl,
  });

  final String? firstname;
  final String? lastname;
  final String? bio;
  final String? avatarUrl;
}

class UpdateProfileUseCase {
  const UpdateProfileUseCase(this._repository);

  final ProfileRepository _repository;

  Future<Profile> call(UpdateProfileParams params) {
    return _repository.updateProfile(
      firstname: params.firstname,
      lastname: params.lastname,
      bio: params.bio,
      avatarUrl: params.avatarUrl,
    );
  }
}
