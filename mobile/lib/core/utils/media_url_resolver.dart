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
      return _rewriteLoopbackUrl(pathOrUrl);
    }
    final normalizedPath =
        pathOrUrl.startsWith('/') ? pathOrUrl : '/$pathOrUrl';
    return '$_mediaOrigin$normalizedPath';
  }

  String _rewriteLoopbackUrl(String absoluteUrl) {
    final sourceUri = Uri.tryParse(absoluteUrl);
    final originUri = Uri.tryParse(_mediaOrigin);
    if (sourceUri == null || originUri == null) {
      return absoluteUrl;
    }

    final host = sourceUri.host.toLowerCase();
    if (host != 'localhost' && host != '127.0.0.1') {
      return absoluteUrl;
    }

    return sourceUri.replace(
      scheme: originUri.scheme,
      host: originUri.host,
      port: originUri.hasPort ? originUri.port : null,
    ).toString();
  }
}
