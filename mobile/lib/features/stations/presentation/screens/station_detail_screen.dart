import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_empty_state.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/entities/station_collected_status.dart';
import '../../domain/entities/station_detail.dart';
import '../../domain/usecases/get_station_detail_usecase.dart';
import '../cubit/station_detail_cubit.dart';
import '../cubit/station_detail_state.dart';
import '../widgets/nearby_places_section.dart';
import '../widgets/station_action_row.dart';
import '../widgets/station_collect_cta.dart';
import '../widgets/station_detail_hero.dart';
import '../widgets/station_history_card.dart';
import '../widgets/station_social_proof_card.dart';

class StationDetailScreen extends StatelessWidget {
  const StationDetailScreen({
    super.key,
    required this.stationId,
    this.cubit,
  });

  final String stationId;
  final StationDetailCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<StationDetailCubit>.value(
        value: cubit!,
        child: const _StationDetailView(),
      );
    }

    return BlocProvider(
      create: (_) => StationDetailCubit(
        getStationDetailUseCase:
            GetStationDetailUseCase(Injection.instance.stationsRepository),
        stationId: stationId,
      )..load(),
      child: const _StationDetailView(),
    );
  }
}

class _StationDetailView extends StatelessWidget {
  const _StationDetailView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: BlocBuilder<StationDetailCubit, StationDetailState>(
        builder: (context, state) {
          switch (state.status) {
            case StationDetailStatus.initial:
            case StationDetailStatus.loading:
              return const AppLoadingView(message: 'Đang tải chi tiết ga...');
            case StationDetailStatus.notFound:
              return AppEmptyState(
                title: 'Không tìm thấy ga',
                message: 'Ga này không tồn tại hoặc đã bị gỡ.',
                icon: Icons.location_off_outlined,
                actionLabel: 'Quay lại',
                onAction: () => context.pop(),
              );
            case StationDetailStatus.failure:
              return AppErrorView(
                message: state.failure?.message ?? 'Không thể tải chi tiết ga.',
                failure: state.failure,
                onRetry: () => context.read<StationDetailCubit>().load(),
              );
            case StationDetailStatus.inactive:
            case StationDetailStatus.loaded:
              return _StationDetailContent(
                detail: state.detail!,
                inactive: state.status == StationDetailStatus.inactive,
              );
          }
        },
      ),
    );
  }
}

class _StationDetailContent extends StatelessWidget {
  const _StationDetailContent({
    required this.detail,
    required this.inactive,
  });

  final StationDetail detail;
  final bool inactive;

  /// Space the collectors card pulls up onto the hero (design strut only).
  static const _collectorsOverlap = 24.0;

  @override
  Widget build(BuildContext context) {
    final imageResolver = StationDetailHeroImageResolver();
    final imageUrl = imageResolver.resolve(
      detail.stampPreviewUrl,
      detail.imageUrl,
    );
    final lineBadge = _lineBadgeLabel(detail);
    final locationLabel = _shortLocationLabel(detail);
    final canCollect = detail.isActive &&
        !inactive &&
        detail.collectedStatus != StationCollectedStatus.collected;
    final hasSocialProof = detail.socialProof != null;

    return Column(
      children: [
        Expanded(
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                StationDetailHero(
                  imageUrl: imageUrl,
                  lineBadgeLabel: lineBadge,
                  stationName: detail.label,
                  locationLabel: locationLabel,
                  contentBottomInset: hasSocialProof
                      ? _collectorsOverlap + AppSpacing.xl
                      : AppSpacing.xxl,
                  onBack: () => context.pop(),
                  onShare: () => Share.share(
                    'Check out ${detail.label} on Metro Stamp!',
                  ),
                ),
                // Collectors only: float onto the hero edge. Action row never
                // overlaps the photo — it always sits on the white sheet.
                if (hasSocialProof)
                  Transform.translate(
                    offset: const Offset(0, -_collectorsOverlap),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppSpacing.xl,
                      ),
                      child: StationSocialProofCard(
                        socialProof: detail.socialProof!,
                      ),
                    ),
                  ),
                Padding(
                  padding: EdgeInsets.fromLTRB(
                    AppSpacing.xl,
                    hasSocialProof ? AppSpacing.lg : AppSpacing.xl,
                    AppSpacing.xl,
                    AppSpacing.xxl +
                        (hasSocialProof ? _collectorsOverlap : 0),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (inactive) ...[
                        _InactiveBanner(),
                        const SizedBox(height: AppSpacing.lg),
                      ],
                      StationActionRow(
                        onDirections: detail.latitude != null &&
                                detail.longitude != null
                            ? () => _openDirections(
                                  detail.latitude!,
                                  detail.longitude!,
                                )
                            : null,
                        onFavorite: () {},
                        onVirtualTour: detail.virtualTourUrl != null
                            ? () => _openUrl(detail.virtualTourUrl!)
                            : null,
                      ),
                      if (detail.description != null) ...[
                        const SizedBox(height: AppSpacing.xxl),
                        StationHistoryCard(
                          description: detail.description!,
                          openingHoursLabel: detail.openingHoursLabel,
                          accessibilityLabel: detail.accessibilityLabel,
                        ),
                      ],
                      if (detail.nearbyPlaces.isNotEmpty) ...[
                        const SizedBox(height: AppSpacing.xxl),
                        NearbyPlacesSection(places: detail.nearbyPlaces),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(
              AppSpacing.xl,
              AppSpacing.md,
              AppSpacing.xl,
              AppSpacing.xl,
            ),
            child: StationCollectCta(
              enabled: canCollect,
              onTap: () => context.go(RouteNames.scanTapToCollect),
            ),
          ),
        ),
      ],
    );
  }

  String _lineBadgeLabel(StationDetail detail) {
    final line = detail.lineName ?? 'Line 1';
    if (detail.lineHubLabel != null) {
      return '$line • ${detail.lineHubLabel}';
    }
    return line;
  }

  /// Design uses a short place line; avoid wrapping a full street address
  /// into the hero when [districtLabel] is missing.
  String? _shortLocationLabel(StationDetail detail) {
    final district = detail.districtLabel?.trim();
    if (district != null && district.isNotEmpty) return district;

    final address = detail.address?.trim();
    if (address == null || address.isEmpty) return null;

    // Prefer the district/city tail after the last comma when possible.
    final parts = address
        .split(',')
        .map((part) => part.trim())
        .where((part) => part.isNotEmpty)
        .toList();
    if (parts.length >= 2) {
      return parts.sublist(parts.length - 2).join(', ');
    }
    return address;
  }

  Future<void> _openDirections(double lat, double lng) async {
    final uri = Uri.parse(
      'https://www.google.com/maps/dir/?api=1&destination=$lat,$lng',
    );
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  Future<void> _openUrl(String url) async {
    final uri = Uri.tryParse(url);
    if (uri != null && await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }
}

class _InactiveBanner extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.redTint,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border),
      ),
      child: Text(
        'Ga không hoạt động — không thể thu stamp tại thời điểm này.',
        style: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.accentRed,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
