class AuthFormUtils {
  const AuthFormUtils._();

  static ({String firstname, String lastname}) splitFullName(String fullName) {
    final parts =
        fullName.trim().split(RegExp(r'\s+')).where((part) => part.isNotEmpty);
    final values = parts.toList();
    if (values.isEmpty) {
      return (firstname: '', lastname: '');
    }
    if (values.length == 1) {
      return (firstname: values.first, lastname: values.first);
    }
    return (
      firstname: values.first,
      lastname: values.sublist(1).join(' '),
    );
  }

  static String usernameFromEmail(String email) {
    final localPart = email.trim().split('@').first;
    final normalized =
        localPart.replaceAll(RegExp(r'[^a-zA-Z0-9._-]'), '').toLowerCase();
    return normalized.isEmpty ? 'user' : normalized;
  }

  static String normalizePhone(String dialCode, String phone) {
    final digits = phone.replaceAll(RegExp(r'\D'), '');
    final codeDigits = dialCode.replaceAll(RegExp(r'\D'), '');
    if (digits.startsWith(codeDigits)) {
      return '+$digits';
    }
    return '$dialCode$digits';
  }
}
