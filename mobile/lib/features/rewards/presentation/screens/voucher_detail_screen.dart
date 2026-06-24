import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/entities/user_reward.dart';
import '../../domain/entities/voucher_detail.dart';
import '../../domain/usecases/get_voucher_detail_usecase.dart';
import '../cubit/voucher_detail_cubit.dart';
import '../cubit/voucher_detail_state.dart';

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
        child: const _VoucherDetailView(),
      );
    }

    return BlocProvider(
      create: (_) => VoucherDetailCubit(
        getVoucherDetailUseCase: GetVoucherDetailUseCase(
          Injection.instance.rewardsRepository,
        ),
        voucherId: voucherId,
      )..load(),
      child: const _VoucherDetailView(),
    );
  }
}

class _VoucherDetailView extends StatelessWidget {
  const _VoucherDetailView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        title: const Text('Chi tiết voucher'),
      ),
      body: BlocBuilder<VoucherDetailCubit, VoucherDetailState>(
        builder: (context, state) {
          switch (state.status) {
            case VoucherDetailStatus.initial:
            case VoucherDetailStatus.loading:
              return const AppLoadingView(
                message: 'Đang tải chi tiết voucher...',
              );
            case VoucherDetailStatus.failure:
              return AppErrorView(
                message:
                    state.failure?.message ?? 'Không thể tải chi tiết voucher.',
                onRetry: () => context.read<VoucherDetailCubit>().load(),
              );
            case VoucherDetailStatus.loaded:
              final detail = state.detail;
              if (detail == null) {
                return const AppLoadingView();
              }
              return _VoucherDetailBody(detail: detail);
          }
        },
      ),
    );
  }
}

class _VoucherDetailBody extends StatelessWidget {
  const _VoucherDetailBody({required this.detail});

  final VoucherDetail detail;

  static String _formatDate(DateTime value) {
    final local = value.toLocal();
    final day = local.day.toString().padLeft(2, '0');
    final month = local.month.toString().padLeft(2, '0');
    final year = local.year.toString();
    return '$day/$month/$year';
  }

  @override
  Widget build(BuildContext context) {
    final mediaResolver = MediaUrlResolver();
    final imageUrl = mediaResolver.resolve(detail.rewardImageUrl);
    final disabled = detail.status != UserRewardStatus.available;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppSpacing.xl),
      child: Opacity(
        opacity: disabled ? 0.7 : 1,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (imageUrl != null)
              ClipRRect(
                borderRadius: BorderRadius.circular(20),
                child: AspectRatio(
                  aspectRatio: 16 / 9,
                  child: CachedNetworkImage(
                    imageUrl: imageUrl,
                    fit: BoxFit.cover,
                  ),
                ),
              ),
            const SizedBox(height: AppSpacing.lg),
            Text(
              detail.rewardTitle,
              style: AppTextStyles.titleLarge.copyWith(
                color: AppColors.primaryBlue,
              ),
            ),
            if (detail.milestoneName != null) ...[
              const SizedBox(height: AppSpacing.xs),
              Text(detail.milestoneName!, style: AppTextStyles.bodyLarge),
            ],
            if (detail.rewardDescription != null) ...[
              const SizedBox(height: AppSpacing.sm),
              Text(detail.rewardDescription!, style: AppTextStyles.bodyMedium),
            ],
            const SizedBox(height: AppSpacing.lg),
            _StatusBanner(status: detail.status),
            const SizedBox(height: AppSpacing.lg),
            if (detail.status == UserRewardStatus.pending)
              Text(
                'Phần thưởng đang chờ mã voucher từ máy chủ. Vui lòng quay lại sau.',
                style: AppTextStyles.bodyMedium.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
            if (detail.showVoucherCode) ...[
              _CodeCard(code: detail.voucherCode!),
              const SizedBox(height: AppSpacing.md),
            ],
            if (detail.expiresAt != null)
              _InfoRow(
                label: 'Hết hạn',
                value: _formatDate(detail.expiresAt!),
              ),
            if (detail.redeemedAt != null)
              _InfoRow(
                label: 'Đã sử dụng',
                value: _formatDate(detail.redeemedAt!),
              ),
            const SizedBox(height: AppSpacing.xl),
            if (detail.showPresentAtCounterCta)
              ElevatedButton(
                onPressed: detail.showVoucherCode
                    ? () {
                        Clipboard.setData(
                          ClipboardData(text: detail.voucherCode!),
                        );
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text(
                              'Đã sao chép mã. Xuất trình mã tại quầy đối tác.',
                            ),
                          ),
                        );
                      }
                    : null,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryBlue,
                  foregroundColor: AppColors.backgroundWhite,
                  disabledBackgroundColor: AppColors.border,
                  padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
                ),
                child: const Text('Xuất trình mã tại quầy'),
              ),
          ],
        ),
      ),
    );
  }
}

class _StatusBanner extends StatelessWidget {
  const _StatusBanner({required this.status});

  final UserRewardStatus status;

  @override
  Widget build(BuildContext context) {
    final (label, color) = switch (status) {
      UserRewardStatus.available => ('Voucher khả dụng', AppColors.primaryBlue),
      UserRewardStatus.used => ('Voucher đã sử dụng', AppColors.textSecondary),
      UserRewardStatus.expired => ('Voucher đã hết hạn', AppColors.accentRed),
      UserRewardStatus.pending => ('Đang chờ mã voucher', AppColors.accentRed),
      UserRewardStatus.unavailable => (
          'Voucher không khả dụng',
          AppColors.textSecondary
        ),
    };

    return Container(
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: color.withValues(alpha: 0.35)),
      ),
      child: Text(
        label,
        style: AppTextStyles.bodyMedium.copyWith(
          color: color,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _CodeCard extends StatelessWidget {
  const _CodeCard({required this.code});

  final String code;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.primaryBlue),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Mã voucher',
            style: AppTextStyles.caption.copyWith(
              color: AppColors.primaryBlue,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            code,
            style: AppTextStyles.headlineMedium.copyWith(
              color: AppColors.primaryBlue,
              letterSpacing: 1.5,
            ),
          ),
        ],
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
      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
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
