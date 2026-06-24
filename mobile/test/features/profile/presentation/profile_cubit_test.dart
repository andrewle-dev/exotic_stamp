import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';
import 'package:metro_stamp_app/features/profile/domain/entities/profile.dart';
import 'package:metro_stamp_app/features/profile/domain/repositories/profile_repository.dart';
import 'package:metro_stamp_app/features/profile/domain/usecases/get_profile_usecase.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/profile_cubit.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/profile_state.dart';
import 'package:mocktail/mocktail.dart';

class MockProfileRepository extends Mock implements ProfileRepository {}

void main() {
  late MockProfileRepository repository;
  late ProfileCubit cubit;

  const profile = Profile(
    id: 'user-1',
    email: 'an@example.com',
    username: 'an.nguyen',
    firstname: 'An',
    lastname: 'Nguyen',
  );

  setUp(() {
    repository = MockProfileRepository();
    cubit = ProfileCubit(
      getProfileUseCase: GetProfileUseCase(repository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<ProfileCubit, ProfileState>(
    'emits loading then loaded on success',
    build: () {
      when(() => repository.getProfile()).thenAnswer((_) async => profile);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<ProfileState>()
          .having((s) => s.status, 'status', ProfileStatus.loading),
      isA<ProfileState>()
          .having((s) => s.status, 'status', ProfileStatus.loaded)
          .having((s) => s.profile?.displayName, 'name', 'An Nguyen'),
    ],
  );

  blocTest<ProfileCubit, ProfileState>(
    'emits unauthorized on auth failure',
    build: () {
      when(() => repository.getProfile()).thenThrow(
        const Failure(
          code: FailureCode.unauthorized,
          message: 'Unauthorized',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<ProfileState>()
          .having((s) => s.status, 'status', ProfileStatus.loading),
      isA<ProfileState>()
          .having((s) => s.status, 'status', ProfileStatus.unauthorized),
    ],
  );

  blocTest<ProfileCubit, ProfileState>(
    'emits error on network failure',
    build: () {
      when(() => repository.getProfile()).thenThrow(
        const Failure(
          code: FailureCode.networkError,
          message: 'Network down',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<ProfileState>()
          .having((s) => s.status, 'status', ProfileStatus.loading),
      isA<ProfileState>()
          .having((s) => s.status, 'status', ProfileStatus.error)
          .having((s) => s.failure?.code, 'code', FailureCode.networkError),
    ],
  );
}
