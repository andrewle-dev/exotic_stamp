import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/usecases/get_voucher_detail_usecase.dart';
import 'voucher_detail_state.dart';

/// MVP: voucher redemption via API is disabled (410 REDEEM_NOT_SUPPORTED).
/// This cubit only loads backend detail — it never calls redeem.
class VoucherDetailCubit extends Cubit<VoucherDetailState> {
  VoucherDetailCubit({
    required GetVoucherDetailUseCase getVoucherDetailUseCase,
    required String voucherId,
  })  : _getVoucherDetailUseCase = getVoucherDetailUseCase,
        _voucherId = voucherId,
        super(const VoucherDetailState());

  final GetVoucherDetailUseCase _getVoucherDetailUseCase;
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
}
