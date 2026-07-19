import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/voucher_detail.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/voucher_detail_cubit.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/voucher_detail_state.dart';
import 'package:metro_stamp_app/features/rewards/presentation/screens/voucher_detail_screen.dart';

class MockVoucherDetailCubit extends Mock implements VoucherDetailCubit {}

void main() {
  late MockVoucherDetailCubit cubit;

  group('available voucher', () {
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
            expiresAt: null,
          ),
        ),
      );
      when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
      when(() => cubit.close()).thenAnswer((_) async {});
      when(() => cubit.load()).thenAnswer((_) async {});
      when(() => cubit.redeem()).thenAnswer((_) async {});
    });

    testWidgets('shows redeem code without online redeem CTA', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: VoucherDetailScreen(
            voucherId: 'reward-1',
            cubit: cubit,
          ),
        ),
      );
      await tester.pump();

      expect(find.text('ABC-123'), findsOneWidget);
      expect(find.text('Redeem code'), findsOneWidget);
      expect(find.text('Đổi quà ngay'), findsNothing);
      expect(find.text('Sao chép mã đổi quà'), findsOneWidget);
    });
  });

  group('used voucher', () {
    setUp(() {
      cubit = MockVoucherDetailCubit();
      when(() => cubit.state).thenReturn(
        VoucherDetailState(
          status: VoucherDetailStatus.loaded,
          detail: VoucherDetail(
            id: 'reward-1',
            rewardTitle: 'Coffee Voucher',
            status: UserRewardStatus.used,
            redeemedAt: DateTime(2026, 6, 20),
          ),
        ),
      );
      when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
      when(() => cubit.close()).thenAnswer((_) async {});
      when(() => cubit.load()).thenAnswer((_) async {});
      when(() => cubit.redeem()).thenAnswer((_) async {});
    });

    testWidgets('is disabled without redeem CTA', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: VoucherDetailScreen(
            voucherId: 'reward-1',
            cubit: cubit,
          ),
        ),
      );
      await tester.pump();

      expect(find.text('Voucher đã sử dụng'), findsOneWidget);
      expect(find.text('Xuất trình mã tại quầy'), findsNothing);
      expect(find.text('Đổi quà ngay'), findsNothing);
    });
  });

  group('expired voucher', () {
    setUp(() {
      cubit = MockVoucherDetailCubit();
      when(() => cubit.state).thenReturn(
        const VoucherDetailState(
          status: VoucherDetailStatus.loaded,
          detail: VoucherDetail(
            id: 'reward-1',
            rewardTitle: 'Coffee Voucher',
            status: UserRewardStatus.expired,
          ),
        ),
      );
      when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
      when(() => cubit.close()).thenAnswer((_) async {});
      when(() => cubit.load()).thenAnswer((_) async {});
    });

    testWidgets('shows expired state without redeem CTA', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: VoucherDetailScreen(
            voucherId: 'reward-1',
            cubit: cubit,
          ),
        ),
      );
      await tester.pump();

      expect(find.text('Voucher đã hết hạn'), findsOneWidget);
      expect(find.text('Xuất trình mã tại quầy'), findsNothing);
      expect(find.text('Đổi quà ngay'), findsNothing);
    });
  });

  group('pending voucher', () {
    setUp(() {
      cubit = MockVoucherDetailCubit();
      when(() => cubit.state).thenReturn(
        const VoucherDetailState(
          status: VoucherDetailStatus.loaded,
          detail: VoucherDetail(
            id: 'reward-1',
            rewardTitle: 'Coffee Voucher',
            status: UserRewardStatus.pending,
          ),
        ),
      );
      when(() => cubit.stream).thenAnswer((_) => const Stream.empty());
      when(() => cubit.close()).thenAnswer((_) async {});
      when(() => cubit.load()).thenAnswer((_) async {});
    });

    testWidgets('shows pending message without code', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: VoucherDetailScreen(
            voucherId: 'reward-1',
            cubit: cubit,
          ),
        ),
      );
      await tester.pump();

      expect(find.textContaining('đang chờ mã voucher'), findsOneWidget);
      expect(find.text('Xuất trình mã tại quầy'), findsNothing);
      expect(find.text('Đổi quà ngay'), findsNothing);
    });
  });
}
