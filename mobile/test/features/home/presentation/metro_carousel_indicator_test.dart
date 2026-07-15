import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/home/presentation/widgets/metro_carousel_indicator.dart';

void main() {
  testWidgets('renders one convoy slot per slotCount with subway on active',
      (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: MetroCarouselIndicator(slotCount: 3, activeSlot: 0),
        ),
      ),
    );

    expect(find.byType(MetroCarouselIndicator), findsOneWidget);
    expect(find.byKey(const ValueKey('subway-0')), findsOneWidget);
    expect(find.byKey(const ValueKey('carriage-1')), findsOneWidget);
    expect(find.byKey(const ValueKey('carriage-2')), findsOneWidget);
    expect(find.byType(Image), findsOneWidget);
  });

  testWidgets('moves subway asset to active slot in the convoy', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: MetroCarouselIndicator(slotCount: 3, activeSlot: 1),
        ),
      ),
    );

    expect(find.byKey(const ValueKey('subway-1')), findsOneWidget);
    expect(find.byKey(const ValueKey('carriage-0')), findsOneWidget);
    expect(find.byKey(const ValueKey('carriage-2')), findsOneWidget);
  });

  testWidgets('hides when slotCount is zero', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: MetroCarouselIndicator(slotCount: 0, activeSlot: 0),
        ),
      ),
    );

    expect(find.byType(Image), findsNothing);
    expect(find.byType(SizedBox), findsOneWidget);
  });

  test('slotCountFor caps at four for ten banners', () {
    expect(MetroCarouselIndicator.slotCountFor(10), 4);
    expect(MetroCarouselIndicator.slotCountFor(4), 4);
    expect(MetroCarouselIndicator.slotCountFor(3), 3);
    expect(MetroCarouselIndicator.slotCountFor(2), 2);
    expect(MetroCarouselIndicator.slotCountFor(1), 0);
    expect(MetroCarouselIndicator.slotCountFor(0), 0);
  });

  test('slotForPage wraps through four slots', () {
    expect(MetroCarouselIndicator.slotForPage(0, 10), 0);
    expect(MetroCarouselIndicator.slotForPage(3, 10), 3);
    expect(MetroCarouselIndicator.slotForPage(4, 10), 0);
    expect(MetroCarouselIndicator.slotForPage(5, 10), 1);
    expect(MetroCarouselIndicator.slotForPage(9, 10), 1);
  });
}
