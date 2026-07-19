import 'package:dio/dio.dart';

import 'failure.dart';

/// Maps backend error envelopes and transport errors into [Failure].
class ErrorMapper {
  const ErrorMapper();

  static const backendCodeMap = <String, FailureCode>{
    'INVALID_CREDENTIALS': FailureCode.invalidCredentials,
    'CURRENT_PASSWORD_INCORRECT': FailureCode.invalidCredentials,
    'PASSWORD_CONFIRMATION_MISMATCH': FailureCode.validationError,
    'NEW_PASSWORD_SAME_AS_CURRENT': FailureCode.validationError,
    'PASSWORD_POLICY_VIOLATION': FailureCode.validationError,
    'ACCOUNT_NOT_VERIFIED': FailureCode.accountNotVerified,
    'OTP_EXPIRED': FailureCode.validationError,
    'RESEND_COOLDOWN': FailureCode.validationError,
    'TOKEN_EXPIRED': FailureCode.tokenExpired,
    'EMAIL_TAKEN': FailureCode.emailTaken,
    'VALIDATION_ERROR': FailureCode.validationError,
    'INVALID_INPUT': FailureCode.validationError,
    'OTP_INVALID': FailureCode.validationError,
    'UNAUTHORIZED': FailureCode.unauthorized,
    'INVALID_TOKEN': FailureCode.unauthorized,
    'STAMP_ALREADY_COLLECTED': FailureCode.stampDuplicate,
    'STAMP_DUPLICATE': FailureCode.stampDuplicate,
    'SCAN_KEY_NOT_FOUND': FailureCode.nfcInvalid,
    'SCAN_PAYLOAD_INVALID': FailureCode.nfcInvalid,
    'INVALID_SCAN_METHOD': FailureCode.nfcInvalid,
    'NFC_INVALID': FailureCode.nfcInvalid,
    'SCAN_KEY_INACTIVE': FailureCode.qrExpired,
    'QR_EXPIRED': FailureCode.qrExpired,
    'GPS_OUT_OF_RANGE': FailureCode.gpsOutsideRange,
    'GPS_OUTSIDE_RANGE': FailureCode.gpsOutsideRange,
    'STATION_INACTIVE': FailureCode.stationInactive,
    'CAMPAIGN_NOT_ACTIVE': FailureCode.campaignInactive,
    'CAMPAIGN_ARCHIVED': FailureCode.campaignInactive,
    'CAMPAIGN_INACTIVE': FailureCode.campaignInactive,
    'DEFAULT_CAMPAIGN_AMBIGUOUS': FailureCode.defaultCampaignAmbiguous,
    'DEFAULT_CAMPAIGN_NOT_FOUND': FailureCode.campaignInactive,
    'REDEEM_NOT_SUPPORTED': FailureCode.redeemNotSupported,
    'NETWORK_ERROR': FailureCode.networkError,
  };

  static const defaultCampaignAmbiguousMessage =
      'Không xác định được chiến dịch mặc định cho sổ sưu tập. Vui lòng chọn tuyến.';

  Failure fromDioException(DioException exception) {
    if (_isNetworkFailure(exception)) {
      return const Failure(
        code: FailureCode.networkError,
        message: 'Không thể kết nối. Kiểm tra mạng và thử lại.',
      );
    }

    final response = exception.response;
    if (response?.data is Map<String, dynamic>) {
      return fromJson(
        response!.data as Map<String, dynamic>,
        statusCode: response.statusCode,
      );
    }

    return Failure(
      code: FailureCode.unknown,
      message: exception.message ?? 'Đã xảy ra lỗi không xác định.',
      statusCode: response?.statusCode,
    );
  }

  Failure fromJson(
    Map<String, dynamic> json, {
    int? statusCode,
  }) {
    final backendCode = json['code'] as String?;
    final message = (json['message'] as String?)?.trim();
    var resolvedCode = backendCode != null
        ? backendCodeMap[backendCode] ?? FailureCode.unknown
        : FailureCode.unknown;

    if (resolvedCode == FailureCode.defaultCampaignAmbiguous ||
        _isAmbiguousDefaultCampaignMessage(message)) {
      resolvedCode = FailureCode.defaultCampaignAmbiguous;
      return Failure(
        code: resolvedCode,
        message: defaultCampaignAmbiguousMessage,
        statusCode: statusCode ?? json['status'] as int?,
        backendCode: backendCode ?? 'DEFAULT_CAMPAIGN_AMBIGUOUS',
      );
    }

    return Failure(
      code: resolvedCode,
      message: message ?? _defaultMessage(resolvedCode),
      statusCode: statusCode ?? json['status'] as int?,
      backendCode: backendCode,
    );
  }

  bool _isAmbiguousDefaultCampaignMessage(String? message) {
    if (message == null || message.isEmpty) {
      return false;
    }
    final normalized = message.toLowerCase();
    return normalized.contains('multiple active default campaigns') ||
        normalized.contains('provide lineid to disambiguate');
  }

  bool _isNetworkFailure(DioException exception) {
    return exception.type == DioExceptionType.connectionError ||
        exception.type == DioExceptionType.connectionTimeout ||
        exception.type == DioExceptionType.receiveTimeout ||
        exception.type == DioExceptionType.sendTimeout;
  }

  String _defaultMessage(FailureCode code) {
    return switch (code) {
      FailureCode.invalidCredentials =>
        'Email/tên đăng nhập hoặc mật khẩu không đúng.',
      FailureCode.accountNotVerified =>
        'Tài khoản chưa được xác minh. Vui lòng nhập mã OTP đã gửi qua email.',
      FailureCode.tokenExpired => 'Phiên đăng nhập đã hết hạn.',
      FailureCode.emailTaken => 'Email này đã được đăng ký.',
      FailureCode.validationError =>
        'Thông tin chưa hợp lệ. Vui lòng kiểm tra lại.',
      FailureCode.unauthorized => 'Bạn cần đăng nhập để tiếp tục.',
      FailureCode.stampDuplicate => 'Bạn đã thu thập stamp tại ga này.',
      FailureCode.nfcInvalid => 'Thẻ NFC không hợp lệ.',
      // Neutral copy: SCAN_KEY_INACTIVE / QR_EXPIRED can apply beyond QR UI.
      FailureCode.qrExpired => 'Mã quét đã hết hạn hoặc không còn hiệu lực.',
      FailureCode.gpsOutsideRange =>
        'Bạn cần ở trong phạm vi ga để thu thập stamp.',
      FailureCode.stationInactive => 'Ga này hiện không hoạt động.',
      FailureCode.campaignInactive => 'Chiến dịch hiện không khả dụng.',
      FailureCode.defaultCampaignAmbiguous => defaultCampaignAmbiguousMessage,
      FailureCode.redeemNotSupported =>
        'Đổi voucher trong app chưa được hỗ trợ.',
      FailureCode.networkError =>
        'Không thể kết nối. Kiểm tra mạng và thử lại.',
      FailureCode.unknown => 'Đã xảy ra lỗi không xác định.',
    };
  }
}
