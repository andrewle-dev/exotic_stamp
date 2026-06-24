import '../config/api_config.dart';

/// Resolves relative backend media paths to absolute URLs.
class MediaUrlResolver {
  MediaUrlResolver({String? mediaOrigin})
      : _mediaOrigin = mediaOrigin ?? ApiConfig.mediaOrigin;

  final String _mediaOrigin;

  String? resolve(String? pathOrUrl) {
    if (pathOrUrl == null || pathOrUrl.isEmpty) {
      return null;
    }
    if (pathOrUrl.startsWith('http://') || pathOrUrl.startsWith('https://')) {
      return pathOrUrl;
    }
    final normalizedPath =
        pathOrUrl.startsWith('/') ? pathOrUrl : '/$pathOrUrl';
    return '$_mediaOrigin$normalizedPath';
  }
}
