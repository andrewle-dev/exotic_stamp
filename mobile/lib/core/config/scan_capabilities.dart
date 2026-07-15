/// Scan collection capabilities for the mobile app.
///
/// QR stamp-collection flow is temporarily hidden by product requirement.
/// Lower-layer QR types, API contracts, and services are retained so the flow
/// can be re-enabled by flipping [enableQrFlow] (or `--dart-define=ENABLE_QR_FLOW=true`).
abstract final class ScanCapabilities {
  /// NFC stamp collection — primary and currently only exposed path.
  static const bool enableNfc = true;

  /// QR stamp-collection UI / fallback entry points.
  ///
  /// Default `false`: no QR tab, CTA, scanner, or QR-specific collect copy.
  /// Set to `true` (or pass `--dart-define=ENABLE_QR_FLOW=true`) to restore.
  static const bool enableQrFlow = bool.fromEnvironment(
    'ENABLE_QR_FLOW',
    defaultValue: false,
  );
}
