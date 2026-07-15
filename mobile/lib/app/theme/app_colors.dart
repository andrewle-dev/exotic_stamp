import 'package:flutter/material.dart';

/// Canonical brand and surface colors for Exotic Stamp.
///
/// Do not invent screen-level HEX values — use these tokens only.
abstract final class AppColors {
  static const primaryBlue = Color(0xFF01599D);
  static const accentRed = Color(0xFFE83B28);
  static const backgroundWhite = Color(0xFFFFFFFF);

  static const textPrimary = Color(0xFF1D2433);
  static const textSecondary = Color(0xFF667085);
  static const border = Color(0xFFE4E7EC);
  static const surface = Color(0xFFF8FAFC);

  static const success = Color(0xFF12B76A);
  static const warning = Color(0xFFF79009);
  static const error = accentRed;

  static const activeIcon = primaryBlue;
  static const inactiveIcon = Color(0xFF94A3B8);

  /// Derived tints — use only via theme tokens, not as one-off screen colors.
  static const inputBackground = Color(0xFFF9FAFB);
  static const blueTint = Color(0xFFEAF3FB);
  static const redTint = Color(0xFFFDEDEB);
  static const blueSurface = Color(0xFFF4F8FC);
  static const lockedSurface = Color(0xFFF3F4F6);
  static const shadow = Color(0x11000000);

  /// Neutral frame behind the debug desktop mobile viewport.
  static const debugViewportFrame = Color(0xFFE8EAED);

  /// Legacy aliases — prefer [activeIcon] / [inactiveIcon].
  static const iconActive = activeIcon;
  static const iconInactive = inactiveIcon;

  /// Legacy aliases — prefer [primaryBlue], [accentRed], [backgroundWhite].
  @Deprecated('Use AppColors.primaryBlue')
  static const brandBlue = primaryBlue;

  @Deprecated('Use AppColors.accentRed')
  static const brandRed = accentRed;

  @Deprecated('Use AppColors.backgroundWhite')
  static const background = backgroundWhite;

  @Deprecated('Use AppColors.textSecondary')
  static const textMuted = textSecondary;
}
