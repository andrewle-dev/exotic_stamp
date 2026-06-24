import 'package:flutter/material.dart';

/// Canonical brand and surface colors for Exotic Stamp.
class AppColors {
  static const primaryBlue = Color(0xFF01599D);
  static const accentRed = Color(0xFFE83B28);
  static const backgroundWhite = Color(0xFFFFFFFF);

  static const textPrimary = Color(0xFF1D2433);
  static const textSecondary = Color(0xFF667085);
  static const border = Color(0xFFE4E7EC);
  static const surface = Color(0xFFF8FAFC);

  static const inputBackground = Color(0xFFF9FAFB);
  static const blueTint = Color(0xFFEAF3FB);
  static const redTint = Color(0xFFFDEDEB);
  static const blueSurface = Color(0xFFF4F8FC);
  static const shadow = Color(0x11000000);

  /// Legacy aliases — prefer [primaryBlue], [accentRed], [backgroundWhite].
  static const brandBlue = primaryBlue;
  static const brandRed = accentRed;
  static const background = backgroundWhite;
  static const textMuted = textSecondary;

  const AppColors._();
}
