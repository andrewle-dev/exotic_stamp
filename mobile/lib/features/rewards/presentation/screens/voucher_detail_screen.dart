import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/entities/user_reward.dart';
import '../../domain/entities/voucher_detail.dart';
import '../../domain/usecases/get_voucher_detail_usecase.dart';
import '../../domain/usecases/voucher_redemption_usecase.dart';
import '../cubit/voucher_detail_cubit.dart';
import '../cubit/voucher_detail_state.dart';
import '../widgets/voucher_detail_sections.dart';

class VoucherDetailScreen extends StatelessWidget {
  const VoucherDetailScreen({
    required this.voucherId,
    super.key,
    this.cubit,
  });

  final String voucherId;
  final VoucherDetailCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<VoucherDetailCubit>.value(
        value: cubit!,
        child: _VoucherDetailView(voucherId: voucherId),
      );
    }

    return BlocProvider(
      create: (_) => VoucherDetailCubit(
        getVoucherDetailUseCase: GetVoucherDetailUseCase(
          Injection.instance.rewardsRepository,
        ),
        redeemVoucherUseCase: RedeemVoucherUseCase(
          Injection.instance.rewardsRepository,
        ),
        voucherId: voucherId,
      )..load(),
      child: _VoucherDetailView(voucherId: voucherId),
    );
  }
}

class _VoucherDetailView extends StatelessWidget {
  const _VoucherDetailView({required this.voucherId});

  final String voucherId;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        title: const Text('Chi tiết Voucher'),
      ),
      body: BlocConsumer<VoucherDetailCubit, VoucherDetailState>(
        listenWhen: (previous, current) =>
            previous.failure != current.failure && current.failure != null,
        listener: (context, state) {
          final failure = state.failure;
          if (failure == null) {
            return;
          }
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(failure.message)),
          );
        },
        builder: (context, state) {
          switch (state.status) {
            case VoucherDetailStatus.initial:
            case VoucherDetailStatus.loading:
              return const AppLoadingView(
                message: 'Đang tải chi tiết voucher...',
              );
            case VoucherDetailStatus.redeeming:
            case VoucherDetailStatus.loaded:
              final detail = state.detail;
              if (detail == null) {
                return const AppLoadingView();
              }
              return _VoucherDetailBody(
                detail: detail,
                isRedeeming: state.status == VoucherDetailStatus.redeeming,
              );
            case VoucherDetailStatus.failure:
              return AppErrorView(
                message:
                    state.failure?.message ?? 'Không thể tải chi tiết voucher.',
                onRetry: () => context.read<VoucherDetailCubit>().load(),
              );
          }
        },
      ),
    );
  }
}

class _VoucherDetailBody extends StatelessWidget {
  const _VoucherDetailBody({
    required this.detail,
    required this.isRedeeming,
  });

  final VoucherDetail detail;
  final bool isRedeeming;

  void _saveVoucher(BuildContext context) {
    if (detail.voucherCode != null) {
      Clipboard.setData(ClipboardData(text: detail.voucherCode!));
    }
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Đã lưu voucher vào bộ nhớ tạm.')),
    );
  }

  Future<void> _redeem(BuildContext context) async {
    await context.read<VoucherDetailCubit>().redeem();
    if (!context.mounted) {
      return;
    }
    final next = context.read<VoucherDetailCubit>().state;
    if (next.detail?.status == UserRewardStatus.used) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đổi quà thành công!')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final disabled = detail.status != UserRewardStatus.available;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppSpacing.xxl),
      child: Opacity(
        opacity: disabled ? 0.75 : 1,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            VoucherDetailHero(detail: detail),
            const SizedBox(height: AppSpacing.xl),
            VoucherStatusBanner(status: detail.status),
            const SizedBox(height: AppSpacing.xl),
            if (detail.unlockCondition != null)
              VoucherUnlockConditionBanner(
                condition: detail.unlockCondition!,
              ),
            if (detail.unlockCondition != null)
              const SizedBox(height: AppSpacing.xl),
            if (detail.status == UserRewardStatus.pending)
              Text(
                'Phần thưởng đang chờ mã voucher từ máy chủ. Vui lòng quay lại sau.',
                style: AppTextStyles.bodyMedium.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            if (detail.showVoucherCode) ...[
              VoucherCodeQrSection(
                code: detail.voucherCode!,
                onCopy: () {
                  Clipboard.setData(
                    ClipboardData(text: detail.voucherCode!),
                  );
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Đã sao chép mã voucher.')),
                  );
                },
              ),
              const SizedBox(height: AppSpacing.xl),
            ],
            if (detail.expiresAt != null)
              _InfoRow(
                label: 'Hết hạn',
                value: formatVoucherDate(detail.expiresAt!),
              ),
            if (detail.redeemedAt != null)
              _InfoRow(
                label: 'Đã sử dụng',
                value: formatVoucherDate(detail.redeemedAt!),
              ),
            if (detail.terms.isNotEmpty) ...[
              const SizedBox(height: AppSpacing.xl),
              VoucherTermsSection(terms: detail.terms),
            ],
            if (detail.relatedVouchers.isNotEmpty) ...[
              const SizedBox(height: AppSpacing.xxl),
              RelatedVouchersSection(vouchers: detail.relatedVouchers),
            ],
            const SizedBox(height: AppSpacing.xxl),
            if (detail.showVoucherCode)
              OutlinedButton(
                onPressed: disabled || isRedeeming ? null : () => _saveVoucher(context),
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.primaryBlue,
                  side: const BorderSide(color: AppColors.primaryBlue),
                  padding: const EdgeInsets.symmetric(vertical: AppSpacing.lg),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(999),
                  ),
                ),
                child: const Text('Lưu Voucher'),
              ),
            if (detail.showRedeemCta) const SizedBox(height: AppSpacing.md),
            if (detail.showRedeemCta)
              ElevatedButton(
                onPressed: isRedeeming ? null : () => _redeem(context),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryBlue,
                  foregroundColor: AppColors.backgroundWhite,
                  disabledBackgroundColor: AppColors.border,
                  padding: const EdgeInsets.symmetric(vertical: AppSpacing.lg),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(999),
                  ),
                ),
                child: isRedeeming
                    ? const SizedBox(
                        height: 22,
                        width: 22,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: AppColors.backgroundWhite,
                        ),
                      )
                    : const Text('Đổi quà ngay'),
              ),
          ],
        ),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({
    required this.label,
    required this.value,
  });

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.md),
      child: Row(
        children: [
          SizedBox(
            width: 110,
            child: Text(label, style: AppTextStyles.bodyMedium),
          ),
          Expanded(
            child: Text(
              value,
              style: AppTextStyles.bodyLarge.copyWith(
                color: AppColors.textPrimary,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
