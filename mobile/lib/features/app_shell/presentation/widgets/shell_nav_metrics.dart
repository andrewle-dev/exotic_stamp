import 'package:flutter/material.dart';

/// Shared metrics for the authenticated shell bottom nav + scan FAB.
///
/// Tuned to the Visily / premium bottom-bar reference.
abstract final class ShellNavMetrics {
  /// BottomAppBar content height (excludes system safe-area inset).
  static const double barHeight = 72;

  /// Top corner radius of the white bar (soft pill feel).
  static const double barTopRadius = 28;

  /// Gap reserved in the tab row for the docked FAB (matches outer FAB).
  static const double fabGap = 72;

  /// Red scan button diameter (inside the white ring) — baseline from design.
  static const double fabSize = 52;

  /// White ring thickness around the scan FAB.
  static const double fabRing = 3.5;

  /// Outer diameter including the white ring — used for layout/notch.
  static const double fabOuterSize = fabSize + fabRing * 2;

  /// Scan FAB icon size (larger, simpler mark).
  static const double fabIconSize = 28;

  /// Tab icon size.
  static const double tabIconSize = 24;

  /// Fixed tap/splash hit area for every nav item (same size for all tabs).
  static const double tabTapWidth = 68;
  static const double tabTapHeight = 52;

  /// Spacing between tab icon and label.
  static const double tabIconLabelGap = 2;

  /// Circular notch margin around the FAB outer edge (deeper cut).
  static const double notchMargin = 8;

  /// Soft lip radius where the bar top meets the notch.
  static const double notchLipRadius = 12;

  /// Sink the docked FAB slightly into the bar (sits in the cutout).
  static const double fabDockSink = 22;

  /// Extra scroll inset so page content clears the docked FAB.
  static const double contentClearance = 48;
}

/// [FloatingActionButtonLocation.centerDocked] with a small downward sink
/// so the scan action sits in the notch without covering page content.
class ShellScanFabLocation extends FloatingActionButtonLocation {
  const ShellScanFabLocation();

  @override
  Offset getOffset(ScaffoldPrelayoutGeometry geometry) {
    final docked =
        FloatingActionButtonLocation.centerDocked.getOffset(geometry);
    return Offset(docked.dx, docked.dy + ShellNavMetrics.fabDockSink);
  }
}
