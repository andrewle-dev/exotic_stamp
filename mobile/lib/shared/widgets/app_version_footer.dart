import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../../app/theme/app_colors.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_typography.dart';

/// Local display name for the user-facing version footer.
///
/// Installed version always comes from [package_info_plus]. Backend
/// `/mobile/app-config` is only used for update/maintenance policy — never
/// for this chrome label.
abstract final class AppVersionCopy {
  static const productName = 'METRO STAMP';

  static String format(String version) => '$productName v$version';
}

/// Full-width, centered muted version line from [package_info_plus].
///
/// Always expands to the parent width so it stays centered even inside
/// columns with [CrossAxisAlignment.start].
class AppVersionFooter extends StatefulWidget {
  const AppVersionFooter({
    super.key,
    this.versionOverride,
    this.topSpacing = AppSpacing.lg,
  });

  /// Test-only override; production reads package metadata.
  final String? versionOverride;

  /// Compact gap above the label (avoid large empty bands).
  final double topSpacing;

  @override
  State<AppVersionFooter> createState() => _AppVersionFooterState();
}

class _AppVersionFooterState extends State<AppVersionFooter> {
  static String? _cachedVersion;
  late final Future<String> _versionFuture;

  @override
  void initState() {
    super.initState();
    _versionFuture = _resolveVersion();
  }

  Future<String> _resolveVersion() async {
    if (widget.versionOverride != null) {
      return widget.versionOverride!;
    }
    if (_cachedVersion != null) {
      return _cachedVersion!;
    }
    final info = await PackageInfo.fromPlatform();
    _cachedVersion = info.version;
    return _cachedVersion!;
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<String>(
      future: _versionFuture,
      builder: (context, snapshot) {
        final version = snapshot.data ?? _cachedVersion ?? widget.versionOverride;
        final label = (version == null || version.isEmpty)
            ? ''
            : AppVersionCopy.format(version);

        // Reserve a stable height so async load does not jump layout much.
        return SizedBox(
          width: double.infinity,
          child: Padding(
            padding: EdgeInsets.only(top: widget.topSpacing),
            child: Text(
              label,
              textAlign: TextAlign.center,
              style: AppTextStyles.caption.copyWith(
                color: AppColors.textSecondary.withValues(alpha: 0.75),
                letterSpacing: 1.1,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        );
      },
    );
  }
}
