import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/stamp_share_option.dart';

class PhotoShareStampSelectorRow extends StatelessWidget {
  const PhotoShareStampSelectorRow({
    super.key,
    required this.options,
    required this.selectedStationId,
    required this.onSelected,
  });

  final List<StampShareOption> options;
  final String? selectedStationId;
  final ValueChanged<StampShareOption> onSelected;

  @override
  Widget build(BuildContext context) {
    if (options.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Chọn Stamp',
          style: AppTextStyles.titleMedium.copyWith(
            color: AppColors.primaryBlue,
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        SizedBox(
          height: 96,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: options.length,
            separatorBuilder: (_, __) => const SizedBox(width: AppSpacing.md),
            itemBuilder: (context, index) {
              final option = options[index];
              final selected = option.stationId == selectedStationId;
              return _StampOptionChip(
                option: option,
                selected: selected,
                onTap: () => onSelected(option),
              );
            },
          ),
        ),
      ],
    );
  }
}

class _StampOptionChip extends StatelessWidget {
  const _StampOptionChip({
    required this.option,
    required this.selected,
    required this.onTap,
  });

  final StampShareOption option;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final mediaResolver = MediaUrlResolver();
    final imageUrl = mediaResolver.resolve(option.stampDesignUrl);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        width: 80,
        padding: const EdgeInsets.all(AppSpacing.sm),
        decoration: BoxDecoration(
          color: selected ? AppColors.blueTint : AppColors.surface,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: selected ? AppColors.primaryBlue : AppColors.border,
            width: selected ? 2 : 1,
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: SizedBox(
                width: 44,
                height: 44,
                child: imageUrl != null
                    ? CachedNetworkImage(
                        imageUrl: imageUrl,
                        fit: BoxFit.cover,
                      )
                    : const ColoredBox(
                        color: AppColors.backgroundWhite,
                        child: Icon(
                          Icons.train_rounded,
                          color: AppColors.primaryBlue,
                          size: 24,
                        ),
                      ),
              ),
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(
              option.stationName,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              textAlign: TextAlign.center,
              style: AppTextStyles.caption.copyWith(
                fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                color: selected
                    ? AppColors.primaryBlue
                    : AppColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

enum PhotoSharePlatform {
  facebook('Facebook', Icons.facebook_rounded),
  instagram('Instagram', Icons.camera_alt_outlined),
  zalo('Zalo', Icons.chat_bubble_outline_rounded),
  copy('Sao chép', Icons.content_copy_rounded),
  other('Khác', Icons.more_horiz_rounded);

  const PhotoSharePlatform(this.label, this.icon);

  final String label;
  final IconData icon;

  String get trackingKey => name;
}

class PhotoSharePlatformChips extends StatelessWidget {
  const PhotoSharePlatformChips({
    super.key,
    required this.onPlatformSelected,
    this.enabled = true,
  });

  final ValueChanged<PhotoSharePlatform> onPlatformSelected;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Chia sẻ qua',
          style: AppTextStyles.titleMedium.copyWith(
            color: AppColors.primaryBlue,
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        Wrap(
          spacing: AppSpacing.md,
          runSpacing: AppSpacing.md,
          children: PhotoSharePlatform.values.map((platform) {
            return ActionChip(
              avatar: Icon(
                platform.icon,
                size: 18,
                color: enabled
                    ? AppColors.primaryBlue
                    : AppColors.textSecondary,
              ),
              label: Text(platform.label),
              onPressed: enabled ? () => onPlatformSelected(platform) : null,
              backgroundColor: AppColors.surface,
              side: const BorderSide(color: AppColors.border),
            );
          }).toList(),
        ),
      ],
    );
  }
}
