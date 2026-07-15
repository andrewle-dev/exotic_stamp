import 'package:flutter/material.dart';

import 'app_action_buttons.dart';

enum AppButtonVariant { primary, accent, outlined }

/// Legacy button wrapper — prefer [PrimaryButton], [SecondaryButton],
/// [DangerActionButton].
class AppButton extends StatelessWidget {
  const AppButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.variant = AppButtonVariant.primary,
    this.isLoading = false,
    this.expand = true,
    this.icon,
  });

  final String label;
  final VoidCallback? onPressed;
  final AppButtonVariant variant;
  final bool isLoading;
  final bool expand;
  final Widget? icon;

  @override
  Widget build(BuildContext context) {
    return switch (variant) {
      AppButtonVariant.primary => PrimaryButton(
          label: label,
          onPressed: onPressed,
          isLoading: isLoading,
          expand: expand,
          icon: icon,
        ),
      AppButtonVariant.accent => DangerActionButton(
          label: label,
          onPressed: onPressed,
          isLoading: isLoading,
          expand: expand,
          icon: icon,
        ),
      AppButtonVariant.outlined => SecondaryButton(
          label: label,
          onPressed: onPressed,
          expand: expand,
          icon: icon,
        ),
    };
  }
}
