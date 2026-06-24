import '../../domain/entities/profile.dart';

class ProfileModel {
  const ProfileModel({
    required this.id,
    required this.email,
    required this.username,
    this.firstname,
    this.lastname,
    this.phoneNumber,
    this.avatarUrl,
    this.bio,
    this.createdAt,
    this.stats,
  });

  factory ProfileModel.fromUserResponse(Map<String, dynamic> json) {
    final createdRaw = json['created_at'] as String?;
    return ProfileModel(
      id: json['id'] as String,
      email: json['email'] as String? ?? '',
      username: json['username'] as String? ?? '',
      firstname: json['firstname'] as String?,
      lastname: json['lastname'] as String?,
      phoneNumber: json['phoneNumber'] as String?,
      avatarUrl: json['avatarUrl'] as String?,
      bio: json['bio'] as String?,
      createdAt: createdRaw == null ? null : DateTime.tryParse(createdRaw),
    );
  }

  final String id;
  final String email;
  final String username;
  final String? firstname;
  final String? lastname;
  final String? phoneNumber;
  final String? avatarUrl;
  final String? bio;
  final DateTime? createdAt;
  final ProfileStats? stats;

  ProfileModel copyWithStats({ProfileStats? stats}) {
    return ProfileModel(
      id: id,
      email: email,
      username: username,
      firstname: firstname,
      lastname: lastname,
      phoneNumber: phoneNumber,
      avatarUrl: avatarUrl,
      bio: bio,
      createdAt: createdAt,
      stats: stats ?? this.stats,
    );
  }

  Profile toEntity() {
    return Profile(
      id: id,
      email: email,
      username: username,
      firstname: firstname,
      lastname: lastname,
      phoneNumber: phoneNumber,
      avatarUrl: avatarUrl,
      bio: bio,
      createdAt: createdAt,
      stats: stats,
    );
  }
}
