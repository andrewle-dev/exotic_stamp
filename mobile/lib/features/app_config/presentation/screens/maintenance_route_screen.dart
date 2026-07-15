import 'package:flutter/material.dart';

import '../../../../core/di/injection.dart';
import '../../domain/entities/app_update_decision.dart';
import 'maintenance_screen.dart';

class MaintenanceRouteScreen extends StatefulWidget {
  const MaintenanceRouteScreen({super.key});

  @override
  State<MaintenanceRouteScreen> createState() => _MaintenanceRouteScreenState();
}

class _MaintenanceRouteScreenState extends State<MaintenanceRouteScreen> {
  var _retrying = false;

  Future<void> _retry() async {
    if (_retrying) {
      return;
    }
    setState(() => _retrying = true);
    try {
      await Injection.instance.refreshAppUpdatePolicy();
    } finally {
      if (mounted) {
        setState(() => _retrying = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final decision = Injection.instance.appUpdateDecision;
    return MaintenanceScreen(
      decision: decision.type == AppUpdateDecisionType.maintenance
          ? decision
          : AppUpdateDecision.maintenance(
              installedVersion: decision.installedVersion,
            ),
      isRetrying: _retrying,
      onRetry: _retry,
    );
  }
}
