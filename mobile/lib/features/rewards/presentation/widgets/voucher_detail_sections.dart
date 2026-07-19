import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/voucher_detail.dart';
import '../../domain/entities/user_reward.dart';

class VoucherDetailHero extends StatelessWidget {
  const VoucherDetailHero({
    super.key,
    required this.detail,
  });

  final VoucherDetail detail;

  @override
  Widget build(BuildContext context) {
    final mediaResolver = MediaUrlResolver();
    final imageUrl = mediaResolver.resolve(detail.rewardImageUrl);
    final partner = detail.partnerName ?? 'Đối tác';

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            AppColors.primaryBlue,
            AppColors.primaryBlue.withValues(alpha: 0.85),
          ],
        ),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 16,
            offset: Offset(0, 6),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.xl),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(999),
                  child: SizedBox(
                    width: 48,
                    height: 48,
                    child: imageUrl != null
                        ? CachedNetworkImage(
                            imageUrl: imageUrl,
                            fit: BoxFit.cover,
                          )
                        : ColoredBox(
                            color: AppColors.backgroundWhite.withValues(
                              alpha: 0.2,
                            ),
                            child: const Icon(
                              Icons.storefront_outlined,
                              color: AppColors.backgroundWhite,
                            ),
                          ),
                  ),
                ),
                const SizedBox(width: AppSpacing.lg),
                Expanded(
                  child: Text(
                    partner,
                    style: AppTextStyles.titleMedium.copyWith(
                      color: AppColors.backgroundWhite,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: AppSpacing.xl),
            Text(
              detail.displayOfferTitle,
              style: AppTextStyles.sectionTitle.copyWith(
                color: AppColors.backgroundWhite,
                fontWeight: FontWeight.w800,
              ),
            ),
            if (detail.rewardDescription != null) ...[
              const SizedBox(height: AppSpacing.md),
              Text(
                detail.rewardDescription!,
                style: AppTextStyles.bodyMedium.copyWith(
                  color: AppColors.backgroundWhite.withValues(alpha: 0.9),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class VoucherUnlockConditionBanner extends StatelessWidget {
  const VoucherUnlockConditionBanner({
    super.key,
    required this.condition,
  });

  final String condition;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.primaryBlue.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          const Icon(
            Icons.lock_open_rounded,
            color: AppColors.primaryBlue,
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Điều kiện mở khóa',
                  style: AppTextStyles.caption.copyWith(
                    color: AppColors.textSecondary,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                Text(
                  condition,
                  style: AppTextStyles.titleMedium.copyWith(
                    color: AppColors.primaryBlue,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class VoucherCodeQrSection extends StatelessWidget {
  const VoucherCodeQrSection({
    super.key,
    required this.code,
    this.onCopy,
  });

  final String code;
  final VoidCallback? onCopy;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          Container(
            width: 120,
            height: 120,
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.border),
            ),
            child: Icon(
              Icons.qr_code_2_rounded,
              size: 72,
              color: AppColors.primaryBlue.withValues(alpha: 0.85),
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            'Redeem code',
            style: AppTextStyles.labelLarge.copyWith(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            'Xuất trình mã tại đối tác. Đổi quà online chưa được hỗ trợ.',
            textAlign: TextAlign.center,
            style: AppTextStyles.caption.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.lg),
          Text(
            'Mã đổi quà',
            style: AppTextStyles.caption.copyWith(
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            code,
            style: AppTextStyles.headlineMedium.copyWith(
              color: AppColors.primaryBlue,
              letterSpacing: 1.2,
            ),
          ),
          if (onCopy != null) ...[
            const SizedBox(height: AppSpacing.lg),
            TextButton.icon(
              onPressed: onCopy,
              icon: const Icon(Icons.copy_rounded, size: 18),
              label: const Text('Sao chép mã'),
            ),
          ],
        ],
      ),
    );
  }
}

class VoucherTermsSection extends StatelessWidget {
  const VoucherTermsSection({super.key, required this.terms});

  final List<String> terms;

  @override
  Widget build(BuildContext context) {
    if (terms.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Điều khoản sử dụng',
          style: AppTextStyles.titleMedium.copyWith(
            color: AppColors.primaryBlue,
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        ...terms.map(
          (term) => Padding(
            padding: const EdgeInsets.only(bottom: AppSpacing.sm),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('•  '),
                Expanded(
                  child: Text(
                    term,
                    style: AppTextStyles.bodyMedium.copyWith(
                      color: AppColors.textSecondary,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class RelatedVouchersSection extends StatelessWidget {
  const RelatedVouchersSection({
    super.key,
    required this.vouchers,
  });

  final List<RelatedVoucherSummary> vouchers;

  @override
  Widget build(BuildContext context) {
    if (vouchers.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Voucher liên quan',
          style: AppTextStyles.titleMedium.copyWith(
            color: AppColors.primaryBlue,
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        ...vouchers.map(
          (voucher) => _RelatedVoucherTile(
            voucher: voucher,
            onTap: () => context.push(RouteNames.voucherDetail(voucher.id)),
          ),
        ),
      ],
    );
  }
}

class _RelatedVoucherTile extends StatelessWidget {
  const _RelatedVoucherTile({
    required this.voucher,
    required this.onTap,
  });

  final RelatedVoucherSummary voucher;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final (label, color) = switch (voucher.status) {
      UserRewardStatus.available => ('Khả dụng', AppColors.primaryBlue),
      UserRewardStatus.used => ('Đã dùng', AppColors.textSecondary),
      UserRewardStatus.expired => ('Hết hạn', AppColors.accentRed),
      UserRewardStatus.pending => ('Đang chờ', AppColors.accentRed),
      UserRewardStatus.unavailable => ('Không khả dụng', AppColors.textSecondary),
    };

    return ListTile(
      contentPadding: EdgeInsets.zero,
      onTap: onTap,
      leading: CircleAvatar(
        backgroundColor: AppColors.blueTint,
        child: Icon(
          Icons.local_offer_outlined,
          color: AppColors.primaryBlue.withValues(alpha: 0.85),
        ),
      ),
      title: Text(voucher.title, style: AppTextStyles.bodyLarge),
      subtitle: voucher.subtitle != null
          ? Text(
              voucher.subtitle!,
              style: AppTextStyles.caption.copyWith(
                color: AppColors.textSecondary,
              ),
            )
          : null,
      trailing: Text(
        label,
        style: AppTextStyles.caption.copyWith(
          color: color,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class VoucherStatusBanner extends StatelessWidget {
  const VoucherStatusBanner({super.key, required this.status});

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
      padding: const EdgeInsets.all(AppSpacing.lg),
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

String formatVoucherDate(DateTime value) {
  final local = value.toLocal();
  final day = local.day.toString().padLeft(2, '0');
  final month = local.month.toString().padLeft(2, '0');
  final year = local.year.toString();
  return '$day/$month/$year';
}
