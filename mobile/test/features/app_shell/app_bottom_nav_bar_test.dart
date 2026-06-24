import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/app/router/route_names.dart';
import 'package:metro_stamp_app/features/app_shell/presentation/widgets/app_bottom_nav_bar.dart';

void main() {
  testWidgets('renders all shell tab labels', (WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          bottomNavigationBar: AppBottomNavBar(
            currentIndex: ShellTabIndex.home,
            onTabSelected: (_) {},
          ),
        ),
      ),
    );

    expect(find.text('Home'), findsOneWidget);
    expect(find.text('Book'), findsOneWidget);
    expect(find.text('Stations'), findsOneWidget);
    expect(find.text('Rewards'), findsOneWidget);
    expect(find.text('Profile'), findsOneWidget);
  });

  testWidgets('tapping each tab invokes onTabSelected with correct index',
      (WidgetTester tester) async {
    final selected = <int>[];

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          bottomNavigationBar: AppBottomNavBar(
            currentIndex: ShellTabIndex.home,
            onTabSelected: selected.add,
          ),
        ),
      ),
    );

    for (final entry in [
      ('Home', ShellTabIndex.home),
      ('Book', ShellTabIndex.book),
      ('Stations', ShellTabIndex.stations),
      ('Rewards', ShellTabIndex.rewards),
      ('Profile', ShellTabIndex.profile),
    ]) {
      await tester.tap(find.text(entry.$1));
      await tester.pump();
      expect(selected.last, entry.$2);
    }
  });
}
