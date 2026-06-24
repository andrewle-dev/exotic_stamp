import 'route_names.dart';

/// Query-parameter refresh signal for [RouteNames.rewards].
///
/// Scan success can navigate with a new token so the shell-persisted rewards tab
/// refetches from the backend without locally inserting reward data.
abstract final class RewardsRouteRefresh {
  static const queryKey = 'refresh';

  static String? readToken(Uri uri) {
    final token = uri.queryParameters[queryKey];
    if (token == null || token.isEmpty) {
      return null;
    }
    return token;
  }

  static String locationWithRefresh(String refreshToken) {
    return '${RouteNames.rewards}?$queryKey=$refreshToken';
  }

  static String newRefreshToken() {
    return DateTime.now().microsecondsSinceEpoch.toString();
  }
}
