import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/profile/domain/entities/profile.dart';
import 'package:metro_stamp_app/features/profile/domain/repositories/profile_repository.dart';
import 'package:metro_stamp_app/features/profile/domain/usecases/get_profile_usecase.dart';
import 'package:metro_stamp_app/features/profile/domain/usecases/logout_all_profile_usecase.dart';
import 'package:metro_stamp_app/features/profile/domain/usecases/logout_profile_usecase.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/privacy_security_cubit.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/privacy_security_state.dart';
import 'package:mocktail/mocktail.dart';

class MockProfileRepository extends Mock implements ProfileRepository {}

void main() {
  late MockProfileRepository repository;
  late PrivacySecurityCubit cubit;

  const profile = Profile(
    id: 'user-1',
    email: 'an@example.com',
    username: 'an.nguyen',
    firstname: 'An',
    lastname: 'Nguyen',
  );

  setUp(() {
    repository = MockProfileRepository();
    cubit = PrivacySecurityCubit(
      getProfileUseCase: GetProfileUseCase(repository),
      logoutProfileUseCase: LogoutProfileUseCase(repository),
      logoutAllProfileUseCase: LogoutAllProfileUseCase(repository),
    );
  });

  tearDown(() => cubit.close());

  blocTest<PrivacySecurityCubit, PrivacySecurityState>(
    'load emits loaded with profile',
    build: () {
      when(() => repository.getProfile()).thenAnswer((_) async => profile);
      return cubit;
    },
    act: (cubit) => cubit.load(),
    expect: () => [
      isA<PrivacySecurityState>().having(
        (s) => s.status,
        'status',
        PrivacySecurityStatus.loading,
      ),
      isA<PrivacySecurityState>()
          .having((s) => s.status, 'status', PrivacySecurityStatus.loaded)
          .having((s) => s.profile?.email, 'email', 'an@example.com'),
    ],
  );

  blocTest<PrivacySecurityCubit, PrivacySecurityState>(
    'logout calls repository logout',
    build: () {
      when(() => repository.logout()).thenAnswer((_) async {});
      return cubit;
    },
    act: (cubit) => cubit.logout(),
    expect: () => [
      isA<PrivacySecurityState>().having(
        (s) => s.status,
        'status',
        PrivacySecurityStatus.loggingOut,
      ),
    ],
    verify: (_) {
      verify(() => repository.logout()).called(1);
    },
  );

  blocTest<PrivacySecurityCubit, PrivacySecurityState>(
    'logoutAll calls repository logoutAll',
    build: () {
      when(() => repository.logoutAll()).thenAnswer((_) async {});
      return cubit;
    },
    act: (cubit) => cubit.logoutAll(),
    expect: () => [
      isA<PrivacySecurityState>().having(
        (s) => s.status,
        'status',
        PrivacySecurityStatus.loggingOut,
      ),
    ],
    verify: (_) {
      verify(() => repository.logoutAll()).called(1);
    },
  );
}
