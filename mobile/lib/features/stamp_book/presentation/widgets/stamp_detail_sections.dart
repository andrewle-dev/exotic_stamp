import 'dart:ui' show ImageFilter;

import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/utils/media_url_resolver.dart';
import '../../domain/entities/stamp_detail.dart';

class StampDetailHeroCard extends StatelessWidget {
  const StampDetailHeroCard({
    super.key,
    required this.detail,
    this.stampImageUrl,
  });

  final StampDetail detail;
  final String? stampImageUrl;

  @override
  Widget build(BuildContext context) {
    final collected = detail.collected;

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(28),
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: collected
              ? const [AppColors.blueTint, AppColors.blueSurface]
              : const [AppColors.lockedSurface, AppColors.surface],
        ),
        border: Border.all(
          color: collected
              ? AppColors.primaryBlue.withValues(alpha: 0.35)
              : AppColors.border,
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: AppColors.primaryBlue.withValues(alpha: collected ? 0.12 : 0.04),
            blurRadius: 24,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(26.5),
        child: AspectRatio(
          aspectRatio: 1,
          child: Stack(
            fit: StackFit.expand,
            children: [
              Padding(
                padding: const EdgeInsets.all(AppSpacing.xl),
                child: stampImageUrl != null
                    ? _StampArtwork(
                        imageUrl: stampImageUrl!,
                        collected: collected,
                      )
                    : Icon(
                        Icons.collections_bookmark_outlined,
                        size: 72,
                        color: collected
                            ? AppColors.primaryBlue
                            : AppColors.textSecondary,
                      ),
              ),
              if (!collected) const _LockedOverlay(),
              Positioned(
                top: AppSpacing.lg,
                left: AppSpacing.lg,
                child: _StatusChip(collected: collected),
              ),
              if (collected)
                const Positioned(
                  top: AppSpacing.lg,
                  right: AppSpacing.lg,
                  child: _CollectedBadge(),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StampArtwork extends StatelessWidget {
  const _StampArtwork({
    required this.imageUrl,
    required this.collected,
  });

  final String imageUrl;
  final bool collected;

  @override
  Widget build(BuildContext context) {
    final image = CachedNetworkImage(
      imageUrl: imageUrl,
      fit: BoxFit.contain,
      placeholder: (_, __) => const Center(
        child: CircularProgressIndicator(
          strokeWidth: 2,
          color: AppColors.primaryBlue,
        ),
      ),
      errorWidget: (_, __, ___) => const Icon(
        Icons.broken_image_outlined,
        size: 48,
        color: AppColors.textSecondary,
      ),
    );

    if (collected) {
      return image;
    }

    return ImageFiltered(
      imageFilter: ImageFilter.blur(sigmaX: 7, sigmaY: 7),
      child: Opacity(opacity: 0.4, child: image),
    );
  }
}

class _LockedOverlay extends StatelessWidget {
  const _LockedOverlay();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.xl,
          vertical: AppSpacing.lg,
        ),
        decoration: BoxDecoration(
          color: AppColors.backgroundWhite.withValues(alpha: 0.92),
          borderRadius: BorderRadius.circular(20),
          boxShadow: const [
            BoxShadow(
              color: AppColors.shadow,
              blurRadius: 16,
              offset: Offset(0, 6),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 52,
              height: 52,
              decoration: const BoxDecoration(
                color: AppColors.redTint,
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.lock_rounded,
                size: 26,
                color: AppColors.accentRed,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            Text(
              'Chưa mở khóa',
              style: AppTextStyles.labelLarge.copyWith(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w800,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({required this.collected});

  final bool collected;

  @override
  Widget build(BuildContext context) {
    final bg = collected ? AppColors.blueTint : AppColors.redTint;
    final fg = collected ? AppColors.primaryBlue : AppColors.accentRed;
    final label = collected ? 'Đã thu' : 'Chưa thu';

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.sm,
      ),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: fg.withValues(alpha: 0.25)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            collected ? Icons.check_circle_rounded : Icons.lock_rounded,
            size: 14,
            color: fg,
          ),
          const SizedBox(width: 4),
          Text(
            label,
            style: AppTextStyles.caption.copyWith(
              color: fg,
              fontWeight: FontWeight.w800,
            ),
          ),
        ],
      ),
    );
  }
}

class _CollectedBadge extends StatelessWidget {
  const _CollectedBadge();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 36,
      height: 36,
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.primaryBlue, width: 1.5),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 8,
            offset: Offset(0, 2),
          ),
        ],
      ),
      child: const Icon(
        Icons.check_rounded,
        size: 20,
        color: AppColors.primaryBlue,
      ),
    );
  }
}

class StampDetailHeader extends StatelessWidget {
  const StampDetailHeader({super.key, required this.detail});

  final StampDetail detail;

  @override
  Widget build(BuildContext context) {
    final title = stampDetailTitle(detail);
    final rarity = formatStampRarity(detail.rarity);
    final chips = <String>[
      if (detail.lineName != null && detail.lineName!.trim().isNotEmpty)
        detail.lineName!.trim(),
      if (rarity.isNotEmpty) rarity,
      if (detail.stationName.trim().isNotEmpty &&
          !_titleAlreadyIncludesStation(title, detail.stationName))
        detail.stationName.trim(),
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: AppTextStyles.displayMedium.copyWith(
            color: AppColors.primaryBlue,
            fontWeight: FontWeight.w800,
            height: 1.2,
          ),
        ),
        if (chips.isNotEmpty) ...[
          const SizedBox(height: AppSpacing.md),
          Wrap(
            spacing: AppSpacing.sm,
            runSpacing: AppSpacing.sm,
            children: [
              for (final chip in chips) _MetaChip(label: chip),
            ],
          ),
        ],
        if (detail.serialNumber != null) ...[
          const SizedBox(height: AppSpacing.md),
          Text(
            detail.serialNumber!,
            style: AppTextStyles.caption.copyWith(
              color: AppColors.textSecondary,
              letterSpacing: 0.6,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ],
    );
  }
}

class _MetaChip extends StatelessWidget {
  const _MetaChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: 6,
      ),
      decoration: BoxDecoration(
        color: AppColors.blueSurface,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(
          color: AppColors.primaryBlue.withValues(alpha: 0.12),
        ),
      ),
      child: Text(
        label,
        style: AppTextStyles.caption.copyWith(
          color: AppColors.primaryBlue,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class StampUnlockHintCard extends StatelessWidget {
  const StampUnlockHintCard({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: AppColors.primaryBlue.withValues(alpha: 0.18),
        ),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: const BoxDecoration(
              color: AppColors.backgroundWhite,
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.nfc_rounded,
              color: AppColors.primaryBlue,
              size: 22,
            ),
          ),
          const SizedBox(width: AppSpacing.lg),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Cách mở khóa',
                  style: AppTextStyles.labelLarge.copyWith(
                    color: AppColors.primaryBlue,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'Ghé nhà ga và chạm thẻ NFC để thu stamp này vào sổ.',
                  style: AppTextStyles.bodyMedium.copyWith(
                    color: AppColors.textPrimary,
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class StampCollectedMetaCard extends StatelessWidget {
  const StampCollectedMetaCard({super.key, required this.detail});

  final StampDetail detail;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.blueSurface,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: AppColors.primaryBlue.withValues(alpha: 0.12),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (detail.collectedAt != null)
            Text(
              'Thu lúc ${formatStampDetailDateTime(detail.collectedAt!)}',
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w600,
              ),
            ),
          if (detail.nfcVerified) ...[
            if (detail.collectedAt != null)
              const SizedBox(height: AppSpacing.md),
            const StampDetailNfcBadge(verified: true),
          ],
        ],
      ),
    );
  }
}

class StampDetailNfcBadge extends StatelessWidget {
  const StampDetailNfcBadge({super.key, required this.verified});

  final bool verified;

  @override
  Widget build(BuildContext context) {
    if (!verified) {
      return const SizedBox.shrink();
    }

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.lg,
        vertical: AppSpacing.md,
      ),
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: AppColors.primaryBlue.withValues(alpha: 0.35)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(
            Icons.nfc_rounded,
            size: 18,
            color: AppColors.primaryBlue,
          ),
          const SizedBox(width: AppSpacing.sm),
          Text(
            'Xác thực NFC',
            style: AppTextStyles.labelLarge.copyWith(
              color: AppColors.primaryBlue,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

class StampAboutSection extends StatelessWidget {
  const StampAboutSection({super.key, required this.description});

  final String description;

  @override
  Widget build(BuildContext context) {
    return _SectionCard(
      icon: Icons.auto_stories_rounded,
      title: 'Về stamp này',
      child: Text(
        description,
        style: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.textPrimary,
          height: 1.55,
        ),
      ),
    );
  }
}

class StampInfoSection extends StatelessWidget {
  const StampInfoSection({super.key, required this.detail});

  final StampDetail detail;

  @override
  Widget build(BuildContext context) {
    final rows = <({IconData icon, String label, String value})>[
      if (cleanCampaignName(detail.campaignName).isNotEmpty)
        (
          icon: Icons.flag_rounded,
          label: 'Chiến dịch',
          value: cleanCampaignName(detail.campaignName),
        ),
      if (detail.stationName.trim().isNotEmpty)
        (
          icon: Icons.location_on_rounded,
          label: 'Nhà ga',
          value: detail.stationName.trim(),
        ),
      if (detail.lineName != null && detail.lineName!.trim().isNotEmpty)
        (
          icon: Icons.route_rounded,
          label: 'Tuyến',
          value: detail.lineName!.trim(),
        ),
      if (formatStampRarity(detail.rarity).isNotEmpty)
        (
          icon: Icons.workspace_premium_rounded,
          label: 'Độ hiếm',
          value: formatStampRarity(detail.rarity),
        ),
    ];

    if (rows.isEmpty) {
      return const SizedBox.shrink();
    }

    return _SectionCard(
      icon: Icons.info_outline_rounded,
      title: 'Thông tin',
      child: Column(
        children: [
          for (var i = 0; i < rows.length; i++) ...[
            if (i > 0) ...[
              const SizedBox(height: AppSpacing.md),
              const Divider(height: 1, color: AppColors.border),
              const SizedBox(height: AppSpacing.md),
            ],
            _InfoRow(
              icon: rows[i].icon,
              label: rows[i].label,
              value: rows[i].value,
            ),
          ],
        ],
      ),
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.icon,
    required this.title,
    required this.child,
  });

  final IconData icon;
  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.border),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 12,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, size: 20, color: AppColors.primaryBlue),
              const SizedBox(width: AppSpacing.sm),
              Text(
                title,
                style: AppTextStyles.titleMedium.copyWith(
                  color: AppColors.primaryBlue,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpacing.lg),
          child,
        ],
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({
    required this.icon,
    required this.label,
    required this.value,
  });

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 18, color: AppColors.textSecondary),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.textSecondary,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                value,
                style: AppTextStyles.bodyMedium.copyWith(
                  color: AppColors.textPrimary,
                  fontWeight: FontWeight.w700,
                  height: 1.35,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class StampCollectionProgressCard extends StatelessWidget {
  const StampCollectionProgressCard({
    super.key,
    required this.progress,
  });

  final StampCollectionProgress progress;

  @override
  Widget build(BuildContext context) {
    final ratio = progress.total == 0
        ? 0.0
        : (progress.collected / progress.total).clamp(0.0, 1.0);

    return _SectionCard(
      icon: Icons.emoji_events_outlined,
      title: 'Tiến độ bộ sưu tập',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            progress.collectionName,
            style: AppTextStyles.bodyMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.lg),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: LinearProgressIndicator(
              value: ratio,
              minHeight: 10,
              backgroundColor: AppColors.border,
              color: AppColors.primaryBlue,
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            '${progress.collected}/${progress.total} stamps',
            style: AppTextStyles.bodyLarge.copyWith(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w700,
            ),
          ),
          if (progress.nextRewardHint != null) ...[
            const SizedBox(height: AppSpacing.md),
            Text(
              progress.nextRewardHint!,
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class StampDetailStorySection extends StatelessWidget {
  const StampDetailStorySection({
    super.key,
    required this.story,
  });

  final String story;

  @override
  Widget build(BuildContext context) {
    return _SectionCard(
      icon: Icons.history_edu_rounded,
      title: 'Câu chuyện ga',
      child: Text(
        story,
        style: AppTextStyles.bodyMedium.copyWith(
          color: AppColors.textPrimary,
          height: 1.55,
        ),
      ),
    );
  }
}

String? resolveStampDetailMedia(String? url) {
  return MediaUrlResolver().resolve(url);
}

String formatStampDetailDateTime(DateTime value) {
  final local = value.toLocal();
  final day = local.day.toString().padLeft(2, '0');
  final month = local.month.toString().padLeft(2, '0');
  final year = local.year.toString();
  final hour = local.hour.toString().padLeft(2, '0');
  final minute = local.minute.toString().padLeft(2, '0');
  return '$day/$month/$year • $hour:$minute';
}

String formatStampRarity(String? rarity) {
  if (rarity == null || rarity.isEmpty) {
    return '';
  }
  switch (rarity.toUpperCase()) {
    case 'COMMON':
      return 'Thường';
    case 'RARE':
      return 'Hiếm';
    case 'EPIC':
      return 'Sử thi';
    case 'LEGENDARY':
      return 'Huyền thoại';
    default:
      return rarity;
  }
}

/// Removes duplicated admin-style prefixes like "Campaign: Campaign: ...".
String cleanCampaignName(String? raw) {
  if (raw == null) {
    return '';
  }
  var value = raw.trim();
  // Strip repeated "Campaign:" / "Campaign" prefixes from bad data entry.
  while (true) {
    final next = value.replaceFirst(
      RegExp(r'^Campaign\s*:?\s*', caseSensitive: false),
      '',
    );
    if (next == value) {
      break;
    }
    value = next.trim();
  }
  return value;
}

String stampDetailTitle(StampDetail detail) {
  final designName = detail.stampDesignName?.trim() ?? '';
  if (designName.isEmpty) {
    return detail.stationName.trim().isEmpty
        ? 'Stamp'
        : detail.stationName.trim();
  }

  // Prefer design name, but drop redundant "Stamp — " when it only wraps station.
  final station = detail.stationName.trim();
  if (station.isNotEmpty) {
    final stripped = designName.replaceFirst(
      RegExp(r'^Stamp\s*[—\-–:]\s*', caseSensitive: false),
      '',
    );
    if (stripped.trim().toLowerCase() == station.toLowerCase()) {
      return station;
    }
  }
  return designName;
}

bool _titleAlreadyIncludesStation(String title, String station) {
  final t = title.toLowerCase();
  final s = station.trim().toLowerCase();
  if (s.isEmpty) {
    return true;
  }
  return t == s || t.contains(s);
}
