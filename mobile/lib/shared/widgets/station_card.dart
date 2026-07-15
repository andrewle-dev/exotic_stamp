import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';
import '../../app/theme/app_radius.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_text_styles.dart';
import '../../features/stations/domain/entities/station_collected_status.dart';
import 'app_card.dart';

/// List card for a metro station with line badge and collected indicator.
class StationCard extends StatelessWidget {
  const StationCard({
    super.key,
    required this.stationName,
    required this.onTap,
    this.lineLabel,
    this.stationCode,
    this.distanceLabel,
    this.imageUrl,
    this.collectedStatus = StationCollectedStatus.unknown,
  });

  final String stationName;
  final String? lineLabel;
  final String? stationCode;
  final String? distanceLabel;
  final String? imageUrl;
  final StationCollectedStatus collectedStatus;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(AppSpacing.lg),
      margin: const EdgeInsets.only(bottom: AppSpacing.md),
      child: Row(
        children: [
          _StationAvatar(
            imageUrl: imageUrl,
            collectedStatus: collectedStatus,
          ),
          const SizedBox(width: AppSpacing.lg),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (lineLabel != null) ...[
                  _LineBadge(label: lineLabel!),
                  const SizedBox(height: AppSpacing.xs),
                ],
                Text(
                  stationName,
                  style: AppTextStyles.cardTitle,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                if (stationCode != null) ...[
                  const SizedBox(height: AppSpacing.xs),
                  Text(stationCode!, style: AppTextStyles.bodyMedium),
                ],
                if (distanceLabel != null) ...[
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    distanceLabel!,
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.primaryBlue,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ],
            ),
          ),
          if (collectedStatus != StationCollectedStatus.unknown)
            _CollectedIndicator(status: collectedStatus),
          const Icon(
            Icons.chevron_right_rounded,
            color: AppColors.textSecondary,
          ),
        ],
      ),
    );
  }
}

class _LineBadge extends StatelessWidget {
  const _LineBadge({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.sm,
        vertical: 2,
      ),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: AppRadius.mdAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Text(
        label,
        style: AppTextStyles.caption.copyWith(
          color: AppColors.primaryBlue,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _StationAvatar extends StatelessWidget {
  const _StationAvatar({
    required this.imageUrl,
    required this.collectedStatus,
  });

  final String? imageUrl;
  final StationCollectedStatus collectedStatus;

  @override
  Widget build(BuildContext context) {
    final borderColor = collectedStatus == StationCollectedStatus.collected
        ? AppColors.primaryBlue
        : AppColors.border;

    return Container(
      width: 52,
      height: 52,
      decoration: BoxDecoration(
        borderRadius: AppRadius.lgAll,
        border: Border.all(color: borderColor, width: 2),
        color: AppColors.surface,
      ),
      clipBehavior: Clip.antiAlias,
      child: imageUrl != null
          ? CachedNetworkImage(imageUrl: imageUrl!, fit: BoxFit.cover)
          : Icon(
              Icons.train_outlined,
              color: collectedStatus == StationCollectedStatus.collected
                  ? AppColors.primaryBlue
                  : AppColors.textSecondary,
            ),
    );
  }
}

class _CollectedIndicator extends StatelessWidget {
  const _CollectedIndicator({required this.status});

  final StationCollectedStatus status;

  @override
  Widget build(BuildContext context) {
    final collected = status == StationCollectedStatus.collected;
    return Padding(
      padding: const EdgeInsets.only(right: AppSpacing.sm),
      child: Icon(
        collected ? Icons.check_circle : Icons.radio_button_unchecked,
        color: collected ? AppColors.primaryBlue : AppColors.textSecondary,
        size: 20,
      ),
    );
  }
}
