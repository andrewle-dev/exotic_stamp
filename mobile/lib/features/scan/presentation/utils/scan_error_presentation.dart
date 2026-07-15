import 'package:flutter/material.dart';

import '../../../../core/config/scan_capabilities.dart';
import '../cubit/scan_flow_state.dart';

class ScanErrorPresentation {
  const ScanErrorPresentation({
    required this.errorCode,
    required this.title,
    required this.message,
    required this.tagLabel,
    required this.icon,
    this.primaryActionLabel = 'Thử lại',
    this.secondaryActionLabel = 'Quay lại trang ga',
  });

  final String errorCode;
  final String title;
  final String message;
  final String tagLabel;
  final IconData icon;
  final String primaryActionLabel;
  final String secondaryActionLabel;

  static ScanErrorPresentation forPhase(ScanFlowState state) {
    final phase = state.phase;
    final fallbackMessage =
        state.statusMessage ?? state.failure?.message ?? 'Đã xảy ra lỗi khi thu stamp.';
    const qrEnabled = ScanCapabilities.enableQrFlow;

    return switch (phase) {
      ScanFlowPhase.duplicate => const ScanErrorPresentation(
          errorCode: 'STAMP_DUPLICATE',
          title: 'Đã nhận stamp rồi',
          message:
              'Bạn đã thu stamp tại ga này. Mỗi ga chỉ thu một lần trong ngày.',
          tagLabel: 'STAMP',
          icon: Icons.collections_bookmark_outlined,
          primaryActionLabel: 'Mở Sổ stamp',
          secondaryActionLabel: 'Quay lại trang ga',
        ),
      // QR-expired phase retained for API mapping; copy is NFC-neutral when QR UI is off.
      ScanFlowPhase.qrExpired => qrEnabled
          ? const ScanErrorPresentation(
              errorCode: 'QR_EXPIRED',
              title: 'Mã QR đã hết hạn',
              message:
                  'Mã QR chỉ có hiệu lực trong thời gian ngắn. Hãy quét mã mới tại ga.',
              tagLabel: 'LỖI QR',
              icon: Icons.error_outline_rounded,
            )
          : const ScanErrorPresentation(
              errorCode: 'SCAN_KEY_INACTIVE',
              title: 'Mã quét không còn hiệu lực',
              message:
                  'Mã quét tại ga không còn hiệu lực. Hãy thử lại với thẻ NFC khác.',
              tagLabel: 'MÃ QUÉT',
              icon: Icons.error_outline_rounded,
            ),
      ScanFlowPhase.gpsOutsideRange => const ScanErrorPresentation(
          errorCode: 'GPS_OUTSIDE_RANGE',
          title: 'Bạn đang ở ngoài khu vực',
          message:
              'Bạn cần ở trong bán kính 50m quanh ga để thu stamp. Kiểm tra lại GPS.',
          tagLabel: 'VỊ TRÍ',
          icon: Icons.location_off_outlined,
        ),
      ScanFlowPhase.invalidTag => qrEnabled
          ? const ScanErrorPresentation(
              errorCode: 'NFC_INVALID',
              title: 'Tag NFC không hợp lệ',
              message:
                  'Không nhận diện được tag NFC. Hãy thử chạm lại hoặc dùng QR fallback.',
              tagLabel: 'NFC',
              icon: Icons.nfc_rounded,
            )
          : const ScanErrorPresentation(
              errorCode: 'NFC_INVALID',
              title: 'Tag NFC không hợp lệ',
              message: 'Không nhận diện được tag NFC. Hãy thử chạm lại.',
              tagLabel: 'NFC',
              icon: Icons.nfc_rounded,
            ),
      ScanFlowPhase.stationInactive => const ScanErrorPresentation(
          errorCode: 'STATION_INACTIVE',
          title: 'Ga không hoạt động',
          message: 'Ga này hiện không thu stamp. Vui lòng thử ga khác.',
          tagLabel: 'GA',
          icon: Icons.train_outlined,
        ),
      ScanFlowPhase.campaignInactive => const ScanErrorPresentation(
          errorCode: 'CAMPAIGN_INACTIVE',
          title: 'Chiến dịch không khả dụng',
          message: 'Chiến dịch stamp hiện không hoạt động. Thử lại sau.',
          tagLabel: 'CHIẾN DỊCH',
          icon: Icons.campaign_outlined,
        ),
      ScanFlowPhase.networkError => ScanErrorPresentation(
          errorCode: 'NETWORK_ERROR',
          title: 'Lỗi kết nối',
          message: fallbackMessage,
          tagLabel: 'MẠNG',
          icon: Icons.wifi_off_rounded,
          primaryActionLabel: state.isUncertainOutcome
              ? 'Kiểm tra trạng thái'
              : 'Thử lại',
        ),
      ScanFlowPhase.locationPermissionDenied => const ScanErrorPresentation(
          errorCode: 'PERMISSION_DENIED',
          title: 'Cần quyền vị trí',
          message: 'Ứng dụng cần quyền vị trí để xác minh bạn đang ở ga.',
          tagLabel: 'VỊ TRÍ',
          icon: Icons.location_disabled_outlined,
        ),
      ScanFlowPhase.locationServiceDisabled => const ScanErrorPresentation(
          errorCode: 'PERMISSION_DENIED',
          title: 'Bật dịch vụ vị trí',
          message: 'Hãy bật Location Services để xác minh vị trí tại ga.',
          tagLabel: 'VỊ TRÍ',
          icon: Icons.location_off_outlined,
        ),
      ScanFlowPhase.locationLowAccuracy => ScanErrorPresentation(
          errorCode: 'GPS_OUTSIDE_RANGE',
          title: 'GPS không đủ chính xác',
          message: fallbackMessage,
          tagLabel: 'VỊ TRÍ',
          icon: Icons.gps_not_fixed_outlined,
        ),
      ScanFlowPhase.locationTimeout => ScanErrorPresentation(
          errorCode: 'NETWORK_ERROR',
          title: 'Không lấy được vị trí',
          message: fallbackMessage,
          tagLabel: 'VỊ TRÍ',
          icon: Icons.timer_off_outlined,
        ),
      _ => ScanErrorPresentation(
          errorCode: 'UNKNOWN',
          title: 'Không thể thu stamp',
          message: fallbackMessage,
          tagLabel: 'LỖI',
          icon: Icons.error_outline_rounded,
        ),
    };
  }
}
