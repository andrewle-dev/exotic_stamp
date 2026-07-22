import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/user_reward.dart';
import 'package:metro_stamp_app/features/rewards/domain/entities/voucher_detail.dart';
import 'package:metro_stamp_app/features/rewards/domain/repositories/rewards_repository.dart';
import 'package:metro_stamp_app/features/rewards/domain/usecases/get_voucher_detail_usecase.dart';
import 'package:metro_stamp_app/features/rewards/domain/usecases/voucher_redemption_usecase.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/voucher_detail_cubit.dart';
import 'package:metro_stamp_app/features/rewards/presentation/cubit/voucher_detail_state.dart';
import 'package:mocktail/mocktail.dart';

class MockRewardsRepository extends Mock implements RewardsRepository {}

void main() {
  late MockRewardsRepository repository;
  late VoucherDetailCubit cubit;

  setUp(() {
    repository = MockRewardsRepository();
    cubit = VoucherDetailCubit(
      getVoucherDetailUseCase: GetVoucherDetailUseCase(repository),
      redeemVoucherUseCase: RedeemVoucherUseCase(repository),
      voucherId: 'reward-1',
    );
  });

  tearDown(() => cubit.close());

  blocTest<VoucherDetailCubit, VoucherDetailState>(
    'loads available voucher detail',
    build: () {
      when(() => repository.getVoucherDetail(id: 'reward-1')).thenAnswer(
        (_) async => const VoucherDetail(
          id: 'reward-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.available,
          voucherCode: 'ABC-123',
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.detail?.status, UserRewardStatus.available);
      expect(cubit.state.detail?.voucherCode, 'ABC-123');
    },
  );

  blocTest<VoucherDetailCubit, VoucherDetailState>(
    'loads used voucher detail',
    build: () {
      when(() => repository.getVoucherDetail(id: 'reward-1')).thenAnswer(
        (_) async => VoucherDetail(
          id: 'reward-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.used,
          redeemedAt: DateTime(2026, 6, 20),
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.detail?.status, UserRewardStatus.used);
    },
  );

  blocTest<VoucherDetailCubit, VoucherDetailState>(
    'loads pending voucher without code',
    build: () {
      when(() => repository.getVoucherDetail(id: 'reward-1')).thenAnswer(
        (_) async => const VoucherDetail(
          id: 'reward-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.pending,
        ),
      );
      return cubit;
    },
    act: (cubit) => cubit.load(),
    verify: (_) {
      expect(cubit.state.detail?.status, UserRewardStatus.pending);
      expect(cubit.state.detail?.voucherCode, isNull);
    },
  );

  blocTest<VoucherDetailCubit, VoucherDetailState>(
    'redeem is no-op while online redeem is disabled',
    build: () {
      when(() => repository.getVoucherDetail(id: 'reward-1')).thenAnswer(
        (_) async => const VoucherDetail(
          id: 'reward-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.available,
          voucherCode: 'ABC-123',
        ),
      );
      when(() => repository.redeemVoucher(id: 'reward-1')).thenAnswer(
        (_) async => VoucherDetail(
          id: 'reward-1',
          rewardTitle: 'Coffee Voucher',
          status: UserRewardStatus.used,
          redeemedAt: DateTime(2026, 6, 22),
        ),
      );
      return cubit;
    },
    seed: () => const VoucherDetailState(
      status: VoucherDetailStatus.loaded,
      detail: VoucherDetail(
        id: 'reward-1',
        rewardTitle: 'Coffee Voucher',
        status: UserRewardStatus.available,
        voucherCode: 'ABC-123',
      ),
    ),
    act: (cubit) => cubit.redeem(),
    verify: (_) {
      expect(cubit.state.detail?.status, UserRewardStatus.available);
      verifyNever(() => repository.redeemVoucher(id: 'reward-1'));
    },
  );
}
