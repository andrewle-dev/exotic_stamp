import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/home/domain/entities/home_summary.dart';
import 'package:metro_stamp_app/features/home/presentation/widgets/home_banner_carousel.dart';
import 'package:metro_stamp_app/features/home/presentation/widgets/metro_carousel_indicator.dart';

List<PartnerBanner> _banners(int count) {
  return List.generate(
    count,
    (i) => PartnerBanner(
      partnerId: 'p$i',
      partnerName: 'Partner $i',
      bannerImageUrl: 'https://cdn.example/$i.png',
    ),
  );
}

Finder _slot(int index) => find.byKey(ValueKey('slot-$index'));

void main() {
  testWidgets('empty banners render fallback without indicator', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: HomeBannerCarousel(banners: []),
        ),
      ),
    );

    expect(find.text('Metro Hanoi'), findsOneWidget);
    expect(find.byType(MetroCarouselIndicator), findsNothing);
    expect(find.byType(PageView), findsNothing);
  });

  testWidgets('one banner disables autoplay and hides indicator', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: HomeBannerCarousel(
            autoPlayInterval: const Duration(milliseconds: 50),
            banners: _banners(1),
          ),
        ),
      ),
    );

    expect(find.text('Partner 0'), findsOneWidget);
    expect(find.byType(MetroCarouselIndicator), findsNothing);

    await tester.pump(const Duration(milliseconds: 80));
    expect(find.text('Partner 0'), findsOneWidget);
  });

  testWidgets('ten banners produce ten pages and four indicator slots',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: HomeBannerCarousel(
            autoPlay: false,
            banners: _banners(10),
          ),
        ),
      ),
    );

    final controller =
        tester.widget<PageView>(find.byType(PageView)).controller!;
    expect(find.text('Partner 0'), findsOneWidget);
    controller.jumpToPage(9);
    await tester.pumpAndSettle();
    expect(find.text('Partner 9'), findsOneWidget);

    expect(find.byType(MetroCarouselIndicator), findsOneWidget);
    expect(_slot(0), findsOneWidget);
    expect(_slot(1), findsOneWidget);
    expect(_slot(2), findsOneWidget);
    expect(_slot(3), findsOneWidget);
    expect(_slot(4), findsNothing);
  });

  testWidgets('page indices map to indicator slots with modulo 4', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: HomeBannerCarousel(
            autoPlay: false,
            banners: _banners(10),
          ),
        ),
      ),
    );

    final controller =
        tester.widget<PageView>(find.byType(PageView)).controller!;

    Future<void> expectActiveSlot(int page, int slot) async {
      controller.jumpToPage(page);
      await tester.pumpAndSettle();
      expect(find.byKey(ValueKey('subway-$slot')), findsOneWidget);
    }

    await expectActiveSlot(0, 0);
    await expectActiveSlot(3, 3);
    await expectActiveSlot(4, 0);
    await expectActiveSlot(9, 1);
  });

  testWidgets('manual swipe advances page among all banners', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: HomeBannerCarousel(
            autoPlay: false,
            banners: _banners(5),
          ),
        ),
      ),
    );

    expect(find.text('Partner 0'), findsOneWidget);
    await tester.fling(find.byType(PageView), const Offset(-400, 0), 1000);
    await tester.pumpAndSettle();
    expect(find.text('Partner 1'), findsOneWidget);
  });

  testWidgets('autoplay advances through banners beyond index 3', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: HomeBannerCarousel(
            autoPlayInterval: const Duration(milliseconds: 100),
            pageAnimationDuration: const Duration(milliseconds: 40),
            banners: _banners(6),
          ),
        ),
      ),
    );

    final controller =
        tester.widget<PageView>(find.byType(PageView)).controller!;
    controller.jumpToPage(3);
    await tester.pump();
    expect(find.text('Partner 3'), findsOneWidget);
    expect(find.byKey(const ValueKey('subway-3')), findsOneWidget);

    // One autoplay tick: page 3 → 4, indicator wraps to slot 0.
    // Do not use pumpAndSettle — Timer.periodic never settles.
    await tester.pump(const Duration(milliseconds: 100));
    await tester.pump(const Duration(milliseconds: 50));
    // Let AnimatedSwitcher finish so only one locomotive remains.
    await tester.pump(const Duration(milliseconds: 250));

    expect(find.text('Partner 4'), findsOneWidget);
    expect(find.byKey(const ValueKey('subway-3')), findsNothing);
    expect(find.byKey(const ValueKey('subway-0')), findsOneWidget);
  });

  testWidgets('two banners show two indicator slots not four', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: HomeBannerCarousel(
            autoPlay: false,
            banners: _banners(2),
          ),
        ),
      ),
    );

    expect(_slot(0), findsOneWidget);
    expect(_slot(1), findsOneWidget);
    expect(_slot(2), findsNothing);
  });
}
