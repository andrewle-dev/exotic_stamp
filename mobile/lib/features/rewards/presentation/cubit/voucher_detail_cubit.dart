import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_voucher_detail_usecase.dart';
import '../../domain/usecases/voucher_redemption_usecase.dart';
import 'voucher_detail_state.dart';

class VoucherDetailCubit extends Cubit<VoucherDetailState> {
  VoucherDetailCubit({
    required GetVoucherDetailUseCase getVoucherDetailUseCase,
    required RedeemVoucherUseCase redeemVoucherUseCase,
    required String voucherId,
  })  : _getVoucherDetailUseCase = getVoucherDetailUseCase,
        _redeemVoucherUseCase = redeemVoucherUseCase,
        _voucherId = voucherId,
        super(const VoucherDetailState());

  final GetVoucherDetailUseCase _getVoucherDetailUseCase;
  final RedeemVoucherUseCase _redeemVoucherUseCase;
  final String _voucherId;

  Future<void> load() async {
    emit(
      state.copyWith(
        status: VoucherDetailStatus.loading,
        clearFailure: true,
      ),
    );
    try {
      final detail = await _getVoucherDetailUseCase(id: _voucherId);
      emit(
        state.copyWith(
          status: VoucherDetailStatus.loaded,
          detail: detail,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: VoucherDetailStatus.failure,
          failure: failure,
          clearDetail: true,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: VoucherDetailStatus.failure,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể tải chi tiết voucher.',
          ),
          clearDetail: true,
        ),
      );
    }
  }

  /// Confirms redemption via repository — never marks used locally only.
  Future<void> redeem() async {
    final current = state.detail;
    if (current == null || !current.canRedeem) {
      return;
    }

    emit(
      state.copyWith(
        status: VoucherDetailStatus.redeeming,
        clearFailure: true,
      ),
    );

    try {
      final detail = await _redeemVoucherUseCase(id: _voucherId);
      emit(
        state.copyWith(
          status: VoucherDetailStatus.loaded,
          detail: detail,
          clearFailure: true,
        ),
      );
    } on Failure catch (failure) {
      emit(
        state.copyWith(
          status: VoucherDetailStatus.loaded,
          failure: failure,
        ),
      );
    } catch (_) {
      emit(
        state.copyWith(
          status: VoucherDetailStatus.loaded,
          failure: const Failure(
            code: FailureCode.unknown,
            message: 'Không thể đổi quà voucher.',
          ),
        ),
      );
    }
  }
}
