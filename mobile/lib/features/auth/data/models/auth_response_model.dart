import '../../domain/entities/session.dart';
import 'user_model.dart';

class AuthResponseModel {
  const AuthResponseModel({
    required this.accessToken,
    required this.tokenType,
    required this.user,
  });

  factory AuthResponseModel.fromJson(Map<String, dynamic> json) {
    return AuthResponseModel(
      accessToken: json['accessToken'] as String,
      tokenType: json['tokenType'] as String? ?? 'Bearer',
      user: UserModel.fromAuthUserInfo(
        json['userInfo'] as Map<String, dynamic>,
      ),
    );
  }

  final String accessToken;
  final String tokenType;
  final UserModel user;

  Session toSession() {
    return Session(
      accessToken: accessToken,
      tokenType: tokenType,
      user: user.toEntity(),
    );
  }
}
