import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/profile/domain/entities/profile.dart';
import 'package:metro_stamp_app/features/profile/domain/repositories/profile_repository.dart';
import 'package:metro_stamp_app/features/profile/domain/usecases/get_profile_usecase.dart';
import 'package:metro_stamp_app/features/profile/domain/usecases/update_profile_usecase.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/settings_cubit.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/settings_state.dart';
import 'package:mocktail/mocktail.dart';

class MockProfileRepository extends Mock implements ProfileRepository {}

void main() {
  late MockProfileRepository repository;
  late SettingsCubit cubit;

  const profile = Profile(
    id: 'user-1',
    email: 'an@example.com',
    username: 'an.nguyen',
    firstname: 'An',
    lastname: 'Nguyen',
  );

  setUp(() {
    repository = MockProfileRepository();
    cubit = SettingsCubit(
      getProfileUseCase: GetProfileUseCase(repository),
      updateProfileUseCase: UpdateProfileUseCase(repository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<SettingsCubit, SettingsState>(
    'updateProfile emits saveSuccess with backend-confirmed profile',
    build: () {
      when(
        () => repository.updateProfile(
          firstname: any(named: 'firstname'),
          lastname: any(named: 'lastname'),
          bio: any(named: 'bio'),
          avatarUrl: any(named: 'avatarUrl'),
        ),
      ).thenAnswer(
        (_) async => const Profile(
          id: 'user-1',
          email: 'an@example.com',
          username: 'an.nguyen',
          firstname: 'Alex',
          lastname: 'Chen',
        ),
      );
      return cubit;
    },
    seed: () => const SettingsState(
      status: SettingsStatus.loaded,
      profile: profile,
    ),
    act: (cubit) => cubit.updateProfile(
      firstname: 'Alex',
      lastname: 'Chen',
    ),
    expect: () => [
      isA<SettingsState>()
          .having((s) => s.status, 'status', SettingsStatus.saving),
      isA<SettingsState>()
          .having((s) => s.status, 'status', SettingsStatus.saveSuccess)
          .having((s) => s.profile?.displayName, 'name', 'Alex Chen'),
    ],
  );

  blocTest<SettingsCubit, SettingsState>(
    'updateProfile maps validation error to field errors',
    build: () {
      when(
        () => repository.updateProfile(
          firstname: any(named: 'firstname'),
          lastname: any(named: 'lastname'),
          bio: any(named: 'bio'),
          avatarUrl: any(named: 'avatarUrl'),
        ),
      ).thenThrow(
        const Failure(
          code: FailureCode.validationError,
          message: 'Invalid firstname value',
        ),
      );
      return cubit;
    },
    seed: () => const SettingsState(
      status: SettingsStatus.loaded,
      profile: profile,
    ),
    act: (cubit) => cubit.updateProfile(
      firstname: 'Alex',
      lastname: 'Chen',
    ),
    expect: () => [
      isA<SettingsState>()
          .having((s) => s.status, 'status', SettingsStatus.saving),
      isA<SettingsState>()
          .having((s) => s.status, 'status', SettingsStatus.saveFailure)
          .having((s) => s.fieldErrors['firstname'], 'firstname',
              'Invalid firstname value'),
    ],
  );

  blocTest<SettingsCubit, SettingsState>(
    'updateProfile emits unauthorized on auth failure',
    build: () {
      when(
        () => repository.updateProfile(
          firstname: any(named: 'firstname'),
          lastname: any(named: 'lastname'),
          bio: any(named: 'bio'),
          avatarUrl: any(named: 'avatarUrl'),
        ),
      ).thenThrow(
        const Failure(
          code: FailureCode.unauthorized,
          message: 'Session expired',
        ),
      );
      return cubit;
    },
    seed: () => const SettingsState(
      status: SettingsStatus.loaded,
      profile: profile,
    ),
    act: (cubit) => cubit.updateProfile(
      firstname: 'Alex',
      lastname: 'Chen',
    ),
    expect: () => [
      isA<SettingsState>()
          .having((s) => s.status, 'status', SettingsStatus.saving),
      isA<SettingsState>()
          .having((s) => s.status, 'status', SettingsStatus.unauthorized),
    ],
  );
}
