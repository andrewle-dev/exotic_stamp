import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/session.dart';

enum AuthStatus {
  initial,
  loading,
  authenticated,
  unauthenticated,
  actionSuccess,
  failure,
}

class AuthState extends Equatable {
  const AuthState({
    this.status = AuthStatus.initial,
    this.session,
    this.message,
    this.failure,
  });

  final AuthStatus status;
  final Session? session;
  final String? message;
  final Failure? failure;

  AuthState copyWith({
    AuthStatus? status,
    Session? session,
    String? message,
    Failure? failure,
    bool clearMessage = false,
    bool clearFailure = false,
    bool clearSession = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      session: clearSession ? null : session ?? this.session,
      message: clearMessage ? null : message ?? this.message,
      failure: clearFailure ? null : failure ?? this.failure,
    );
  }

  @override
  List<Object?> get props => [status, session, message, failure];
}
