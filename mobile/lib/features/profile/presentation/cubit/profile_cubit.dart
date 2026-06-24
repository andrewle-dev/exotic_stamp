import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import 'profile_state.dart';

class ProfileCubit extends Cubit<ProfileState> {
  ProfileCubit({required GetProfileUseCase getProfileUseCase})
      : _getProfileUseCase = getProfileUseCase,
        super(const ProfileState());

  final GetProfileUseCase _getProfileUseCase;

  Future<void> load() async {
    emit(state.copyWith(status: ProfileStatus.loading, clearFailure: true));
    try {
      final profile = await _getProfileUseCase();
      emit(
        state.copyWith(
          status: ProfileStatus.loaded,
          profile: profile,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      final status = failure.isAuthFailure
          ? ProfileStatus.unauthorized
          : ProfileStatus.error;
      emit(
        state.copyWith(
          status: status,
          failure: failure,
          clearProfile: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: ProfileStatus.error,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải hồ sơ.',
          ),
          clearProfile: true,
        ),
      );
    }
  }
}
