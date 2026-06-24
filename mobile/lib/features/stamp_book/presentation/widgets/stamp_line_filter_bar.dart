import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../stations/domain/entities/line.dart';

class StampLineFilterBar extends StatelessWidget {
  const StampLineFilterBar({
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

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children: [
          for (final line in lines) ...[
            ChoiceChip(
              label: Text(line.label),
              selected: line.id == selectedLineId,
              onSelected: (_) => onSelected(line.id),
              selectedColor: AppColors.blueTint,
              labelStyle: AppTextStyles.labelMedium.copyWith(
                color: line.id == selectedLineId
                    ? AppColors.primaryBlue
                    : AppColors.textSecondary,
              ),
              side: const BorderSide(color: AppColors.border),
            ),
            const SizedBox(width: AppSpacing.sm),
          ],
        ],
      ),
    );
  }
}
