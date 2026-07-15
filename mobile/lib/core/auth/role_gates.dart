import '../../features/auth/domain/entities/user.dart';

/// Role checks for admin-only mobile utilities.
abstract final class RoleGates {
  static const adminRoles = {'ADMIN', 'OPERATOR'};

  static bool isAdmin(User? user) {
    if (user == null) {
      return false;
    }
    return user.roles.any((role) => adminRoles.contains(role.toUpperCase()));
  }

  static bool hasAdminRole(List<String> roles) {
    return roles.any((role) => adminRoles.contains(role.toUpperCase()));
  }
}
