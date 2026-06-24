import 'package:share_plus/share_plus.dart';

/// Native share sheet wrapper for tests.
abstract class NativeShareService {
  Future<NativeShareResult> shareImage({
    required String filePath,
    String? text,
  });
}

enum NativeShareOutcome {
  completed,
  dismissed,
  unavailable,
}

class NativeShareResult {
  const NativeShareResult(this.outcome);

  final NativeShareOutcome outcome;

  bool get isUserSuccess =>
      outcome == NativeShareOutcome.completed ||
      outcome == NativeShareOutcome.dismissed;
}

class SharePlusNativeShareService implements NativeShareService {
  @override
  Future<NativeShareResult> shareImage({
    required String filePath,
    String? text,
  }) async {
    final result = await Share.shareXFiles(
      [XFile(filePath)],
      text: text,
    );
    return NativeShareResult(_mapStatus(result.status));
  }

  NativeShareOutcome _mapStatus(ShareResultStatus status) {
    return switch (status) {
      ShareResultStatus.success => NativeShareOutcome.completed,
      ShareResultStatus.dismissed => NativeShareOutcome.dismissed,
      ShareResultStatus.unavailable => NativeShareOutcome.unavailable,
    };
  }
}
