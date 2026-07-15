import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/line.dart';
import '../utils/stations_line_filter.dart';

class StationsLineFilterBar extends StatelessWidget {
  const StationsLineFilterBar({
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

    final effectiveSelection = selectedLineId ?? StationsLineFilter.allLines;

    return SizedBox(
      height: 44,
      child: ListView(
        scrollDirection: Axis.horizontal,
        children: [
          _LineChip(
            label: 'All Lines',
            selected: effectiveSelection == StationsLineFilter.allLines,
            onTap: () => onSelected(StationsLineFilter.allLines),
          ),
          for (final line in lines) ...[
            const SizedBox(width: AppSpacing.md),
            _LineChip(
              label: line.displayName ?? line.name,
              selected: effectiveSelection == line.id,
              onTap: () => onSelected(line.id),
            ),
          ],
        ],
      ),
    );
  }
}

class _LineChip extends StatelessWidget {
  const _LineChip({
    required this.label,
    required this.selected,
    required this.onTap,
  });

  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: selected ? AppColors.primaryBlue : AppColors.surface,
      borderRadius: BorderRadius.circular(AppRadius.pill),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(AppRadius.pill),
        child: Container(
          alignment: Alignment.center,
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpacing.lg,
            vertical: AppSpacing.sm,
          ),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(AppRadius.pill),
            border: Border.all(
              color: selected ? AppColors.primaryBlue : AppColors.border,
            ),
          ),
          child: Text(
            label,
            style: AppTextStyles.labelMedium.copyWith(
              color: selected
                  ? AppColors.backgroundWhite
                  : AppColors.textSecondary,
              fontWeight: FontWeight.w700,
              fontSize: 13,
              height: 1.2,
            ),
          ),
        ),
      ),
    );
  }
}
