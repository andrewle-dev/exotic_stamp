import 'package:equatable/equatable.dart';

class PlatformVersionPolicy extends Equatable {
  const PlatformVersionPolicy({
    required this.minimumSupportedVersion,
    required this.latestVersion,
    required this.forceUpdate,
    this.storeUrl,
  });

  final String minimumSupportedVersion;
  final String latestVersion;
  final bool forceUpdate;
  final String? storeUrl;

  @override
  List<Object?> get props => [
        minimumSupportedVersion,
        latestVersion,
        forceUpdate,
        storeUrl,
      ];
}
