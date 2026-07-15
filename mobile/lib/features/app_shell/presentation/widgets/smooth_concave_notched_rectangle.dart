import 'dart:math' as math;

import 'package:flutter/painting.dart';

/// Host shape with rounded top corners and a deep, smooth concave notch.
///
/// Produces a semicircle-like dent with soft lip fillets — closer to the
/// Visily / premium mock than stock [CircularNotchedRectangle].
class SmoothConcaveNotchedRectangle extends NotchedShape {
  const SmoothConcaveNotchedRectangle({
    this.cornerRadius = 28,
    this.lipRadius = 12,
  });

  /// Top-left / top-right corner radius of the bar.
  final double cornerRadius;

  /// Softening distance where the top edge blends into the notch arc.
  final double lipRadius;

  @override
  Path getOuterPath(Rect host, Rect? guest) {
    final corner = cornerRadius.clamp(0.0, host.height / 2);

    if (guest == null || !host.overlaps(guest)) {
      return Path()
        ..addRRect(
          RRect.fromRectAndCorners(
            host,
            topLeft: Radius.circular(corner),
            topRight: Radius.circular(corner),
          ),
        );
    }

    // Guest is FAB bounds expanded by [BottomAppBar.notchMargin].
    final r = guest.width / 2.0;
    final cx = guest.center.dx;
    // Semicircle anchored on the bar's top edge → deep, even cut.
    final cy = host.top;
    final lip = math.min(lipRadius, r * 0.5);

    // Arc endpoints sit slightly below the top edge so lip beziers can blend.
    const startAngle = math.pi - 0.35; // ~160°
    const endAngle = 0.35; // ~20°
    final arcLeft = Offset(
      cx + r * math.cos(startAngle),
      cy + r * math.sin(startAngle),
    );
    final arcRight = Offset(
      cx + r * math.cos(endAngle),
      cy + r * math.sin(endAngle),
    );

    final path = Path()
      ..moveTo(host.left, host.bottom)
      ..lineTo(host.left, host.top + corner)
      ..quadraticBezierTo(host.left, host.top, host.left + corner, host.top)
      ..lineTo(cx - r - lip, host.top)

      // Soft left lip into the concave arc.
      ..quadraticBezierTo(cx - r, host.top, arcLeft.dx, arcLeft.dy)

      // Deep semicircle-like arc (left → bottom → right).
      ..arcToPoint(
        arcRight,
        radius: Radius.circular(r),
        clockwise: false,
        largeArc: false,
      )

      // Soft right lip back onto the top edge.
      ..quadraticBezierTo(cx + r, host.top, cx + r + lip, host.top)
      ..lineTo(host.right - corner, host.top)
      ..quadraticBezierTo(host.right, host.top, host.right, host.top + corner)
      ..lineTo(host.right, host.bottom)
      ..lineTo(host.left, host.bottom)
      ..close();

    return path;
  }
}
