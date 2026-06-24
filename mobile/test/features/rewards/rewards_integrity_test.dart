import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test('rewards remote datasource does not call redeem endpoint', () {
    final source = File(
      'lib/features/rewards/data/datasources/rewards_remote_datasource.dart',
    ).readAsStringSync();

    expect(source.contains('/redeem'), isFalse);
    expect(source.contains('redeem'), isFalse);
  });

  test('rewards feature production code does not call redeem endpoint', () {
    final rewardsDir = Directory('lib/features/rewards');
    final violations = <String>[];

    for (final entity in rewardsDir.listSync(recursive: true)) {
      if (entity is! File || !entity.path.endsWith('.dart')) {
        continue;
      }
      final content = entity.readAsStringSync();
      if (content.contains('/redeem') || content.contains("'/redeem")) {
        violations.add(entity.path);
      }
    }

    expect(violations, isEmpty);
  });

  test('no production mock rewards page remains', () {
    expect(
      File('lib/features/rewards/rewards_page.dart').existsSync(),
      isFalse,
    );
  });
}
