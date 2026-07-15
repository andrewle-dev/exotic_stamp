import 'package:flutter/material.dart';

import 'app_colors.dart';
import 'app_radius.dart';

/// Shared elevation and shadow tokens for cards and floating elements.
///
/// Prefer soft card shadows — avoid heavy neon glow.
abstract final class AppShadows {
  static const double cardElevation = 2;

  static List<BoxShadow> get softCard => const [
        BoxShadow(
          color: AppColors.shadow,
          blurRadius: 16,
          offset: Offset(0, 4),
        ),
      ];

  static List<BoxShadow> get floating => const [
        BoxShadow(
          color: AppColors.shadow,
          blurRadius: 8,
          offset: Offset(0, 2),
        ),
      ];

  static List<BoxShadow> get bottomNav => [
        BoxShadow(
          color: Colors.black.withValues(alpha: 0.14),
          blurRadius: 10,
          offset: const Offset(0, -2),
        ),
      ];

  static List<BoxShadow> get fab => [
        BoxShadow(
          color: AppColors.accentRed.withValues(alpha: 0.10),
          blurRadius: 8,
          offset: const Offset(0, 2),
        ),
      ];

  static List<BoxShadow> get viewportFrame => const [
        BoxShadow(
          color: AppColors.shadow,
          blurRadius: 24,
          offset: Offset(0, 8),
        ),
      ];

  /// Legacy aliases — prefer [softCard] / [floating].
  static List<BoxShadow> get card => softCard;
  static List<BoxShadow> get cardSubtle => floating;

  static BoxDecoration cardDecoration({
    Color color = AppColors.backgroundWhite,
    BorderRadius? borderRadius,
    Border? border,
  }) {
    return BoxDecoration(
      color: color,
      borderRadius: borderRadius ?? AppRadius.xlAll,
      border: border ?? Border.all(color: AppColors.border),
      boxShadow: softCard,
    );
  }
}

/// Legacy name kept for existing imports — prefer [AppShadows].
abstract final class AppShadow {
  static const double cardElevation = AppShadows.cardElevation;
  static List<BoxShadow> get softCard => AppShadows.softCard;
  static List<BoxShadow> get floating => AppShadows.floating;
  static List<BoxShadow> get bottomNav => AppShadows.bottomNav;
  static List<BoxShadow> get fab => AppShadows.fab;
  static List<BoxShadow> get viewportFrame => AppShadows.viewportFrame;
  static List<BoxShadow> get card => AppShadows.card;
  static List<BoxShadow> get cardSubtle => AppShadows.cardSubtle;

  static BoxDecoration cardDecoration({
    Color color = AppColors.backgroundWhite,
    BorderRadius? borderRadius,
    Border? border,
  }) {
    return AppShadows.cardDecoration(
      color: color,
      borderRadius: borderRadius,
      border: border,
    );
  }
}
