import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../stations/domain/entities/line.dart';

/// Sentinel value for the stamp book "All Lines" filter chip.
abstract final class StampBookLineFilter {
  static const allLines = '__all__';
}

Color lineAccentFromHex(String? colorHex) {
  if (colorHex == null || colorHex.isEmpty) {
    return AppColors.primaryBlue;
  }
  final cleaned = colorHex.replaceFirst('#', '');
  if (cleaned.length == 6) {
    return Color(int.parse('FF$cleaned', radix: 16));
  }
  return AppColors.primaryBlue;
}

Color accentForLineSelection({
  required String? selectedLineId,
  required List<Line> lines,
  required int sequence,
}) {
  if (selectedLineId != null && selectedLineId != StampBookLineFilter.allLines) {
    final line = lines.firstWhere(
      (entry) => entry.id == selectedLineId,
      orElse: () => lines.first,
    );
    return lineAccentFromHex(line.colorHex);
  }
  return sequence.isOdd ? AppColors.primaryBlue : AppColors.accentRed;
}
