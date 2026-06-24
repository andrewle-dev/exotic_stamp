import '../entities/profile.dart';
import '../repositories/profile_repository.dart';

class GetProfileUseCase {
  const GetProfileUseCase(this._repository);

  final ProfileRepository _repository;

  Future<Profile> call() {
    return _repository.getProfile();
  }
}
