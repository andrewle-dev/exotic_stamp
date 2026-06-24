import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/rewards_route_refresh.dart';
import '../cubit/rewards_cubit.dart';
import 'rewards_refresh_coordinator.dart';

/// Watches rewards route refresh query changes and triggers backend refetch.
class RewardsRefreshListener extends StatefulWidget {
  const RewardsRefreshListener({
    required this.child,
    this.coordinator = const RewardsRefreshCoordinator(),
    super.key,
  });

  final Widget child;
  final RewardsRefreshCoordinator coordinator;

  @override
  State<RewardsRefreshListener> createState() => _RewardsRefreshListenerState();
}

class _RewardsRefreshListenerState extends State<RewardsRefreshListener> {
  String? _lastHandledRefreshToken;
  bool _didInitialSync = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _syncFromRoute());
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_didInitialSync) {
      _syncFromRoute();
    }
  }

  void _syncFromRoute() {
    if (!mounted) {
      return;
    }

    final refreshToken =
        RewardsRouteRefresh.readToken(GoRouterState.of(context).uri);
    final cubit = context.read<RewardsCubit>();
    final action = widget.coordinator.resolve(
      refreshToken: refreshToken,
      lastHandledToken: _lastHandledRefreshToken,
      status: cubit.state.status,
      isInitialMount: !_didInitialSync,
    );

    _didInitialSync = true;

    if (refreshToken != null &&
        refreshToken.isNotEmpty &&
        refreshToken != _lastHandledRefreshToken) {
      _lastHandledRefreshToken = refreshToken;
    }

    switch (action) {
      case RewardsRefreshAction.load:
        cubit.load();
      case RewardsRefreshAction.refresh:
        cubit.refresh();
      case RewardsRefreshAction.none:
        break;
    }
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
