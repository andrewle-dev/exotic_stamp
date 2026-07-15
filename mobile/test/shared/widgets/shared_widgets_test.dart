import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stamp_book/domain/entities/stamp_item.dart';
import 'package:metro_stamp_app/shared/widgets/app_action_buttons.dart';
import 'package:metro_stamp_app/shared/widgets/progress_card.dart';
import 'package:metro_stamp_app/shared/widgets/section_header.dart';
import 'package:metro_stamp_app/shared/widgets/stamp_tile.dart';

void main() {
  testWidgets('ProgressCard shows collected fraction', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: ProgressCard(
            collectedCount: 5,
            totalCount: 14,
            nextRewardText: 'Còn 2 stamp nữa',
          ),
        ),
      ),
    );

    expect(find.text('5 / 14 Stamps'), findsOneWidget);
    expect(find.text('36%'), findsOneWidget);
    expect(find.text('Còn 2 stamp nữa'), findsOneWidget);
  });

  testWidgets('SectionHeader renders trailing action', (tester) async {
    var tapped = false;
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: SectionHeader(
            title: 'Recently Collected',
            trailingLabel: 'View Book',
            onTrailingTap: () => tapped = true,
          ),
        ),
      ),
    );

    expect(find.text('Recently Collected'), findsOneWidget);
    await tester.tap(find.text('View Book'));
    expect(tapped, isTrue);
  });

  testWidgets('StampTile reflects collected flag from item', (tester) async {
    const collected = StampItem(
      stationId: 's1',
      stationName: 'Ga Bến Thành',
      sequence: 1,
      collected: true,
    );
    const locked = StampItem(
      stationId: 's2',
      stationName: 'Ga Thảo Điền',
      sequence: 2,
      collected: false,
    );

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: SizedBox(
            width: 120,
            height: 160,
            child: Row(
              children: [
                Expanded(child: StampTile(item: collected, onTap: () {})),
                Expanded(child: StampTile(item: locked, onTap: () {})),
              ],
            ),
          ),
        ),
      ),
    );

    expect(find.byIcon(Icons.check_rounded), findsOneWidget);
    expect(find.byIcon(Icons.lock_rounded), findsWidgets);
  });

  testWidgets('PrimaryButton and DangerActionButton render labels',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: Column(
            children: [
              PrimaryButton(label: 'Primary', onPressed: () {}),
              DangerActionButton(label: 'Collect', onPressed: () {}),
            ],
          ),
        ),
      ),
    );

    expect(find.text('Primary'), findsOneWidget);
    expect(find.text('Collect'), findsOneWidget);
  });
}
