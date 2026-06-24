import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/line.dart';
import '../../domain/entities/station.dart';
import '../../domain/entities/station_collected_status.dart';

class StationListTile extends StatelessWidget {
  const StationListTile({
    super.key,
    required this.station,
    required this.onTap,
    this.distanceLabel,
    this.mediaUrlResolver,
  });

  final Station station;
  final VoidCallback onTap;
  final String? distanceLabel;
  final MediaUrlResolver? mediaUrlResolver;

  MediaUrlResolver get _resolver => mediaUrlResolver ?? MediaUrlResolver();

  @override
  Widget build(BuildContext context) {
    final imageUrl = _resolver.resolve(
      station.stampPreviewUrl ?? station.imageUrl,
    );

    return Material(
      color: AppColors.backgroundWhite,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.sm,
          ),
          child: Row(
            children: [
              _StationAvatar(
                imageUrl: imageUrl,
                collectedStatus: station.collectedStatus,
              ),
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      station.label,
                      style: AppTextStyles.titleMedium,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: AppSpacing.xxs),
                    Text(
                      station.code,
                      style: AppTextStyles.bodyMedium,
                    ),
                    if (distanceLabel != null) ...[
                      const SizedBox(height: AppSpacing.xxs),
                      Text(
                        distanceLabel!,
                        style: AppTextStyles.caption.copyWith(
                          color: AppColors.primaryBlue,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
              if (station.collectedStatus != StationCollectedStatus.unknown)
                _CollectedBadge(collectedStatus: station.collectedStatus),
              const Icon(
                Icons.chevron_right_rounded,
                color: AppColors.textSecondary,
              ),
            ],
          ),
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

  bool get _isCollected => collectedStatus == StationCollectedStatus.collected;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 52,
      height: 52,
      decoration: BoxDecoration(
        color: _isCollected ? AppColors.blueTint : AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(
          color: _isCollected ? AppColors.primaryBlue : AppColors.border,
        ),
        image: imageUrl == null
            ? null
            : DecorationImage(
                image: NetworkImage(imageUrl!),
                fit: BoxFit.cover,
              ),
      ),
      child: imageUrl == null
          ? Icon(
              _isCollected ? Icons.verified : Icons.train_outlined,
              color: _isCollected
                  ? AppColors.primaryBlue
                  : AppColors.textSecondary,
            )
          : null,
    );
  }
}

class _CollectedBadge extends StatelessWidget {
  const _CollectedBadge({required this.collectedStatus});

  final StationCollectedStatus collectedStatus;

  bool get _isCollected => collectedStatus == StationCollectedStatus.collected;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(right: AppSpacing.xs),
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.xs,
        vertical: AppSpacing.xxs,
      ),
      decoration: BoxDecoration(
        color: _isCollected ? AppColors.blueTint : AppColors.surface,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(
          color: _isCollected ? AppColors.primaryBlue : AppColors.border,
        ),
      ),
      child: Text(
        _isCollected ? 'Đã thu' : 'Chưa thu',
        style: AppTextStyles.caption.copyWith(
          color: _isCollected ? AppColors.primaryBlue : AppColors.textSecondary,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class LineFilterChips extends StatelessWidget {
  const LineFilterChips({
    super.key,
    required this.lines,
    required this.selectedLineId,
    required this.onSelected,
  });

  final List<Line> lines;
  final String? selectedLineId;
  final ValueChanged<String> onSelected;

  @override
  Widget build(BuildContext context) {
    if (lines.isEmpty) {
      return const SizedBox.shrink();
    }

    return SizedBox(
      height: 40,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg),
        itemCount: lines.length,
        separatorBuilder: (_, __) => const SizedBox(width: AppSpacing.xs),
        itemBuilder: (context, index) {
          final line = lines[index];
          final isSelected = line.id == selectedLineId;
          return ChoiceChip(
            label: Text(line.label),
            selected: isSelected,
            onSelected: (_) => onSelected(line.id),
            selectedColor: AppColors.primaryBlue,
            backgroundColor: AppColors.surface,
            labelStyle: AppTextStyles.caption.copyWith(
              color: isSelected
                  ? AppColors.backgroundWhite
                  : AppColors.textPrimary,
              fontWeight: FontWeight.w600,
            ),
            side: const BorderSide(color: AppColors.border),
            showCheckmark: false,
          );
        },
      ),
    );
  }
}
