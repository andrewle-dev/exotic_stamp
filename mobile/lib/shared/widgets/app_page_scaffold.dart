import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';
import '../../app/theme/app_dimensions.dart';
import '../../app/theme/app_spacing.dart';
import '../../app/theme/app_typography.dart';

/// Mobile-safe page shell with consistent background and padding.
class AppPageScaffold extends StatelessWidget {
  const AppPageScaffold({
    required this.body,
    super.key,
    this.title,
    this.header,
    this.actions,
    this.showBackButton = false,
    this.onBack,
    this.bottomNavigationBar,
    this.floatingActionButton,
    this.floatingActionButtonLocation,
    this.padding = const EdgeInsets.symmetric(
      horizontal: AppDimensions.screenHorizontalPadding,
    ),
    this.includeSafeArea = true,
    this.scrollable = false,
  });

  final Widget body;
  final String? title;
  final Widget? header;
  final List<Widget>? actions;
  final bool showBackButton;
  final VoidCallback? onBack;
  final Widget? bottomNavigationBar;
  final Widget? floatingActionButton;
  final FloatingActionButtonLocation? floatingActionButtonLocation;
  final EdgeInsets padding;
  final bool includeSafeArea;
  final bool scrollable;

  /// Extra bottom inset when nested inside the app shell (FAB + bottom nav).
  /// Prefer [AppDimensions.bottomNavBottomPadding].
  static const double shellBottomInset = AppDimensions.bottomNavBottomPadding;

  @override
  Widget build(BuildContext context) {
    Widget content;

    if (scrollable) {
      content = SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: padding,
        child: body,
      );
    } else if (header != null) {
      content = Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: EdgeInsets.fromLTRB(
              padding.left,
              AppSpacing.md,
              padding.right,
              AppSpacing.md,
            ),
            child: header,
          ),
          Expanded(
            child: Padding(padding: padding, child: body),
          ),
        ],
      );
    } else {
      content = Padding(padding: padding, child: body);
    }

    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: title != null
          ? AppBar(
              backgroundColor: AppColors.backgroundWhite,
              foregroundColor: AppColors.textPrimary,
              surfaceTintColor: AppColors.backgroundWhite,
              elevation: 0,
              title: Text(title!, style: AppTextStyles.appTitle),
              leading: showBackButton
                  ? IconButton(
                      icon: const Icon(Icons.arrow_back_ios_new_rounded),
                      onPressed: onBack ?? () => Navigator.maybePop(context),
                    )
                  : null,
              actions: actions,
            )
          : null,
      body: includeSafeArea ? SafeArea(child: content) : content,
      bottomNavigationBar: bottomNavigationBar,
      floatingActionButton: floatingActionButton,
      floatingActionButtonLocation: floatingActionButtonLocation,
    );
  }
}
