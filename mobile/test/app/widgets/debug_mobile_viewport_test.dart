import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/app/widgets/debug_mobile_viewport.dart';

void main() {
  group('DebugMobileViewport', () {
    testWidgets('passes child through when disabled', (tester) async {
      await tester.pumpWidget(
        const DebugMobileViewport(
          enabledOverride: false,
          child: SizedBox(key: Key('inner')),
        ),
      );

      expect(find.byKey(const Key('inner')), findsOneWidget);
      expect(find.byType(ColoredBox), findsNothing);
    });

    testWidgets('wraps child in frame when enabled', (tester) async {
      await tester.pumpWidget(
        const DebugMobileViewport(
          enabledOverride: true,
          child: SizedBox(key: Key('inner')),
        ),
      );

      expect(find.byKey(const Key('inner')), findsOneWidget);
      expect(find.byType(ColoredBox), findsOneWidget);
    });

    test('documents viewport dimensions', () {
      expect(DebugMobileViewport.maxWidth, 430);
      expect(DebugMobileViewport.preferredWidth, 390);
    });
  });
}
