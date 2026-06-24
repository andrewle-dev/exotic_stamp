class UpdateProfileRequestModel {
  const UpdateProfileRequestModel({
    this.firstname,
    this.lastname,
    this.bio,
    this.avatarUrl,
  });

  final String? firstname;
  final String? lastname;
  final String? bio;
  final String? avatarUrl;

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (firstname != null) {
      json['firstname'] = firstname;
    }
    if (lastname != null) {
      json['lastname'] = lastname;
    }
    if (bio != null) {
      json['bio'] = bio;
    }
    if (avatarUrl != null) {
      json['avatarUrl'] = avatarUrl;
    }
    return json;
  }
}
