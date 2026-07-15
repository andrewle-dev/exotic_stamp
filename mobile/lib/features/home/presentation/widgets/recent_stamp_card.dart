import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/home_summary.dart';
import '../utils/recent_stamp_time_label.dart';

class RecentStampCard extends StatelessWidget {
  const RecentStampCard({
    super.key,
    required this.stamp,
    this.mediaUrlResolver,
  });

  final RecentStamp stamp;
  final MediaUrlResolver? mediaUrlResolver;

  MediaUrlResolver get _resolver => mediaUrlResolver ?? MediaUrlResolver();

  @override
  Widget build(BuildContext context) {
    final imageUrl = _resolver.resolve(stamp.stampDesignUrl);

    return SizedBox(
      width: 88,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 88,
            height: 88,
            decoration: BoxDecoration(
              color: AppColors.blueTint,
              borderRadius: AppRadius.lgAll,
              border: Border.all(
                color: AppColors.primaryBlue.withValues(alpha: 0.35),
                width: 1.5,
              ),
              image: imageUrl == null
                  ? null
                  : DecorationImage(
                      image: NetworkImage(imageUrl),
                      fit: BoxFit.cover,
                    ),
            ),
            child: Stack(
              children: [
                if (imageUrl == null)
                  const Center(
                    child: Icon(
                      Icons.verified_outlined,
                      color: AppColors.primaryBlue,
                      size: 28,
                    ),
                  ),
                Positioned(
                  top: 6,
                  right: 6,
                  child: Container(
                    width: 22,
                    height: 22,
                    decoration: BoxDecoration(
                      color: AppColors.backgroundWhite,
                      shape: BoxShape.circle,
                      border: Border.all(color: AppColors.primaryBlue),
                    ),
                    child: const Icon(
                      Icons.check_rounded,
                      color: AppColors.primaryBlue,
                      size: 14,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            stamp.stationName,
            style: AppTextStyles.caption.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
          Text(
            formatRecentStampTime(stamp.collectedAt),
            style: AppTextStyles.caption.copyWith(
              color: AppColors.textSecondary,
              fontSize: 11,
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
