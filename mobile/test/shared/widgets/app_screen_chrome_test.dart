import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/app/theme/app_icons.dart';
import 'package:metro_stamp_app/shared/widgets/app_action_button.dart';
import 'package:metro_stamp_app/shared/widgets/app_back_button.dart';
import 'package:metro_stamp_app/shared/widgets/app_logo.dart';
import 'package:metro_stamp_app/shared/widgets/app_screen_header.dart';
import 'package:metro_stamp_app/shared/widgets/app_version_footer.dart';

void main() {
  testWidgets('top-level header shows transparent logo + title + action',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AppScreenHeader(
            title: 'Metro Stamp',
            actionIcon: Icons.settings_outlined,
            onAction: () {},
          ),
        ),
      ),
    );

    expect(find.text('Metro Stamp'), findsOneWidget);
    expect(find.byType(AppLogo), findsOneWidget);
    expect(find.byIcon(Icons.settings_outlined), findsOneWidget);
    // Logo is Image.asset, not a blue Material/Container plate.
    expect(find.byType(Image), findsOneWidget);
  });

  testWidgets('Stamp Book header also shows logo', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AppScreenHeader(
            title: 'Stamp Book',
            actionIcon: Icons.search_rounded,
            onAction: () {},
          ),
        ),
      ),
    );

    expect(find.text('Stamp Book'), findsOneWidget);
    expect(find.byType(AppLogo), findsOneWidget);
    expect(find.byType(AppActionButton), findsOneWidget);
  });

  testWidgets('secondary header shows back + title without logo', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AppScreenHeader.secondary(
            title: 'Rewards',
            actionIcon: AppIcons.reward,
            onAction: () {},
          ),
        ),
      ),
    );

    expect(find.text('Rewards'), findsOneWidget);
    expect(find.byType(AppBackButton), findsOneWidget);
    expect(find.byIcon(AppIcons.back), findsOneWidget);
    expect(find.byType(AppLogo), findsNothing);
    expect(find.byType(AppActionButton), findsOneWidget);
  });

  testWidgets('AppBackButton falls back to /home when cannot pop',
      (tester) async {
    final router = GoRouter(
      initialLocation: RouteNames.rewards,
      routes: [
        GoRoute(
          path: RouteNames.home,
          builder: (context, state) => const Scaffold(
            body: Text('Home Screen'),
          ),
        ),
        GoRoute(
          path: RouteNames.rewards,
          builder: (context, state) => const Scaffold(
            body: AppBackButton(),
          ),
        ),
      ],
    );

    await tester.pumpWidget(MaterialApp.router(routerConfig: router));
    await tester.pumpAndSettle();

    expect(find.byType(AppBackButton), findsOneWidget);
    await tester.tap(find.byType(AppBackButton));
    await tester.pumpAndSettle();

    expect(find.text('Home Screen'), findsOneWidget);
  });

  testWidgets('AppBackButton is icon-only without circular Material plate',
      (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: AppBackButton(),
        ),
      ),
    );

    expect(find.byType(AppBackButton), findsOneWidget);
    expect(find.byType(IconButton), findsOneWidget);
    expect(find.byIcon(AppIcons.back), findsOneWidget);

    final materials = tester.widgetList<Material>(find.byType(Material));
    for (final material in materials) {
      expect(material.shape, isNot(isA<CircleBorder>()));
    }
  });

  testWidgets('version footer is full-width centered', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              AppVersionFooter(versionOverride: '0.1.0'),
            ],
          ),
        ),
      ),
    );
    await tester.pump();

    expect(find.text('METRO STAMP v0.1.0'), findsOneWidget);
    final footerSize = tester.getSize(find.byType(AppVersionFooter));
    final columnSize = tester.getSize(find.byType(Column).first);
    expect(footerSize.width, columnSize.width);
  });

  test('AppAssets.logo points at local transparent PNG', () {
    expect(AppAssets.logo, 'assets/logo/ExoticStamp_logo.png');
  });
}
