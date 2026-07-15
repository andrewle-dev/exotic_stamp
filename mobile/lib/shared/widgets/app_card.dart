import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';
import '../../app/theme/app_radius.dart';
import '../../app/theme/app_shadows.dart';
import '../../app/theme/app_spacing.dart';

/// Shared card container with border and optional shadow.
class AppCard extends StatelessWidget {
  const AppCard({
    required this.child,
    super.key,
    this.padding,
    this.margin,
    this.onTap,
    this.backgroundColor = AppColors.backgroundWhite,
    this.borderRadius,
    this.showShadow = false,
    this.borderColor = AppColors.border,
  });

  final Widget child;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;
  final VoidCallback? onTap;
  final Color backgroundColor;
  final BorderRadius? borderRadius;
  final bool showShadow;
  final Color borderColor;

  @override
  Widget build(BuildContext context) {
    final radius = borderRadius ?? AppRadius.xlAll;
    final decoration = BoxDecoration(
      color: backgroundColor,
      borderRadius: radius,
      border: Border.all(color: borderColor),
      boxShadow: showShadow ? AppShadows.softCard : null,
    );

    Widget card = Ink(
      decoration: decoration,
      child: Padding(
        padding: padding ?? const EdgeInsets.all(AppSpacing.xl),
        child: child,
      ),
    );

    if (onTap != null) {
      card = Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: radius,
          child: card,
        ),
      );
    }

    if (margin != null) {
      card = Padding(padding: margin!, child: card);
    }

    return card;
  }
}
