import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:share_plus/share_plus.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../domain/entities/reward_unlocked_share_payload.dart';
import '../widgets/reward_unlocked_sections.dart';

class RewardUnlockedShareScreen extends StatelessWidget {
  const RewardUnlockedShareScreen({
    super.key,
    this.payload,
  });

  final RewardUnlockedSharePayload? payload;

  @override
  Widget build(BuildContext context) {
    final resolved = payload ??
        GoRouterState.of(context).extra as RewardUnlockedSharePayload?;

    if (resolved == null) {
      return Scaffold(
        backgroundColor: AppColors.backgroundWhite,
        appBar: AppBar(
          backgroundColor: AppColors.backgroundWhite,
          foregroundColor: AppColors.textPrimary,
          title: const Text('Phần thưởng'),
          leading: IconButton(
            icon: const Icon(Icons.close_rounded),
            onPressed: () => context.pop(),
          ),
        ),
        body: const Center(
          child: Text('Không có dữ liệu phần thưởng để hiển thị.'),
        ),
      );
    }

    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.close_rounded),
          onPressed: () => context.pop(),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.xxl),
          child: Column(
            children: [
              const Spacer(),
              const RewardUnlockedCelebrationHeader(),
              const SizedBox(height: AppSpacing.xxl),
              RewardUnlockedCard(payload: resolved),
              const Spacer(),
              AppButton(
                label: 'Chia sẻ phần thưởng',
                variant: AppButtonVariant.accent,
                onPressed: () => _shareReward(resolved),
                icon: const Icon(
                  Icons.ios_share_rounded,
                  color: AppColors.backgroundWhite,
                  size: 20,
                ),
              ),
              const SizedBox(height: AppSpacing.md),
              AppButton(
                label: 'Xem phần thưởng',
                variant: AppButtonVariant.outlined,
                onPressed: () {
                  context.push(RouteNames.voucherDetail(resolved.rewardId));
                },
                icon: const Icon(
                  Icons.card_giftcard_outlined,
                  color: AppColors.primaryBlue,
                  size: 20,
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _shareReward(RewardUnlockedSharePayload payload) async {
    final message = StringBuffer('Tôi vừa mở khóa ${payload.displayTitle}!');
    if (payload.partnerName != null) {
      message.write(' tại ${payload.partnerName}');
    }
    message.write(' trên Exotic Stamp.');
    if (payload.voucherCode != null) {
      message.write(' Mã: ${payload.voucherCode}');
    }
    await Share.share(message.toString());
  }
}
