import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../shared/widgets/app_text_field.dart';

class PhotoShareEditorControls extends StatelessWidget {
  const PhotoShareEditorControls({
    super.key,
    required this.captionController,
    required this.showStationName,
    required this.showCollectionDate,
    required this.onShowStationNameChanged,
    required this.onShowCollectionDateChanged,
    this.hasStampContext = false,
  });

  final TextEditingController captionController;
  final bool showStationName;
  final bool showCollectionDate;
  final ValueChanged<bool> onShowStationNameChanged;
  final ValueChanged<bool> onShowCollectionDateChanged;
  final bool hasStampContext;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (hasStampContext) ...[
          Row(
            children: [
              const Icon(
                Icons.camera_alt_outlined,
                color: AppColors.primaryBlue,
                size: 20,
              ),
              const SizedBox(width: AppSpacing.xs),
              Text(
                'Stamp đã chọn',
                style: AppTextStyles.titleMedium.copyWith(
                  color: AppColors.primaryBlue,
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.sm),
        ],
        Text(
          'LỜI TỰA KỶ NIỆM',
          style: AppTextStyles.caption.copyWith(
            color: AppColors.textSecondary,
            fontWeight: FontWeight.w700,
            letterSpacing: 0.8,
          ),
        ),
        const SizedBox(height: AppSpacing.xs),
        AppTextField(
          controller: captionController,
          hint: 'Viết lời tựa cho kỷ niệm của bạn...',
          keyboardType: TextInputType.multiline,
          textInputAction: TextInputAction.newline,
        ),
        if (hasStampContext) ...[
          const SizedBox(height: AppSpacing.lg),
          _ToggleTile(
            title: 'Hiển thị tên nhà ga',
            subtitle: 'Tên ga sẽ xuất hiện trên Stamp sticker',
            value: showStationName,
            onChanged: onShowStationNameChanged,
          ),
          const SizedBox(height: AppSpacing.sm),
          _ToggleTile(
            title: 'Hiển thị ngày nhận',
            subtitle: 'Đánh dấu thời gian bạn nhận được Stamp',
            value: showCollectionDate,
            onChanged: onShowCollectionDateChanged,
          ),
        ],
      ],
    );
  }
}

class _ToggleTile extends StatelessWidget {
  const _ToggleTile({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.xs,
      ),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border),
      ),
      child: SwitchListTile(
        contentPadding: EdgeInsets.zero,
        title: Text(title, style: AppTextStyles.titleMedium),
        subtitle: Text(
          subtitle,
          style: AppTextStyles.caption.copyWith(
            color: AppColors.textSecondary,
          ),
        ),
        value: value,
        activeThumbColor: AppColors.backgroundWhite,
        activeTrackColor: AppColors.primaryBlue,
        onChanged: onChanged,
      ),
    );
  }
}
