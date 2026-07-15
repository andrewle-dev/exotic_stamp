import 'app_spacing.dart';

/// Shared layout dimensions for chrome, headers, and bottom nav clearance.
///
/// Bottom-nav numeric values must stay in sync with [ShellNavMetrics].
abstract final class AppDimensions {
  /// Minimum row height for top-level screen headers (logo / title / action).
  static const double headerMinHeight = 40;

  /// Circular header action button diameter.
  static const double actionButtonSize = 40;

  /// Horizontal padding for shell tab screens.
  static const double screenHorizontalPadding = AppSpacing.xl;

  /// BottomAppBar content height (excludes system safe-area inset).
  static const double bottomNavHeight = 72;

  /// Scan FAB outer diameter including white ring.
  static const double bottomNavFabSize = 59;

  /// Scroll inset so content clears the docked FAB + bottom bar.
  static const double bottomNavBottomPadding = 48;

  /// Soft minimum for primary content cards.
  static const double cardMinHeight = 72;
}
