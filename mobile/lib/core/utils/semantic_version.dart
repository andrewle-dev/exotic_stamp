/// Parses and compares semantic version strings (`X.Y.Z`).
///
/// Ignores build metadata after `+` and prerelease after `-`.
/// Lexical string compare is intentionally not used (`1.10.0` > `1.2.0`).
abstract final class SemanticVersion {
  const SemanticVersion._();

  /// Returns negative if [a] < [b], zero if equal, positive if [a] > [b].
  static int compare(String a, String b) {
    final left = parse(a);
    final right = parse(b);
    for (var i = 0; i < 3; i++) {
      final diff = left[i].compareTo(right[i]);
      if (diff != 0) {
        return diff;
      }
    }
    return 0;
  }

  static bool isAtLeast(String version, String minimum) =>
      compare(version, minimum) >= 0;

  static bool isBelow(String version, String other) =>
      compare(version, other) < 0;

  /// Extracts `[major, minor, patch]` from strings like `1.2.3`, `1.2.3+12`,
  /// `1.2.3-beta.1`. Missing parts default to `0`.
  static List<int> parse(String raw) {
    final core = raw.trim().split('+').first.split('-').first;
    final parts = core.split('.');
    return [
      _parsePart(parts, 0),
      _parsePart(parts, 1),
      _parsePart(parts, 2),
    ];
  }

  static int _parsePart(List<String> parts, int index) {
    if (index >= parts.length) {
      return 0;
    }
    final match = RegExp(r'^(\d+)').firstMatch(parts[index].trim());
    if (match == null) {
      return 0;
    }
    return int.tryParse(match.group(1)!) ?? 0;
  }
}
