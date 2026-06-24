/// Backend `CollectStatusResponse.status` values.
enum CollectStatusOutcome {
  success,
  duplicate,
  notFound,
  pending,
  failed,
  unknown;

  static CollectStatusOutcome fromApi(String? raw) {
    return switch (raw?.toUpperCase()) {
      'SUCCESS' => CollectStatusOutcome.success,
      'DUPLICATE' => CollectStatusOutcome.duplicate,
      'NOT_FOUND' => CollectStatusOutcome.notFound,
      'PENDING' => CollectStatusOutcome.pending,
      'FAILED' => CollectStatusOutcome.failed,
      _ => CollectStatusOutcome.unknown,
    };
  }
}
