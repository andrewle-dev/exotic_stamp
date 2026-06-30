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
    this.actionKey,
  });

  final AuthStatus status;
  final Session? session;
  final String? message;
  final Failure? failure;
  final String? actionKey;

  AuthState copyWith({
    AuthStatus? status,
    Session? session,
    String? message,
    Failure? failure,
    String? actionKey,
    bool clearMessage = false,
    bool clearFailure = false,
    bool clearSession = false,
    bool clearActionKey = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      session: clearSession ? null : session ?? this.session,
      message: clearMessage ? null : message ?? this.message,
      failure: clearFailure ? null : failure ?? this.failure,
      actionKey: clearActionKey ? null : actionKey ?? this.actionKey,
    );
  }

  @override
  List<Object?> get props => [status, session, message, failure, actionKey];
}
