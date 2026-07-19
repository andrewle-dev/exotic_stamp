class Validators {
  /// Aligned with backend [PasswordPolicy] (min 8, max 50).
  static const minPasswordLength = 8;
  static const maxPasswordLength = 50;

  static bool isNotEmpty(String value) => value.trim().isNotEmpty;

  static bool isValidEmail(String value) {
    final normalized = value.trim();
    if (normalized.isEmpty) {
      return false;
    }
    return RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(normalized);
  }

  static bool isValidPhone(String value) {
    final normalized = value.replaceAll(RegExp(r'\s+'), '');
    return RegExp(r'^\d{8,15}$').hasMatch(normalized);
  }

  static bool isValidPassword(String value) =>
      value.length >= minPasswordLength && value.length <= maxPasswordLength;
}
