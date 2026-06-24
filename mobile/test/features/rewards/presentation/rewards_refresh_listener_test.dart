import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/rewards_route_refresh.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_cubit.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/rewards_state.dart';
import 'package:metro_stamp_app/features/rewards/presentation/widgets/rewards_refresh_listener.dart';
import 'package:mocktail/mocktail.dart';

class MockRewardsCubit extends Mock implements RewardsCubit {}

void main() {
  late MockRewardsCubit cubit;
  late GoRouter router;

  setUp(() {
    cubit = MockRewardsCubit();
    when(() => cubit.state).thenReturn(const RewardsState());
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.load()).thenAnswer((_) async {});
    when(() => cubit.refresh()).thenAnswer((_) async {});
  });

  Widget buildApp(String location) {
    router = GoRouter(
      initialLocation: location,
      routes: [
        GoRoute(
          path: RouteNames.rewards,
          builder: (context, state) => BlocProvider<RewardsCubit>.value(
            value: cubit,
            child: const RewardsRefreshListener(
              child: SizedBox(key: Key('rewards-body')),
            ),
          ),
        ),
      ],
    );
    return MaterialApp.router(routerConfig: router);
  }

  testWidgets('loads once on first mount without refresh token',
      (tester) async {
    await tester.pumpWidget(buildApp(RouteNames.rewards));
    await tester.pump();
    await tester.pump();

    verify(() => cubit.load()).called(1);
    verifyNever(() => cubit.refresh());
  });

  testWidgets('refetches when refresh query changes', (tester) async {
    when(() => cubit.state).thenReturn(
      const RewardsState(status: RewardsStatus.loaded),
    );

    await tester.pumpWidget(
      buildApp(RewardsRouteRefresh.locationWithRefresh('token-1')),
    );
    await tester.pump();
    await tester.pump();

    verify(() => cubit.refresh()).called(1);
    verifyNever(() => cubit.load());
  });
}
