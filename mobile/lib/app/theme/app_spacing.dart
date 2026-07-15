/// Consistent spacing scale for layout and padding.
///
/// Canonical values (M0 design system):
/// xs=4, sm=8, md=12, lg=16, xl=20, xxl=24, xxxl=32
abstract final class AppSpacing {
  static const double xs = 4;
  static const double sm = 8;
  static const double md = 12;
  static const double lg = 16;
  static const double xl = 20;
  static const double xxl = 24;
  static const double xxxl = 32;

  /// Sparse layouts only (rare). Prefer [xxxl] when possible.
  static const double huge = 40;

  /// Legacy alias — prefer [xs].
  @Deprecated('Use AppSpacing.xs')
  static const double xxs = xs;
}
