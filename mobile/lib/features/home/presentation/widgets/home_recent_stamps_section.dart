import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../shared/widgets/app_empty_state.dart';
import '../../domain/entities/home_summary.dart';
import 'recent_stamp_card.dart';

class HomeRecentStampsSection extends StatelessWidget {
  const HomeRecentStampsSection({super.key, required this.stamps});

  final List<RecentStamp> stamps;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Icon(
              Icons.history_rounded,
              color: AppColors.primaryBlue,
              size: 20,
            ),
            const SizedBox(width: AppSpacing.sm),
            const Expanded(
              child: Text(
                'Recently Collected',
                style: AppTextStyles.titleMedium,
              ),
            ),
            TextButton(
              onPressed: () => context.go(RouteNames.stampBook),
              style: TextButton.styleFrom(
                foregroundColor: AppColors.primaryBlue,
                padding: EdgeInsets.zero,
                minimumSize: Size.zero,
                tapTargetSize: MaterialTapTargetSize.shrinkWrap,
              ),
              child: const Text(
                'View Book',
                style: TextStyle(fontWeight: FontWeight.w700),
              ),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.md),
        if (stamps.isEmpty)
          const AppEmptyState(
            title: 'Chưa có stamp',
            message: 'Chạm NFC tại ga metro để bắt đầu sưu tập stamp.',
            icon: Icons.collections_bookmark_outlined,
          )
        else
          SizedBox(
            height: 132,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: stamps.length,
              separatorBuilder: (_, __) => const SizedBox(width: AppSpacing.md),
              itemBuilder: (context, index) {
                return RecentStampCard(stamp: stamps[index]);
              },
            ),
          ),
      ],
    );
  }
}
