import 'route_names.dart';

/// Query-parameter based refresh signal for [RouteNames.stampBook].
///
/// Scan success navigates with a new token so the shell-persisted stamp book
/// refetches from the backend without locally inserting collect results.
abstract final class StampBookRouteRefresh {
  static const queryKey = 'refresh';

  static String? readToken(Uri uri) {
    final token = uri.queryParameters[queryKey];
    if (token == null || token.isEmpty) {
      return null;
    }
    return token;
  }

  static String locationWithRefresh(String refreshToken) {
    return '${RouteNames.stampBook}?$queryKey=$refreshToken';
  }

  static String newRefreshToken() {
    return DateTime.now().microsecondsSinceEpoch.toString();
  }
}
