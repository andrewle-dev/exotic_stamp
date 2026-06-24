import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/utils/idempotency_key_generator.dart';
import 'package:uuid/uuid.dart';

void main() {
  group('IdempotencyKeyGenerator', () {
    test('generates valid UUID v4 strings', () {
      const generator = IdempotencyKeyGenerator();
      final key = generator.generate();

      expect(Uuid.isValidUUID(fromString: key), isTrue);
    });

    test('generates unique keys', () {
      const generator = IdempotencyKeyGenerator();
      final first = generator.generate();
      final second = generator.generate();

      expect(first, isNot(equals(second)));
    });
  });
}
