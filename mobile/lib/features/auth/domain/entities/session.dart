import 'package:equatable/equatable.dart';

import 'user.dart';

class Session extends Equatable {
  const Session({
    required this.accessToken,
    required this.user,
    this.tokenType = 'Bearer',
  });

  final String accessToken;
  final String tokenType;
  final User user;

  @override
  List<Object?> get props => [accessToken, tokenType, user];
}
