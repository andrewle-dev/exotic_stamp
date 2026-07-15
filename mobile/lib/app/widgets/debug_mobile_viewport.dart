import 'dart:io' show Platform;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_shadow.dart';

/// Constrains the app to a phone-width column when debugging on desktop OS targets.
///
/// Active only in [kDebugMode] on Windows, macOS, or Linux. Does not affect
/// Android/iOS builds or release/profile desktop builds.
class DebugMobileViewport extends StatelessWidget {
  const DebugMobileViewport({
    required this.child,
    this.enabledOverride,
    super.key,
  });

  final Widget child;

  /// When set, overrides [isEnabled] (used in widget tests).
  final bool? enabledOverride;

  /// Maximum width of the simulated device frame.
  static const double maxWidth = 430;

  /// Preferred design reference width (iPhone 14 class).
  static const double preferredWidth = 390;

  static bool get isEnabled {
    if (!kDebugMode || kIsWeb) {
      return false;
    }
    try {
      return Platform.isWindows || Platform.isMacOS || Platform.isLinux;
    } on UnsupportedError {
      return false;
    }
  }

  @override
  Widget build(BuildContext context) {
    final enabled = enabledOverride ?? isEnabled;
    if (!enabled) {
      return child;
    }

    return ColoredBox(
      color: AppColors.debugViewportFrame,
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: maxWidth),
          child: SizedBox(
            width: preferredWidth,
            child: DecoratedBox(
              decoration: BoxDecoration(
                color: AppColors.backgroundWhite,
                border: Border.all(color: AppColors.border),
                boxShadow: AppShadow.viewportFrame,
              ),
              child: ClipRect(child: child),
            ),
          ),
        ),
      ),
    );
  }
}
