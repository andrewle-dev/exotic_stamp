import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/location/app_location_service.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../stations/domain/entities/station.dart';
import '../../data/datasources/admin_scan_key_remote_datasource.dart';
import '../cubit/nfc_tag_writer_cubit.dart';
import '../cubit/nfc_tag_writer_state.dart';

class NfcTagWriterScreen extends StatelessWidget {
  const NfcTagWriterScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => NfcTagWriterCubit(
        stationsRepository: Injection.instance.stationsRepository,
        adminScanKeyRemoteDataSource: AdminScanKeyRemoteDataSource(
          apiClient: Injection.instance.apiClient,
        ),
        locationService: AppLocationService(),
      )..loadStations(),
      child: const _NfcTagWriterView(),
    );
  }
}

class _NfcTagWriterView extends StatefulWidget {
  const _NfcTagWriterView();

  @override
  State<_NfcTagWriterView> createState() => _NfcTagWriterViewState();
}

class _NfcTagWriterViewState extends State<_NfcTagWriterView> {
  final _labelController = TextEditingController();
  final _placementController = TextEditingController();

  @override
  void dispose() {
    _labelController.dispose();
    _placementController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        title: const Text('NFC Tag Writer'),
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
      ),
      body: BlocBuilder<NfcTagWriterCubit, NfcTagWriterState>(
        builder: (context, state) {
          if (state.phase == NfcTagWriterPhase.loadingStations) {
            return const AppLoadingView(message: 'Loading stations…');
          }

          return ListView(
            padding: const EdgeInsets.all(AppSpacing.xl),
            children: [
              Text(
                'Admin utility — write production scan keys to physical NFC tags.',
                style: AppTextStyles.bodyMedium.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
              const SizedBox(height: AppSpacing.xl),
              const Text('Station', style: AppTextStyles.titleMedium),
              const SizedBox(height: AppSpacing.md),
              InputDecorator(
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                ),
                child: DropdownButtonHideUnderline(
                  child: DropdownButton<Station>(
                    value: state.selectedStation,
                    isExpanded: true,
                    hint: const Text('Select station'),
                    items: state.stations
                        .map(
                          (station) => DropdownMenuItem(
                            value: station,
                            child: Text('${station.name} (${station.code})'),
                          ),
                        )
                        .toList(),
                    onChanged: (station) {
                      if (station != null) {
                        context.read<NfcTagWriterCubit>().selectStation(station);
                      }
                    },
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              TextField(
                controller: _labelController,
                decoration: const InputDecoration(
                  labelText: 'Label (optional)',
                  border: OutlineInputBorder(),
                ),
                maxLength: 100,
              ),
              TextField(
                controller: _placementController,
                decoration: const InputDecoration(
                  labelText: 'Placement note (optional)',
                  border: OutlineInputBorder(),
                ),
                maxLength: 255,
              ),
              const SizedBox(height: AppSpacing.lg),
              FilledButton(
                onPressed: state.selectedStation == null ||
                        state.phase == NfcTagWriterPhase.creatingKey
                    ? null
                    : () => context.read<NfcTagWriterCubit>().createKey(
                          label: _labelController.text.trim(),
                          placementNote: _placementController.text.trim(),
                        ),
                child: Text(
                  state.phase == NfcTagWriterPhase.creatingKey
                      ? 'Creating…'
                      : 'Create NFC key',
                ),
              ),
              if (state.createdKey != null) ...[
                const SizedBox(height: AppSpacing.xl),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(AppSpacing.lg),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('Payload to write', style: AppTextStyles.titleMedium),
                      const SizedBox(height: AppSpacing.sm),
                      SelectableText(
                        state.createdKey!.payloadToWrite,
                        style: AppTextStyles.bodyMedium.copyWith(
                          fontFamily: 'monospace',
                        ),
                      ),
                      const SizedBox(height: AppSpacing.sm),
                      Text(
                        'Prefix: ${state.createdKey!.keyPrefix}…',
                        style: AppTextStyles.bodyMedium.copyWith(
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),
                FilledButton.icon(
                  onPressed: state.phase == NfcTagWriterPhase.writingTag ||
                          state.phase == NfcTagWriterPhase.verifyingInstall
                      ? null
                      : () => context
                          .read<NfcTagWriterCubit>()
                          .writeTagAndVerifyInstallation(),
                  icon: const Icon(Icons.nfc),
                  label: Text(
                    state.phase == NfcTagWriterPhase.writingTag
                        ? 'Writing tag…'
                        : state.phase == NfcTagWriterPhase.verifyingInstall
                            ? 'Verifying…'
                            : 'Write NFC Tag',
                  ),
                ),
              ],
              if (state.statusMessage != null) ...[
                const SizedBox(height: AppSpacing.lg),
                Text(
                  state.statusMessage!,
                  style: AppTextStyles.bodyMedium.copyWith(
                    color: state.phase == NfcTagWriterPhase.success
                        ? Colors.green.shade700
                        : state.phase == NfcTagWriterPhase.error
                            ? AppColors.accentRed
                            : AppColors.textPrimary,
                  ),
                ),
              ],
              if (state.phase == NfcTagWriterPhase.success) ...[
                const SizedBox(height: AppSpacing.lg),
                const Text(
                  'Installation verified. Check lastInstallVerifiedAt in the web admin dashboard.',
                ),
              ],
            ],
          );
        },
      ),
    );
  }
}
