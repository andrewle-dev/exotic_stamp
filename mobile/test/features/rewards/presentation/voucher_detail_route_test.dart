import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/voucher_detail.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/voucher_detail_cubit.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/voucher_detail_state.dart';
import 'package:metro_stamp_app/features/rewards/presentation/screens/voucher_detail_screen.dart';
import 'package:mocktail/mocktail.dart';

class MockVoucherDetailCubit extends Mock implements VoucherDetailCubit {}

void main() {
  late MockVoucherDetailCubit cubit;
  late GoRouter router;

  setUp(() {
    cubit = MockVoucherDetailCubit();
    when(() => cubit.state).thenReturn(
      const VoucherDetailState(
        status: VoucherDetailStatus.loaded,
        detail: VoucherDetail(
          id: 'reward-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.available,
          voucherCode: 'ABC-123',
        ),
      ),
    );
    when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
    when(() => cubit.close()).thenAnswer((_) async {});
    when(() => cubit.load()).thenAnswer((_) async {});

    router = GoRouter(
      initialLocation: RouteNames.voucherDetail('reward-1'),
      routes: [
        GoRoute(
          path: '/rewards/vouchers/:voucherId',
          builder: (context, state) => VoucherDetailScreen(
            voucherId: state.pathParameters['voucherId']!,
            cubit: cubit,
          ),
        ),
      ],
    );
  });

  testWidgets('route to voucher detail works', (tester) async {
    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pump();

    expect(find.text('Chi tiết voucher'), findsOneWidget);
    expect(find.text('Coffee Voucher'), findsOneWidget);
    expect(find.text('ABC-123'), findsOneWidget);
  });
}
