import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/home_summary.dart';

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
      width: 92,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 92,
            height: 92,
            decoration: BoxDecoration(
              color: AppColors.blueTint,
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: AppColors.border),
              image: imageUrl == null
                  ? null
                  : DecorationImage(
                      image: NetworkImage(imageUrl),
                      fit: BoxFit.cover,
                    ),
            ),
            child: imageUrl == null
                ? const Center(
                    child: Icon(
                      Icons.verified_outlined,
                      color: AppColors.primaryBlue,
                      size: 28,
                    ),
                  )
                : null,
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            stamp.stationName,
            style: AppTextStyles.caption.copyWith(
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
