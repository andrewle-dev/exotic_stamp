import 'package:uuid/uuid.dart';

/// Generates client idempotency keys for collect requests.
class IdempotencyKeyGenerator {
  const IdempotencyKeyGenerator({Uuid? uuid}) : _uuid = uuid ?? const Uuid();

  final Uuid _uuid;

  String generate() => _uuid.v4();
}
