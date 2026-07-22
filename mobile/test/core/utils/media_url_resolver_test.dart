import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/utils/media_url_resolver.dart';

void main() {
  group('MediaUrlResolver', () {
    final resolver = MediaUrlResolver(mediaOrigin: 'http://localhost:8080');
    final emulatorResolver =
        MediaUrlResolver(mediaOrigin: 'http://10.0.2.2:8080');

    test('returns null for empty path', () {
      expect(resolver.resolve(null), isNull);
      expect(resolver.resolve(''), isNull);
    });

    test('returns absolute URLs unchanged', () {
      const url = 'https://cdn.example.com/stamps/a.png';
      expect(resolver.resolve(url), url);
    });

    test('rewrites localhost absolute URLs to current media origin host', () {
      expect(
        emulatorResolver.resolve(
          'http://localhost:8080/uploads/public/stamps/ben-thanh.png',
        ),
        'http://10.0.2.2:8080/uploads/public/stamps/ben-thanh.png',
      );
    });

    test('prefixes relative upload paths', () {
      expect(
        resolver.resolve('/uploads/public/stamps/ben-thanh.png'),
        'http://localhost:8080/uploads/public/stamps/ben-thanh.png',
      );
    });

    test('normalizes paths without leading slash', () {
      expect(
        resolver.resolve('uploads/public/stamps/ben-thanh.png'),
        'http://localhost:8080/uploads/public/stamps/ben-thanh.png',
      );
    });
  });
}
