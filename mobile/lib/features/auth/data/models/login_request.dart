class LoginRequest {
  const LoginRequest({
    required this.identifier,
    required this.password,
    required this.deviceFingerprint,
  });

  final String identifier;
  final String password;
  final String deviceFingerprint;

  Map<String, dynamic> toJson() {
    return {
      'identifier': identifier,
      'password': password,
      'deviceFingerprint': deviceFingerprint,
    };
  }
}
