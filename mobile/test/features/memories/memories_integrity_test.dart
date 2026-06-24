import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/memories/data/datasources/memories_remote_datasource.dart';
import 'package:metro_stamp_app/features/memories/data/models/record_share_event_request_model.dart';
import 'package:metro_stamp_app/features/memories/data/models/share_event_model.dart';
import 'package:metro_stamp_app/features/memories/data/repositories/memories_repository_impl.dart';
import 'package:metro_stamp_app/features/memories/domain/repositories/memories_repository.dart';
import 'package:mocktail/mocktail.dart';

class MockMemoriesRemoteDataSource extends Mock
    implements MemoriesRemoteDataSource {}

void main() {
  late MockMemoriesRemoteDataSource remoteDataSource;
  late MemoriesRepositoryImpl repository;

  setUpAll(() {
    registerFallbackValue(
      const RecordShareEventRequestModel(
        platform: 'native',
        shareType: 'stamp_collected',
      ),
    );
  });

  setUp(() {
    remoteDataSource = MockMemoriesRemoteDataSource();
    repository = MemoriesRepositoryImpl(remoteDataSource: remoteDataSource);
  });

  test('recordShareEvent maps response to entity', () async {
    when(() => remoteDataSource.recordShareEvent(any())).thenAnswer(
      (_) async => ShareEventModel(
        id: 'share-1',
        platform: 'native',
        shareType: 'stamp_collected',
        targetId: 'stamp-uuid',
        sharedAt: DateTime.utc(2026, 6, 24, 10, 10),
      ),
    );

    final event = await repository.recordShareEvent(
      const RecordShareEventParams(
        platform: 'native',
        shareType: 'stamp_collected',
        targetId: 'stamp-uuid',
        metadata: {'stationName': 'Ben Thanh'},
      ),
    );

    expect(event.id, 'share-1');
    expect(event.platform, 'native');
    expect(event.shareType, 'stamp_collected');
    expect(event.targetId, 'stamp-uuid');
  });

  test('request model serializes share event without image payload', () {
    const request = RecordShareEventRequestModel(
      platform: 'native',
      shareType: 'stamp_collected',
      targetId: 'stamp-uuid',
      metadata: {'stationName': 'Ben Thanh'},
    );

    final json = request.toJson();

    expect(json['platform'], 'native');
    expect(json['shareType'], 'stamp_collected');
    expect(json['targetId'], 'stamp-uuid');
    expect(json['metadata'], {'stationName': 'Ben Thanh'});
    expect(json.containsKey('image'), isFalse);
    expect(json.containsKey('photo'), isFalse);
    expect(json.containsKey('file'), isFalse);
  });

  test('remote datasource only exposes share-events POST contract path', () {
    final source = File(
      'lib/features/memories/data/datasources/memories_remote_datasource.dart',
    ).readAsStringSync();
    expect(source.contains('/community/share-events'), isTrue);
    expect(source.contains('upload'), isFalse);
    expect(source.contains('getMemories'), isFalse);
  });

  test('repository has no gallery or list memories methods', () {
    final source = File(
      'lib/features/memories/domain/repositories/memories_repository.dart',
    ).readAsStringSync();
    expect(source.contains('getMemories'), isFalse);
    expect(source.contains('listMemories'), isFalse);
    expect(source.contains('upload'), isFalse);
  });
}
