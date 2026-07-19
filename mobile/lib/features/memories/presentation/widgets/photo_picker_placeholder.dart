import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';

class PhotoPickerPlaceholder extends StatelessWidget {
  const PhotoPickerPlaceholder({
    super.key,
    required this.onPickGallery,
    required this.onPickCamera,
  });

  final VoidCallback onPickGallery;
  final VoidCallback onPickCamera;

  @override
  Widget build(BuildContext context) {
    return AspectRatio(
      aspectRatio: 1,
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: AppRadius.xxlAll,
          border: Border.all(color: AppColors.border, width: 2),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.add_a_photo_outlined,
              size: 56,
              color: AppColors.primaryBlue,
            ),
            const SizedBox(height: AppSpacing.lg),
            Text(
              'Chọn hoặc chụp ảnh kỷ niệm',
              style: AppTextStyles.titleMedium.copyWith(
                color: AppColors.textPrimary,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: AppSpacing.md),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.xxl),
              child: Text(
                'Ảnh chỉ được lưu trên thiết bị của bạn. Ứng dụng không tải ảnh lên dịch vụ.',
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.textSecondary,
                ),
                textAlign: TextAlign.center,
              ),
            ),
            const SizedBox(height: AppSpacing.xl),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _PickButton(
                  icon: Icons.photo_library_outlined,
                  label: 'Thư viện',
                  onTap: onPickGallery,
                ),
                const SizedBox(width: AppSpacing.lg),
                _PickButton(
                  icon: Icons.photo_camera_outlined,
                  label: 'Chụp ảnh',
                  onTap: onPickCamera,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _PickButton extends StatelessWidget {
  const _PickButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return OutlinedButton.icon(
      onPressed: onTap,
      icon: Icon(icon, size: 20),
      label: Text(label),
      style: OutlinedButton.styleFrom(
        foregroundColor: AppColors.primaryBlue,
        side: const BorderSide(color: AppColors.primaryBlue),
        shape: RoundedRectangleBorder(borderRadius: AppRadius.xlAll),
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.lg,
          vertical: AppSpacing.md,
        ),
      ),
    );
  }
}
