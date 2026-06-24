import 'router/route_names.dart';

/// Legacy route constants for existing feature pages during go_router migration.
///
/// Prefer [RouteNames] and `context.go()` / `context.push()` in new code.
/// `Navigator.pushNamed` is not compatible with [MaterialApp.router].
class AppRouter {
  static const welcome = RouteNames.welcome;
  static const login = RouteNames.login;
  static const auth = RouteNames.auth;
  static const register = RouteNames.register;
  static const forgotPassword = RouteNames.forgotPassword;
  static const home = RouteNames.home;
  static const stations = RouteNames.stations;
  static const scan = RouteNames.scan;
  static const stampBook = RouteNames.stampBook;
  static const rewards = RouteNames.rewards;
  static const profile = RouteNames.profile;
  static const settings = RouteNames.settings;
}
