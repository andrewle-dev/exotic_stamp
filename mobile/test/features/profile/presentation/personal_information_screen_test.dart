import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/profile/domain/entities/profile.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/settings_cubit.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/settings_state.dart';
import 'package:metro_stamp_app/features/profile/presentation/screens/personal_information_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockSettingsCubit extends MockCubit<SettingsState>
    implements SettingsCubit {}

void main() {
  late MockSettingsCubit settingsCubit;

  setUp(() {
    settingsCubit = MockSettingsCubit();
    when(() => settingsCubit.state).thenReturn(
      const SettingsState(
        status: SettingsStatus.loaded,
        profile: Profile(
          id: 'user-1',
          email: 'an@example.com',
          username: 'an.nguyen',
          firstname: 'An',
          lastname: 'Nguyen',
          bio: 'Commuter',
        ),
      ),
    );
    when(() => settingsCubit.load()).thenAnswer((_) async {});
  });

  testWidgets('shows editable personal fields and read-only email',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: PersonalInformationScreen(cubit: settingsCubit),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Personal Information'), findsOneWidget);
    expect(find.text('Your details'), findsOneWidget);
    expect(find.text('Account details'), findsOneWidget);
    expect(find.text('First name'), findsOneWidget);
    expect(find.text('Last name'), findsOneWidget);
    expect(find.text('Bio'), findsOneWidget);
    expect(
      find.text(
        'Email is managed with your account and can’t be edited here.',
      ),
      findsOneWidget,
    );
    expect(
      find.text(
        'Phone number can’t be changed in the app. Contact support if you need an update.',
      ),
      findsOneWidget,
    );
    expect(find.text('Save changes'), findsOneWidget);
    expect(find.text('Log Out'), findsNothing);
    expect(find.text('Đăng xuất'), findsNothing);
  });
}
