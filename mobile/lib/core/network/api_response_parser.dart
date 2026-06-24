/// Parses backend envelopes where some endpoints wrap payloads in `{ data: ... }`.
abstract final class ApiResponseParser {
  static dynamic unwrap(dynamic body) {
    if (body is Map<String, dynamic> && body.containsKey('data')) {
      return body['data'];
    }
    return body;
  }

  static Map<String, dynamic>? asMap(dynamic body) {
    final unwrapped = unwrap(body);
    if (unwrapped is Map<String, dynamic>) {
      return unwrapped;
    }
    return null;
  }

  static List<Map<String, dynamic>> asMapList(dynamic body) {
    final unwrapped = unwrap(body);
    if (unwrapped is List) {
      return unwrapped.whereType<Map<String, dynamic>>().toList();
    }
    return const [];
  }

  static List<Map<String, dynamic>> paginatedContent(dynamic body) {
    final unwrapped = unwrap(body);
    if (unwrapped is Map<String, dynamic>) {
      final content = unwrapped['content'];
      if (content is List) {
        return content.whereType<Map<String, dynamic>>().toList();
      }
    }
    return const [];
  }
}
