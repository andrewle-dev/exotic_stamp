import 'package:flutter/material.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';

/// Static help content for normal users.
class HelpCenterScreen extends StatelessWidget {
  const HelpCenterScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: const AppSecondaryAppBar(
        title: 'Help Center',
        fallbackRoute: RouteNames.profile,
      ),
      body: SafeArea(
        top: false,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(
            AppSpacing.xl,
            AppSpacing.xxl,
            AppSpacing.xl,
            AppSpacing.huge,
          ),
          children: const [
            _HelpCard(
              title: 'Collecting stamps',
              body:
                  'Tap your phone on an NFC tag at a Metro Stamp station. '
                  'We check your location near the station before collecting '
                  'a stamp.',
            ),
            SizedBox(height: AppSpacing.lg),
            _HelpCard(
              title: 'Rewards & vouchers',
              body:
                  'Milestones and vouchers unlock as you collect stamps. '
                  'Open Rewards to view available vouchers and redeem codes.',
            ),
            SizedBox(height: AppSpacing.lg),
            _HelpCard(
              title: 'Account & security',
              body:
                  'Update your name in Personal Information. '
                  'Use Privacy & Security to change your password or log out '
                  'of this device or all devices.',
            ),
            SizedBox(height: AppSpacing.lg),
            _HelpCard(
              title: 'Need more help?',
              body:
                  'If something looks wrong after a scan or login, try again '
                  'on a stable network. Contact support through the official '
                  'Exotic Stamp channels if the issue continues.',
            ),
          ],
        ),
      ),
    );
  }
}

class _HelpCard extends StatelessWidget {
  const _HelpCard({required this.title, required this.body});

  final String title;
  final String body;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: AppRadius.xlAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: AppTextStyles.titleMedium.copyWith(
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            body,
            style: AppTextStyles.bodyMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}
