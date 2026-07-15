import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/config/mock_config.dart';

void main() {
  test('useMockData defaults to false', () {
    expect(MockConfig.useMockData, isFalse);
  });

  test('isMockMode is false when flag is off', () {
    expect(MockConfig.isMockMode, isFalse);
  });

  test('allowMockWrites follows isMockMode', () {
    expect(MockConfig.allowMockWrites, MockConfig.isMockMode);
  });
}
