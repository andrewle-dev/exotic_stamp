import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/utils/semantic_version.dart';

void main() {
  group('SemanticVersion.compare', () {
    test('1.10.0 is greater than 1.2.0', () {
      expect(SemanticVersion.compare('1.10.0', '1.2.0'), greaterThan(0));
    });

    test('equal versions', () {
      expect(SemanticVersion.compare('1.0.0', '1.0.0'), 0);
    });

    test('build metadata is ignored for equality floor', () {
      expect(SemanticVersion.compare('1.0.0+2', '1.0.0'), 0);
      expect(SemanticVersion.isAtLeast('1.0.0+2', '1.0.0'), isTrue);
    });

    test('0.9.9 is below 1.0.0', () {
      expect(SemanticVersion.isBelow('0.9.9', '1.0.0'), isTrue);
      expect(SemanticVersion.compare('0.9.9', '1.0.0'), lessThan(0));
    });

    test('prerelease suffix is ignored on core compare', () {
      expect(SemanticVersion.compare('1.2.3-beta', '1.2.3'), 0);
    });
  });
}
