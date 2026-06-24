import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test('production lib does not import station_catalog.dart', () {
    final libDir = Directory('lib');
    expect(libDir.existsSync(), isTrue);

    final offenders = <String>[];
    for (final entity in libDir.listSync(recursive: true)) {
      if (entity is! File || !entity.path.endsWith('.dart')) {
        continue;
      }
      final content = entity.readAsStringSync();
      if (content.contains('station_catalog.dart')) {
        offenders.add(entity.path);
      }
    }

    expect(
      offenders,
      isEmpty,
      reason: 'station_catalog must not be used in production: $offenders',
    );
  });
}
