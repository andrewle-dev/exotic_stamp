import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/nfc/nfc_availability.dart';
import '../../../../core/nfc/nfc_reader.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import '../widgets/scan_action_buttons.dart';

class ScanScreen extends StatefulWidget {
  const ScanScreen({super.key});

  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> with WidgetsBindingObserver {
  final MobileScannerController _qrController = MobileScannerController(
    formats: const [BarcodeFormat.qrCode],
    detectionSpeed: DetectionSpeed.noDuplicates,
  );

  bool _handlingQr = false;
  bool _nfcSessionActive = false;
  ScanFlowCubit? _flowCubit;
  NfcReader? _nfcReader;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) {
        return;
      }
      _bindFlowDependencies();
      final cubit = _flowCubit;
      if (cubit == null) {
        return;
      }
      if (_isTerminalPhase(cubit.state.phase)) {
        cubit.resetFlow();
      }
      cubit.initialize().then((_) => _syncNfcSession());
    });
  }

  void _bindFlowDependencies() {
    final cubit = context.read<ScanFlowCubit>();
    _flowCubit = cubit;
    _nfcReader = cubit.nfcReader;
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _stopNfcSession();
    _qrController.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _syncNfcSession();
      return;
    }
    if (state == AppLifecycleState.paused ||
        state == AppLifecycleState.inactive ||
        state == AppLifecycleState.hidden) {
      _stopNfcSession();
      _qrController.stop();
    }
  }

  bool _isOnScanRootRoute() {
    if (!mounted) {
      return false;
    }
    return GoRouter.of(context).state.matchedLocation == RouteNames.scan;
  }

  bool _isTerminalPhase(ScanFlowPhase phase) {
    return phase == ScanFlowPhase.success ||
        phase == ScanFlowPhase.duplicate ||
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

  Future<void> _syncNfcSession() async {
    final cubit = _flowCubit;
    final nfcReader = _nfcReader;
    if (cubit == null || nfcReader == null || !mounted) {
      return;
    }

    final state = cubit.state;
    if (!_isOnScanRootRoute() ||
        state.phase != ScanFlowPhase.waitingForNfc ||
        state.qrFallbackAvailable ||
        _nfcSessionActive) {
      return;
    }

    await _startNfcSession(cubit, nfcReader);
  }

  Future<void> _startNfcSession(
    ScanFlowCubit cubit,
    NfcReader nfcReader,
  ) async {
    if (_nfcSessionActive) {
      return;
    }

    _nfcSessionActive = true;
    try {
      await nfcReader.startSession(
        onPayload: (payload) async {
          await _stopNfcSession();
          await cubit.onNfcPayloadRead(payload);
        },
      );
    } catch (_) {
      _nfcSessionActive = false;
      cubit.enableQrFallback();
    }
  }

  Future<void> _stopNfcSession() async {
    if (!_nfcSessionActive) {
      return;
    }
    await _nfcReader?.stopSession();
    _nfcSessionActive = false;
  }

  Future<void> _resumeNfcFromQrFallback() async {
    final cubit = _flowCubit;
    if (cubit == null) {
      return;
    }

    await _qrController.stop();
    if (!mounted) {
      return;
    }
    await cubit.resumeWaitingForNfc();
    await _syncNfcSession();
  }

  Future<void> _handleQrDetect(BarcodeCapture capture) async {
    if (_handlingQr) {
      return;
    }

    final raw = capture.barcodes
        .map((barcode) => barcode.rawValue)
        .whereType<String>()
        .map((value) => value.trim())
        .firstWhere((value) => value.isNotEmpty, orElse: () => '');

    if (raw.isEmpty) {
      return;
    }

    final cubit = _flowCubit;
    if (cubit == null) {
      return;
    }

    _handlingQr = true;
    await _qrController.stop();
    await cubit.onQrPayloadRead(raw);
    _handlingQr = false;
  }

  void _onFlowStateChanged(BuildContext context, ScanFlowState state) {
    if (state.phase != ScanFlowPhase.waitingForNfc) {
      _stopNfcSession();
    }

    if (state.awaitingCollectConfirmation && state.resolvedStation != null) {
      context.push(RouteNames.scanLocationVerification);
      return;
    }

    if (state.phase == ScanFlowPhase.success) {
      context.push(RouteNames.scanSuccess);
      return;
    }

    if (_isTerminalPhase(state.phase) &&
        state.phase != ScanFlowPhase.success &&
        !state.awaitingCollectConfirmation) {
      context.push(RouteNames.scanError);
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<ScanFlowCubit, ScanFlowState>(
      listenWhen: (previous, current) =>
          previous.phase != current.phase ||
          previous.awaitingCollectConfirmation !=
              current.awaitingCollectConfirmation,
      listener: _onFlowStateChanged,
      child: Scaffold(
        backgroundColor: AppColors.backgroundWhite,
        appBar: AppBar(
          backgroundColor: AppColors.backgroundWhite,
          foregroundColor: AppColors.textPrimary,
          title: const Text('Chạm NFC'),
          centerTitle: true,
        ),
        body: BlocBuilder<ScanFlowCubit, ScanFlowState>(
          builder: (context, state) {
            if (state.phase == ScanFlowPhase.checkingNfcAvailability ||
                state.phase == ScanFlowPhase.readingNfc ||
                state.phase == ScanFlowPhase.checkingLocation ||
                state.phase == ScanFlowPhase.resolvingStation ||
                state.phase == ScanFlowPhase.collecting) {
              return const AppLoadingView(
                message: 'Đang xử lý mã quét...',
              );
            }

            if (state.phase == ScanFlowPhase.qrFallbackReady) {
              return _QrFallbackView(
                controller: _qrController,
                onDetect: _handleQrDetect,
                onUseNfc: state.nfcAvailability == NfcAvailabilityStatus.enabled
                    ? _resumeNfcFromQrFallback
                    : null,
              );
            }

            return _NfcWaitingView(
              nfcAvailability: state.nfcAvailability,
              onStartNfc: _syncNfcSession,
              onUseQr: state.qrFallbackAvailable
                  ? () {
                      _stopNfcSession();
                      _flowCubit?.enableQrFallback();
                      _qrController.start();
                    }
                  : null,
            );
          },
        ),
      ),
    );
  }
}

class _NfcWaitingView extends StatelessWidget {
  const _NfcWaitingView({
    required this.nfcAvailability,
    required this.onStartNfc,
    this.onUseQr,
  });

  final NfcAvailabilityStatus? nfcAvailability;
  final VoidCallback onStartNfc;
  final VoidCallback? onUseQr;

  @override
  Widget build(BuildContext context) {
    final subtitle = switch (nfcAvailability) {
      NfcAvailabilityStatus.iosTestBuildDisabled =>
        'NFC tạm tắt trên bản test iOS. Bạn có thể dùng QR fallback.',
      NfcAvailabilityStatus.disabled =>
        'Hãy bật NFC trong Cài đặt để tiếp tục.',
      NfcAvailabilityStatus.unavailable =>
        'Thiết bị không hỗ trợ NFC. Dùng QR fallback nếu cần.',
      _ =>
        'Đưa mặt lưng điện thoại lại gần tag NFC tại ga và giữ yên vài giây.',
    };

    return Padding(
      padding: const EdgeInsets.all(AppSpacing.xl),
      child: Column(
        children: [
          ScanHeroCard(
            title: 'Chạm NFC để thu stamp',
            subtitle: subtitle,
            icon: Icons.nfc_rounded,
          ),
          const Spacer(),
          if (nfcAvailability == NfcAvailabilityStatus.enabled)
            ScanPrimaryButton(
              label: 'Bắt đầu quét NFC',
              onPressed: onStartNfc,
            ),
          if (onUseQr != null) ...[
            const SizedBox(height: AppSpacing.sm),
            ScanOutlineButton(
              label: 'Dùng QR fallback',
              onPressed: onUseQr,
            ),
          ],
        ],
      ),
    );
  }
}

class _QrFallbackView extends StatelessWidget {
  const _QrFallbackView({
    required this.controller,
    required this.onDetect,
    this.onUseNfc,
  });

  final MobileScannerController controller;
  final void Function(BarcodeCapture capture) onDetect;
  final VoidCallback? onUseNfc;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'QR fallback',
            style: AppTextStyles.titleMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.xs),
          const Text(
            'Chỉ dùng khi NFC không khả dụng.',
            style: AppTextStyles.bodyMedium,
          ),
          const SizedBox(height: AppSpacing.lg),
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(18),
              child: MobileScanner(
                controller: controller,
                onDetect: onDetect,
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.lg),
          if (onUseNfc != null)
            ScanOutlineButton(
              label: 'Quay lại NFC',
              onPressed: onUseNfc,
            ),
        ],
      ),
    );
  }
}
