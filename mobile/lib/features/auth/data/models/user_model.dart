import '../../domain/entities/user.dart';

class UserModel {
  const UserModel({
    required this.id,
    required this.email,
    required this.username,
    this.firstname,
    this.lastname,
    this.phoneNumber,
    this.avatarUrl,
    this.roles = const [],
  });

  factory UserModel.fromAuthUserInfo(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as String,
      email: json['email'] as String? ?? '',
      username: json['username'] as String? ?? '',
      roles: (json['roles'] as List<dynamic>?)
              ?.map((role) => role.toString())
              .toList() ??
          const [],
    );
  }

  factory UserModel.fromUserResponse(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as String,
      email: json['email'] as String? ?? '',
      username: json['username'] as String? ?? '',
      firstname: json['firstname'] as String?,
      lastname: json['lastname'] as String?,
      phoneNumber: json['phoneNumber'] as String?,
      avatarUrl: json['avatarUrl'] as String?,
    );
  }

  final String id;
  final String email;
  final String username;
  final String? firstname;
  final String? lastname;
  final String? phoneNumber;
  final String? avatarUrl;
  final List<String> roles;

  User toEntity() {
    return User(
      id: id,
      email: email,
      username: username,
      firstname: firstname,
      lastname: lastname,
      phoneNumber: phoneNumber,
      avatarUrl: avatarUrl,
      roles: roles,
    );
  }
}
