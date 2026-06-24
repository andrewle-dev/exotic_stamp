import 'package:equatable/equatable.dart';

class User extends Equatable {
  const User({
    required this.id,
    required this.email,
    required this.username,
    this.firstname,
    this.lastname,
    this.phoneNumber,
    this.avatarUrl,
    this.roles = const [],
  });

  final String id;
  final String email;
  final String username;
  final String? firstname;
  final String? lastname;
  final String? phoneNumber;
  final String? avatarUrl;
  final List<String> roles;

  String get displayName {
    final first = firstname?.trim();
    final last = lastname?.trim();
    if (first != null && first.isNotEmpty && last != null && last.isNotEmpty) {
      return '$first $last';
    }
    if (first != null && first.isNotEmpty) {
      return first;
    }
    return username.isNotEmpty ? username : email;
  }

  @override
  List<Object?> get props => [
        id,
        email,
        username,
        firstname,
        lastname,
        phoneNumber,
        avatarUrl,
        roles,
      ];
}
