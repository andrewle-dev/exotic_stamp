import '../entities/voucher_detail.dart';
import '../repositories/rewards_repository.dart';

class GetVoucherDetailUseCase {
  const GetVoucherDetailUseCase(this._repository);

  final RewardsRepository _repository;

  Future<VoucherDetail> call({required String id}) {
    return _repository.getVoucherDetail(id: id);
  }
}
