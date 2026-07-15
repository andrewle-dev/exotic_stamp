import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import 'pre_stamp_ad_sheet.dart';

/// Central navigation reactions for the NFC-first scan flow.
class ScanFlowListener extends StatelessWidget {
  const ScanFlowListener({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return BlocListener<ScanFlowCubit, ScanFlowState>(
      listenWhen: (previous, current) =>
          previous.phase != current.phase ||
          previous.awaitingCollectConfirmation !=
              current.awaitingCollectConfirmation,
      listener: (context, state) {
        if (state.awaitingCollectConfirmation && state.resolvedStation != null) {
          final location = GoRouter.of(context).state.matchedLocation;
          if (!location.contains('location-verification')) {
            context.push(RouteNames.scanLocationVerification);
          }
          return;
        }

        if (state.phase == ScanFlowPhase.preStampAd &&
            state.collectResult?.sponsorAd != null) {
          PreStampAdSheet.show(
            context,
            ad: state.collectResult!.sponsorAd!,
            onContinue: () {
              context.read<ScanFlowCubit>().acknowledgePreStampAd();
            },
          );
          return;
        }

        if (state.phase == ScanFlowPhase.success) {
          final location = GoRouter.of(context).state.matchedLocation;
          if (!location.contains('success')) {
            context.push(RouteNames.scanSuccess);
          }
          return;
        }

        if (_isErrorPhase(state.phase) &&
            !state.awaitingCollectConfirmation &&
            state.phase != ScanFlowPhase.preStampAd) {
          final location = GoRouter.of(context).state.matchedLocation;
          if (!location.contains('error')) {
            context.push(RouteNames.scanError);
          }
        }
      },
      child: child,
    );
  }

  bool _isErrorPhase(ScanFlowPhase phase) {
    return phase == ScanFlowPhase.duplicate ||
        phase == ScanFlowPhase.invalidTag ||
        phase == ScanFlowPhase.qrExpired ||
        phase == ScanFlowPhase.gpsOutsideRange ||
        phase == ScanFlowPhase.stationInactive ||
        phase == ScanFlowPhase.campaignInactive ||
        phase == ScanFlowPhase.networkError ||
        phase == ScanFlowPhase.locationPermissionDenied ||
        phase == ScanFlowPhase.locationServiceDisabled ||
        phase == ScanFlowPhase.locationLowAccuracy ||
        phase == ScanFlowPhase.locationTimeout ||
        phase == ScanFlowPhase.unknownError;
  }
}
