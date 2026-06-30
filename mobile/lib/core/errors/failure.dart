import 'package:equatable/equatable.dart';

/// Typed failure codes mapped from backend and client-side errors.
enum FailureCode {
  invalidCredentials,
  accountNotVerified,
  tokenExpired,
  unauthorized,
  emailTaken,
  validationError,
  stampDuplicate,
  nfcInvalid,
  qrExpired,
  gpsOutsideRange,
  stationInactive,
  campaignInactive,
  redeemNotSupported,
  networkError,
  unknown,
}

/// Domain-level failure surfaced to presentation layers.
class Failure extends Equatable implements Exception {
  const Failure({
    required this.code,
    required this.message,
    this.statusCode,
    this.backendCode,
  });

  final FailureCode code;
  final String message;
  final int? statusCode;
  final String? backendCode;

  bool get isAuthFailure =>
      code == FailureCode.invalidCredentials ||
      code == FailureCode.accountNotVerified ||
      code == FailureCode.tokenExpired ||
      code == FailureCode.unauthorized;

  bool get requiresAccountVerification =>
      code == FailureCode.accountNotVerified;

  @override
  List<Object?> get props => [code, message, statusCode, backendCode];
}
