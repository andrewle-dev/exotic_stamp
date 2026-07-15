import 'package:equatable/equatable.dart';

class MaintenancePolicy extends Equatable {
  const MaintenancePolicy({
    required this.enabled,
    this.message,
  });

  final bool enabled;
  final String? message;

  @override
  List<Object?> get props => [enabled, message];
}
