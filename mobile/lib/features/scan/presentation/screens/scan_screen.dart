import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/config/scan_capabilities.dart';
import '../../../../core/nfc/nfc_availability.dart';
import '../../../../core/nfc/nfc_reader.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';
import '../cubit/scan_flow_cubit.dart';
import '../cubit/scan_flow_state.dart';
import '../widgets/nfc_pulse_circle.dart';
import '../widgets/scan_action_buttons.dart';
import '../widgets/scan_flow_listener.dart';
import '../widgets/scan_pro_tip_card.dart';

class ScanScreen extends StatefulWidget {
  const ScanScreen({super.key});

  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> with WidgetsBindingObserver {
  // QR scanner retained for future re-enable via ScanCapabilities.enableQrFlow.
  MobileScannerController? _qrController;

  bool _handlingQr = false;
  bool _nfcSessionActive = false;
  ScanFlowCubit? _flowCubit;
  NfcReader? _nfcReader;

  bool get _qrEnabled => ScanCapabilities.enableQrFlow;

  MobileScannerController get _requireQrController {
    return _qrController ??= MobileScannerController(
      formats: const [BarcodeFormat.qrCode],
      detectionSpeed: DetectionSpeed.noDuplicates,
    );
  }

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
    _qrController?.dispose();
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
      _qrController?.stop();
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

    await _qrController?.stop();
    if (!mounted) {
      return;
    }
    await cubit.resumeWaitingForNfc();
    await _syncNfcSession();
  }

  Future<void> _handleQrDetect(BarcodeCapture capture) async {
    if (!_qrEnabled || _handlingQr) {
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
    await _qrController?.stop();
    await cubit.onQrPayloadRead(raw);
    _handlingQr = false;
  }

  void _cancelScan() {
    _stopNfcSession();
    _qrController?.stop();
    _flowCubit?.resetFlow();
    context.go(RouteNames.scanTapToCollect);
  }

  @override
  Widget build(BuildContext context) {
    return ScanFlowListener(
      child: BlocListener<ScanFlowCubit, ScanFlowState>(
        listenWhen: (previous, current) =>
            previous.phase != current.phase &&
            current.phase == ScanFlowPhase.waitingForNfc,
        listener: (_, __) => _syncNfcSession(),
        child: Scaffold(
          backgroundColor: AppColors.backgroundWhite,
          appBar: AppSecondaryAppBar(
            title: 'Quét NFC',
            showBottomDivider: false,
            onBack: _cancelScan,
          ),
          body: BlocBuilder<ScanFlowCubit, ScanFlowState>(
            builder: (context, state) {
              if (state.phase == ScanFlowPhase.checkingNfcAvailability ||
                  state.phase == ScanFlowPhase.readingNfc ||
                  state.phase == ScanFlowPhase.checkingLocation ||
                  state.phase == ScanFlowPhase.resolvingStation ||
                  state.phase == ScanFlowPhase.collecting ||
                  state.phase == ScanFlowPhase.checkingCollectStatus) {
                return const AppLoadingView(message: 'Đang xử lý...');
              }

              // QR fallback view: only when ScanCapabilities.enableQrFlow.
              if (_qrEnabled &&
                  state.phase == ScanFlowPhase.qrFallbackReady) {
                return _QrFallbackView(
                  controller: _requireQrController,
                  onDetect: _handleQrDetect,
                  onCancel: _cancelScan,
                  onUseNfc:
                      state.nfcAvailability == NfcAvailabilityStatus.enabled
                          ? _resumeNfcFromQrFallback
                          : null,
                );
              }

              return _NfcScanView(
                state: state,
                nfcSessionActive: _nfcSessionActive,
                onCancel: _cancelScan,
                onUseQr: _qrEnabled && state.qrFallbackAvailable
                    ? () {
                        _stopNfcSession();
                        _flowCubit?.enableQrFallback();
                        _requireQrController.start();
                      }
                    : null,
              );
            },
          ),
        ),
      ),
    );
  }
}

class _NfcScanView extends StatelessWidget {
  const _NfcScanView({
    required this.state,
    required this.nfcSessionActive,
    required this.onCancel,
    this.onUseQr,
  });

  final ScanFlowState state;
  final bool nfcSessionActive;
  final VoidCallback onCancel;
  final VoidCallback? onUseQr;

  String get _statusTitle {
    return switch (state.nfcAvailability) {
      NfcAvailabilityStatus.disabled => 'NFC đang tắt',
      NfcAvailabilityStatus.unavailable => 'NFC không hỗ trợ',
      NfcAvailabilityStatus.iosTestBuildDisabled =>
        'NFC tạm không khả dụng trên thiết bị này',
      _ => nfcSessionActive ? 'Đang quét...' : 'Sẵn sàng quét',
    };
  }

  String get _statusSubtitle {
    // NFC-only copy when QR flow is temporarily disabled.
    if (!ScanCapabilities.enableQrFlow) {
      return switch (state.nfcAvailability) {
        NfcAvailabilityStatus.disabled =>
          'Bật NFC trong Cài đặt để thu stamp tại ga.',
        NfcAvailabilityStatus.unavailable =>
          'Thiết bị không hỗ trợ NFC. Không thể thu stamp trên thiết bị này.',
        NfcAvailabilityStatus.iosTestBuildDisabled =>
          'NFC tạm không khả dụng trên thiết bị này. Thử lại trên thiết bị hỗ trợ NFC.',
        _ =>
          'Chạm điện thoại vào tag NFC tại ga và giữ yên vài giây.',
      };
    }

    return switch (state.nfcAvailability) {
      NfcAvailabilityStatus.disabled =>
        'Bật NFC trong Cài đặt hoặc dùng mã QR.',
      NfcAvailabilityStatus.unavailable =>
        'Thiết bị không hỗ trợ NFC. Dùng mã QR nếu cần.',
      NfcAvailabilityStatus.iosTestBuildDisabled =>
        'NFC tạm không khả dụng trên thiết bị này. Dùng mã QR nếu cần.',
      _ =>
        'Chạm điện thoại vào tag NFC tại ga và giữ yên vài giây.',
    };
  }

  @override
  Widget build(BuildContext context) {
    final isScanning =
        state.nfcAvailability == NfcAvailabilityStatus.enabled &&
            nfcSessionActive;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppSpacing.xxl),
      child: Column(
        children: [
          Text(
            _statusTitle,
            style: AppTextStyles.displayMedium.copyWith(
              color: AppColors.primaryBlue,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            _statusSubtitle,
            style: AppTextStyles.bodyLarge.copyWith(
              color: AppColors.textSecondary,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpacing.xxxl),
          NfcPulseCircle(isScanning: isScanning),
          const SizedBox(height: AppSpacing.xxxl),
          const ScanProTipCard(),
          const SizedBox(height: AppSpacing.xxxl),
          ScanOutlineButton(label: 'Hủy quét', onPressed: onCancel),
          if (onUseQr != null) ...[
            const SizedBox(height: AppSpacing.md),
            TextButton(
              onPressed: onUseQr,
              child: Text(
                'Dùng mã QR',
                style: AppTextStyles.bodyMedium.copyWith(
                  color: AppColors.textSecondary,
                  decoration: TextDecoration.underline,
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

/// Retained for possible future re-enable via [ScanCapabilities.enableQrFlow].
class _QrFallbackView extends StatelessWidget {
  const _QrFallbackView({
    required this.controller,
    required this.onDetect,
    required this.onCancel,
    this.onUseNfc,
  });

  final MobileScannerController controller;
  final void Function(BarcodeCapture capture) onDetect;
  final VoidCallback onCancel;
  final VoidCallback? onUseNfc;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(AppSpacing.xl),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Quét mã QR',
            style: AppTextStyles.sectionTitle.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.sm),
          Text(
            'Chỉ dùng khi NFC không khả dụng hoặc bạn chọn quét mã QR.',
            style: AppTextStyles.bodyMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.xl),
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(18),
              child: ColoredBox(
                color: AppColors.surface,
                child: MobileScanner(
                  controller: controller,
                  onDetect: onDetect,
                ),
              ),
            ),
          ),
          const SizedBox(height: AppSpacing.xl),
          ScanOutlineButton(label: 'Hủy quét', onPressed: onCancel),
          if (onUseNfc != null) ...[
            const SizedBox(height: AppSpacing.md),
            ScanPrimaryButton(
              label: 'Quay lại NFC',
              onPressed: onUseNfc,
            ),
          ],
        ],
      ),
    );
  }
}
