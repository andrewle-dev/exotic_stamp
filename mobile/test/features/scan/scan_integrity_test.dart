import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test('scan remote datasource exposes collect status endpoint', () {
    final source = File(
      'lib/features/scan/data/datasources/scan_remote_datasource.dart',
    ).readAsStringSync();

    expect(source.contains('/collection/collect/status'), isTrue);
    expect(source.contains('idempotencyKey'), isTrue);
  });

  test('scan flow cubit never locally inserts stamp on uncertain outcome', () {
    final source = File(
      'lib/features/scan/presentation/cubit/scan_flow_cubit.dart',
    ).readAsStringSync();

    expect(source.contains('checkCollectStatus'), isTrue);
    expect(source.contains('collectResult: result'), isTrue);
    // Must not fabricate stamp without backend response.
    expect(
      RegExp(r'CollectedStamp\s*\(').allMatches(source).length,
      0,
    );
  });

  test('production lib scan feature has no mock collect data', () {
    final scanDir = Directory('lib/features/scan');
    final forbidden = ['mockStamp', 'fakeStamp', 'sampleStamp', 'demoCollect'];

    for (final entity in scanDir.listSync(recursive: true)) {
      if (entity is! File || !entity.path.endsWith('.dart')) {
        continue;
      }
      final content = entity.readAsStringSync();
      for (final pattern in forbidden) {
        expect(
          content.contains(pattern),
          isFalse,
          reason: '${entity.path} must not contain $pattern',
        );
      }
    }
  });
}
