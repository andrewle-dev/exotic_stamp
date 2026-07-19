import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/line.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station.dart';
import 'package:metro_stamp_app/features/stations/domain/entities/station_collected_status.dart';
import 'package:metro_stamp_app/features/stations/presentation/utils/stations_line_filter.dart';
import 'package:metro_stamp_app/features/stations/presentation/utils/stations_list_presenter.dart';

void main() {
  const lines = [
    Line(id: 'line-2', name: 'Line 2', sortOrder: 2),
    Line(id: 'line-1', name: 'Line 1', sortOrder: 1),
  ];

  const stations = [
    Station(
      id: 'far',
      lineId: 'line-2',
      code: 'F01',
      name: 'Far Station',
      latitude: 10.9,
      longitude: 106.9,
      sortOrder: 2,
      collectedStatus: StationCollectedStatus.collected,
      status: 'ACTIVE',
    ),
    Station(
      id: 'near',
      lineId: 'line-1',
      code: 'N01',
      name: 'Near Station',
      latitude: 10.771,
      longitude: 106.701,
      sortOrder: 1,
      collectedStatus: StationCollectedStatus.uncollected,
      status: 'ACTIVE',
    ),
    Station(
      id: 'mid',
      lineId: 'line-1',
      code: 'M01',
      name: 'Alpha Mid',
      latitude: 10.78,
      longitude: 106.71,
      sortOrder: 3,
      collectedStatus: StationCollectedStatus.uncollected,
      status: 'INACTIVE',
    ),
    Station(
      id: 'nocoords',
      lineId: 'line-1',
      code: 'X01',
      name: 'No Coords',
      sortOrder: 0,
      collectedStatus: StationCollectedStatus.collected,
      status: 'ACTIVE',
    ),
  ];

  test('sort by distance puts missing coords last', () {
    final sorted = StationsListPresenter.sortStations(
      stations: stations,
      sortMode: StationsSortMode.distance,
      lines: lines,
      userLatitude: 10.77,
      userLongitude: 106.7,
      hasGps: true,
    );

    expect(sorted.first.id, 'near');
    expect(sorted.last.id, 'nocoords');
  });

  test('sort by distance without GPS falls back to line order', () {
    final sorted = StationsListPresenter.sortStations(
      stations: stations,
      sortMode: StationsSortMode.distance,
      lines: lines,
      userLatitude: null,
      userLongitude: null,
      hasGps: false,
    );

    expect(sorted.map((s) => s.id).toList(), [
      'nocoords',
      'near',
      'mid',
      'far',
    ]);
  });

  test('sort by name is A-Z', () {
    final sorted = StationsListPresenter.sortStations(
      stations: stations,
      sortMode: StationsSortMode.name,
      lines: lines,
      userLatitude: null,
      userLongitude: null,
      hasGps: false,
    );

    expect(sorted.map((s) => s.name).toList(), [
      'Alpha Mid',
      'Far Station',
      'Near Station',
      'No Coords',
    ]);
  });

  test('sort by collected status puts uncollected first', () {
    final sorted = StationsListPresenter.sortStations(
      stations: stations,
      sortMode: StationsSortMode.collectedStatus,
      lines: lines,
      userLatitude: null,
      userLongitude: null,
      hasGps: false,
    );

    expect(sorted.take(2).every((s) => !s.isCollected), isTrue);
    expect(sorted.skip(2).every((s) => s.isCollected), isTrue);
  });

  test('collection filter keeps only uncollected', () {
    final filtered = StationsListPresenter.applyClientFilters(
      stations: stations,
      collectionFilter: StationsCollectionFilter.notCollected,
      availabilityFilter: StationsAvailabilityFilter.all,
    );

    expect(filtered.map((s) => s.id), ['near', 'mid']);
  });

  test('active-only filter excludes inactive stations', () {
    final filtered = StationsListPresenter.applyClientFilters(
      stations: stations,
      collectionFilter: StationsCollectionFilter.all,
      availabilityFilter: StationsAvailabilityFilter.activeOnly,
    );

    expect(filtered.any((s) => s.id == 'mid'), isFalse);
    expect(filtered.length, 3);
  });
}
