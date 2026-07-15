import '../../domain/entities/voucher_detail.dart';
import '../../domain/repositories/rewards_repository.dart';

class RedeemVoucherUseCase {
  const RedeemVoucherUseCase(this._repository);

  final RewardsRepository _repository;

  Future<VoucherDetail> call({required String id}) {
    return _repository.redeemVoucher(id: id);
  }
}
