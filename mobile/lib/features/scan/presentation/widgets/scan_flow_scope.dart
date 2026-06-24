import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/di/injection.dart';
import '../cubit/scan_flow_cubit.dart';

/// Provides the shared [ScanFlowCubit] to all scan flow screens.
class ScanFlowScope extends StatelessWidget {
  const ScanFlowScope({required this.child, super.key});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return BlocProvider<ScanFlowCubit>.value(
      value: Injection.instance.scanFlowCubit,
      child: child,
    );
  }
}
