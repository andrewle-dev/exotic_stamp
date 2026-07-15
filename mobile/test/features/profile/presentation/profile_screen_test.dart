import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/profile/domain/entities/profile.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/profile_cubit.dart';
import 'package:metro_stamp_app/features/profile/presentation/cubit/profile_state.dart';
import 'package:metro_stamp_app/features/profile/presentation/screens/profile_screen.dart';
import 'package:metro_stamp_app/shared/widgets/app_loading_view.dart';
import 'package:mocktail/mocktail.dart';

class MockProfileCubit extends MockCubit<ProfileState>
    implements ProfileCubit {}

void main() {
  late MockProfileCubit cubit;

  setUp(() {
    cubit = MockProfileCubit();
  });

  Widget buildSubject() {
    return MaterialApp(
      home: ProfileScreen(cubit: cubit),
    );
  }

  testWidgets('does not show fake stats when API data is missing',
      (tester) async {
    when(() => cubit.state).thenReturn(
      const ProfileState(
        status: ProfileStatus.loaded,
        profile: Profile(
          id: 'user-1',
          email: 'an@example.com',
          username: 'an.nguyen',
          firstname: 'An',
          lastname: 'Nguyen',
        ),
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.text('42'), findsNothing);
    expect(find.text('#152'), findsNothing);
    expect(find.text('12/24'), findsNothing);
    expect(
      find.text('Stats will appear when the backend provides profile data.'),
      findsOneWidget,
    );
    expect(find.text('0'), findsNothing);
    expect(find.text('STAMPS'), findsNothing);
    expect(find.text('LINES'), findsNothing);
    expect(find.text('RANK'), findsNothing);
  });

  testWidgets('shows backend-provided stats when available', (tester) async {
    when(() => cubit.state).thenReturn(
      const ProfileState(
        status: ProfileStatus.loaded,
        profile: Profile(
          id: 'user-1',
          email: 'an@example.com',
          username: 'an.nguyen',
          firstname: 'An',
          lastname: 'Nguyen',
          stats: ProfileStats(
            collectedStampsCount: 7,
            linesCount: 2,
            rankPosition: 88,
          ),
        ),
      ),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.text('7'), findsOneWidget);
    expect(find.text('#88'), findsOneWidget);
    expect(find.text('STAMPS'), findsOneWidget);
    expect(find.text('LINES'), findsOneWidget);
    expect(find.text('RANK'), findsOneWidget);
  });

  testWidgets('shows loading view while fetching profile', (tester) async {
    when(() => cubit.state).thenReturn(
      const ProfileState(status: ProfileStatus.loading),
    );
    when(() => cubit.load()).thenAnswer((_) async {});

    await tester.pumpWidget(buildSubject());
    await tester.pump();

    expect(find.byType(AppLoadingView), findsOneWidget);
  });
}
