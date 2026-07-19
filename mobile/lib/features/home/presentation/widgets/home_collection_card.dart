import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_shadow.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/home_summary.dart';

class HomeCollectionCard extends StatelessWidget {
  const HomeCollectionCard({
    super.key,
    required this.progress,
    this.rankTitle,
    this.rankSubtitle,
  });

  final CollectionProgress progress;
  final String? rankTitle;
  final String? rankSubtitle;

  @override
  Widget build(BuildContext context) {
    final fraction =
        progress.total > 0 ? progress.collected / progress.total : 0.0;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            AppColors.blueTint,
            AppColors.blueSurface,
            AppColors.backgroundWhite,
          ],
          stops: [0.0, 0.55, 1.0],
        ),
        borderRadius: AppRadius.xxlAll,
        border: Border.all(color: AppColors.border),
        boxShadow: AppShadow.softCard,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${progress.collected}/${progress.total} STAMPS',
                      style: AppTextStyles.displayMedium.copyWith(
                        color: AppColors.primaryBlue,
                        fontSize: 26,
                        letterSpacing: 0.2,
                      ),
                    ),
                    const SizedBox(height: AppSpacing.md),
                    SizedBox(
                      width: 72,
                      height: 36,
                      child: CustomPaint(
                        painter: _ArcProgressPainter(value: fraction),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: AppColors.backgroundWhite.withValues(alpha: 0.9),
                  shape: BoxShape.circle,
                  border: Border.all(color: AppColors.border),
                ),
                child: const Icon(
                  Icons.emoji_events_outlined,
                  color: AppColors.primaryBlue,
                  size: 22,
                ),
              ),
            ],
          ),
          if (rankTitle != null) ...[
            const SizedBox(height: AppSpacing.lg),
            Text(
              rankTitle!,
              style: AppTextStyles.titleMedium.copyWith(
                fontWeight: FontWeight.w800,
              ),
            ),
          ],
          if (rankSubtitle != null) ...[
            const SizedBox(height: AppSpacing.xs),
            Text(
              rankSubtitle!,
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

class _ArcProgressPainter extends CustomPainter {
  _ArcProgressPainter({required this.value});

  final double value;

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height);
    final radius = size.width / 2;
    const strokeWidth = 5.0;

    final backgroundPaint = Paint()
      ..color = AppColors.border
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round;

    final progressPaint = Paint()
      ..color = AppColors.primaryBlue
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round;

    const startAngle = 3.14159;
    const sweepAngle = 3.14159;

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      sweepAngle,
      false,
      backgroundPaint,
    );

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      sweepAngle * value.clamp(0.0, 1.0),
      false,
      progressPaint,
    );
  }

  @override
  bool shouldRepaint(covariant _ArcProgressPainter oldDelegate) {
    return oldDelegate.value != value;
  }
}

class HomeCollectionPlaceholder extends StatelessWidget {
  const HomeCollectionPlaceholder({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: AppShadow.cardDecoration(
        borderRadius: AppRadius.xlAll,
        color: AppColors.surface,
      ),
      child: const Text(
        'Tiến độ sưu tập sẽ hiển thị khi có dữ liệu tuyến metro.',
        style: AppTextStyles.bodyMedium,
        textAlign: TextAlign.center,
      ),
    );
  }
}
