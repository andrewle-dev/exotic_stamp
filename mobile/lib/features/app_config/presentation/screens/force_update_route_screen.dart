import 'package:flutter/material.dart';

import '../../../../core/di/injection.dart';
import 'force_update_screen.dart';

class ForceUpdateRouteScreen extends StatelessWidget {
  const ForceUpdateRouteScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ForceUpdateScreen(
      decision: Injection.instance.appUpdateDecision,
    );
  }
}
