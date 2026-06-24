import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/stamp_book_route_refresh.dart';
import '../cubit/stamp_book_cubit.dart';
import 'stamp_book_refresh_coordinator.dart';

/// Watches stamp-book route refresh query changes and triggers backend refetch.
class StampBookRefreshListener extends StatefulWidget {
  const StampBookRefreshListener({
    required this.child,
    this.coordinator = const StampBookRefreshCoordinator(),
    super.key,
  });

  final Widget child;
  final StampBookRefreshCoordinator coordinator;

  @override
  State<StampBookRefreshListener> createState() =>
      _StampBookRefreshListenerState();
}

class _StampBookRefreshListenerState extends State<StampBookRefreshListener> {
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
        StampBookRouteRefresh.readToken(GoRouterState.of(context).uri);
    final cubit = context.read<StampBookCubit>();
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
      case StampBookRefreshAction.load:
        cubit.load();
      case StampBookRefreshAction.refresh:
        cubit.refresh();
      case StampBookRefreshAction.none:
        break;
    }
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
