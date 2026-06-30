class VerifyAccountRequest {
  const VerifyAccountRequest({
    required this.email,
    required this.otp,
  });

  final String email;
  final String otp;

  Map<String, dynamic> toJson() => {
        'email': email,
        'otp': otp,
      };
}
