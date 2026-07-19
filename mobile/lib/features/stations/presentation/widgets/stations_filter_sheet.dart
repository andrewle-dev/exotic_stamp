import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/line.dart';
import '../cubit/stations_state.dart';
import '../utils/stations_line_filter.dart';

Future<void> showStationsFilterSheet({
  required BuildContext context,
  required StationsState state,
  required void Function({
    required StationsSortMode sortMode,
    required String selectedLineId,
    required StationsCollectionFilter collectionFilter,
    required StationsAvailabilityFilter availabilityFilter,
  }) onApply,
  required VoidCallback onReset,
}) {
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    backgroundColor: AppColors.backgroundWhite,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (sheetContext) {
      return _StationsFilterSheet(
        initialState: state,
        onApply: onApply,
        onReset: onReset,
      );
    },
  );
}

class _StationsFilterSheet extends StatefulWidget {
  const _StationsFilterSheet({
    required this.initialState,
    required this.onApply,
    required this.onReset,
  });

  final StationsState initialState;
  final void Function({
    required StationsSortMode sortMode,
    required String selectedLineId,
    required StationsCollectionFilter collectionFilter,
    required StationsAvailabilityFilter availabilityFilter,
  }) onApply;
  final VoidCallback onReset;

  @override
  State<_StationsFilterSheet> createState() => _StationsFilterSheetState();
}

class _StationsFilterSheetState extends State<_StationsFilterSheet> {
  late StationsSortMode _sortMode;
  late String _selectedLineId;
  late StationsCollectionFilter _collectionFilter;
  late StationsAvailabilityFilter _availabilityFilter;

  @override
  void initState() {
    super.initState();
    final state = widget.initialState;
    final hasGps = state.hasGpsCoordinates;
    _sortMode = state.sortMode == StationsSortMode.distance && !hasGps
        ? StationsSortMode.lineOrder
        : state.sortMode;
    _selectedLineId = state.selectedLineId ?? StationsLineFilter.allLines;
    _collectionFilter = state.collectionFilter;
    _availabilityFilter = state.availabilityFilter;
  }

  bool get _hasGps => widget.initialState.hasGpsCoordinates;

  List<Line> get _lines => widget.initialState.lines;

  @override
  Widget build(BuildContext context) {
    final bottomInset = MediaQuery.paddingOf(context).bottom;

    return SafeArea(
      child: Padding(
        padding: EdgeInsets.fromLTRB(
          AppSpacing.xl,
          AppSpacing.lg,
          AppSpacing.xl,
          AppSpacing.xl + bottomInset,
        ),
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            mainAxisSize: MainAxisSize.min,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(999),
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              Text(
                'Lọc & sắp xếp',
                style: AppTextStyles.titleMedium.copyWith(
                  fontWeight: FontWeight.w800,
                ),
              ),
              const SizedBox(height: AppSpacing.xl),
              const _SectionTitle('Sắp xếp theo'),
              const SizedBox(height: AppSpacing.md),
              _ChoiceChipWrap(
                children: [
                  _FilterChoice(
                    label: 'Khoảng cách',
                    selected: _sortMode == StationsSortMode.distance,
                    enabled: _hasGps,
                    onTap: _hasGps
                        ? () => setState(
                              () => _sortMode = StationsSortMode.distance,
                            )
                        : null,
                  ),
                  _FilterChoice(
                    label: 'Thứ tự tuyến',
                    selected: _sortMode == StationsSortMode.lineOrder,
                    onTap: () => setState(
                      () => _sortMode = StationsSortMode.lineOrder,
                    ),
                  ),
                  if (widget.initialState.hasCollectionFilterData)
                    _FilterChoice(
                      label: 'Trạng thái thu thập',
                      selected:
                          _sortMode == StationsSortMode.collectedStatus,
                      onTap: () => setState(
                        () => _sortMode = StationsSortMode.collectedStatus,
                      ),
                    ),
                  _FilterChoice(
                    label: 'Tên A-Z',
                    selected: _sortMode == StationsSortMode.name,
                    onTap: () =>
                        setState(() => _sortMode = StationsSortMode.name),
                  ),
                ],
              ),
              if (!_hasGps) ...[
                const SizedBox(height: AppSpacing.sm),
                Text(
                  'Bật GPS để sắp xếp theo khoảng cách.',
                  style: AppTextStyles.caption.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
              const SizedBox(height: AppSpacing.xxl),
              const _SectionTitle('Tuyến'),
              const SizedBox(height: AppSpacing.md),
              _ChoiceChipWrap(
                children: [
                  _FilterChoice(
                    label: 'Tất cả tuyến',
                    selected: _selectedLineId == StationsLineFilter.allLines,
                    onTap: () => setState(
                      () => _selectedLineId = StationsLineFilter.allLines,
                    ),
                  ),
                  for (final line in _lines)
                    _FilterChoice(
                      label: line.label,
                      selected: _selectedLineId == line.id,
                      onTap: () => setState(() => _selectedLineId = line.id),
                    ),
                ],
              ),
              if (widget.initialState.hasCollectionFilterData) ...[
                const SizedBox(height: AppSpacing.xxl),
                const _SectionTitle('Trạng thái thu thập'),
                const SizedBox(height: AppSpacing.md),
                _ChoiceChipWrap(
                  children: [
                    _FilterChoice(
                      label: 'Tất cả',
                      selected:
                          _collectionFilter == StationsCollectionFilter.all,
                      onTap: () => setState(
                        () => _collectionFilter =
                            StationsCollectionFilter.all,
                      ),
                    ),
                    _FilterChoice(
                      label: 'Đã thu',
                      selected: _collectionFilter ==
                          StationsCollectionFilter.collected,
                      onTap: () => setState(
                        () => _collectionFilter =
                            StationsCollectionFilter.collected,
                      ),
                    ),
                    _FilterChoice(
                      label: 'Chưa thu',
                      selected: _collectionFilter ==
                          StationsCollectionFilter.notCollected,
                      onTap: () => setState(
                        () => _collectionFilter =
                            StationsCollectionFilter.notCollected,
                      ),
                    ),
                  ],
                ),
              ],
              if (widget.initialState.hasAvailabilityFilterData) ...[
                const SizedBox(height: AppSpacing.xxl),
                const _SectionTitle('Trạng thái ga'),
                const SizedBox(height: AppSpacing.md),
                _ChoiceChipWrap(
                  children: [
                    _FilterChoice(
                      label: 'Tất cả',
                      selected: _availabilityFilter ==
                          StationsAvailabilityFilter.all,
                      onTap: () => setState(
                        () => _availabilityFilter =
                            StationsAvailabilityFilter.all,
                      ),
                    ),
                    _FilterChoice(
                      label: 'Chỉ đang hoạt động',
                      selected: _availabilityFilter ==
                          StationsAvailabilityFilter.activeOnly,
                      onTap: () => setState(
                        () => _availabilityFilter =
                            StationsAvailabilityFilter.activeOnly,
                      ),
                    ),
                  ],
                ),
              ],
              const SizedBox(height: AppSpacing.xxl),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () {
                        widget.onReset();
                        Navigator.of(context).pop();
                      },
                      child: const Text('Đặt lại'),
                    ),
                  ),
                  const SizedBox(width: AppSpacing.md),
                  Expanded(
                    child: FilledButton(
                      onPressed: () {
                        widget.onApply(
                          sortMode: _sortMode,
                          selectedLineId: _selectedLineId,
                          collectionFilter: _collectionFilter,
                          availabilityFilter: _availabilityFilter,
                        );
                        Navigator.of(context).pop();
                      },
                      style: FilledButton.styleFrom(
                        backgroundColor: AppColors.primaryBlue,
                      ),
                      child: const Text('Áp dụng'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.label);

  final String label;

  @override
  Widget build(BuildContext context) {
    return Text(
      label,
      style: AppTextStyles.labelLarge.copyWith(
        color: AppColors.textPrimary,
        fontWeight: FontWeight.w700,
      ),
    );
  }
}

class _ChoiceChipWrap extends StatelessWidget {
  const _ChoiceChipWrap({required this.children});

  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: AppSpacing.sm,
      runSpacing: AppSpacing.sm,
      children: children,
    );
  }
}

class _FilterChoice extends StatelessWidget {
  const _FilterChoice({
    required this.label,
    required this.selected,
    this.enabled = true,
    this.onTap,
  });

  final String label;
  final bool selected;
  final bool enabled;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final effectiveSelected = selected && enabled;
    return FilterChip(
      label: Text(label),
      selected: effectiveSelected,
      onSelected: enabled && onTap != null ? (_) => onTap!() : null,
      selectedColor: AppColors.primaryBlue.withValues(alpha: 0.15),
      checkmarkColor: AppColors.primaryBlue,
      labelStyle: AppTextStyles.labelMedium.copyWith(
        color: enabled
            ? (effectiveSelected
                ? AppColors.primaryBlue
                : AppColors.textPrimary)
            : AppColors.textSecondary,
        fontWeight: FontWeight.w600,
      ),
      side: BorderSide(
        color: effectiveSelected ? AppColors.primaryBlue : AppColors.border,
      ),
      backgroundColor: AppColors.surface,
      showCheckmark: true,
    );
  }
}
