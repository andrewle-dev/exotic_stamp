import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test('production lib does not contain mock profile user data', () {
    final libDir = Directory('lib');
    expect(libDir.existsSync(), isTrue);

    final forbiddenPatterns = [
      'Hoàng Anh',
      'hoanganghle0203@gmail.com',
      'settings_page.dart',
      'profile_page.dart',
    ];

    for (final entity in libDir.listSync(recursive: true)) {
      if (entity is! File || !entity.path.endsWith('.dart')) {
        continue;
      }

      final content = entity.readAsStringSync();
      for (final pattern in forbiddenPatterns) {
        expect(
          content.contains(pattern),
          isFalse,
          reason: '${entity.path} must not contain mock profile data: $pattern',
        );
      }
    }
  });
}
