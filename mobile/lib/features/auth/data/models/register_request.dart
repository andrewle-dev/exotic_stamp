class RegisterRequest {
  const RegisterRequest({
    required this.firstname,
    required this.lastname,
    required this.username,
    required this.email,
    required this.phoneNumber,
    required this.password,
  });

  final String firstname;
  final String lastname;
  final String username;
  final String email;
  final String phoneNumber;
  final String password;

  Map<String, dynamic> toJson() {
    return {
      'firstname': firstname,
      'lastname': lastname,
      'username': username,
      'email': email,
      'phoneNumber': phoneNumber,
      'password': password,
    };
  }
}
