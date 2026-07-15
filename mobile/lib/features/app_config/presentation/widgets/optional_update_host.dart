import 'package:flutter/material.dart';

import '../../../../core/di/injection.dart';
import '../../domain/entities/app_update_decision.dart';
import 'optional_update_dialog.dart';

/// Shows the optional update dialog once per dismissed [latestVersion].
class OptionalUpdateHost extends StatefulWidget {
  const OptionalUpdateHost({
    required this.child,
    super.key,
  });

  final Widget child;

  @override
  State<OptionalUpdateHost> createState() => _OptionalUpdateHostState();
}

class _OptionalUpdateHostState extends State<OptionalUpdateHost> {
  var _promptScheduled = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _maybePrompt());
  }

  Future<void> _maybePrompt() async {
    if (_promptScheduled || !mounted) {
      return;
    }
    _promptScheduled = true;

    final injection = Injection.instance;
    final decision = injection.appUpdateDecision;
    if (decision.type != AppUpdateDecisionType.optionalUpdate) {
      return;
    }

    final latest = decision.latestVersion;
    if (latest == null || latest.isEmpty) {
      return;
    }
    if (injection.localPreferences.optionalUpdateDismissedVersion == latest) {
      return;
    }

    final updated = await showOptionalUpdateDialog(
      context: context,
      decision: decision,
    );
    if (!mounted) {
      return;
    }
    // Dismissed via Later, barrier, or after opening store — don't re-prompt
    // for this latest version in this install session/preference.
    if (updated == false || updated == true || updated == null) {
      await injection.localPreferences.setOptionalUpdateDismissedVersion(latest);
    }
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
