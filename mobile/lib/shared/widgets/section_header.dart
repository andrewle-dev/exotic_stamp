import 'package:flutter/material.dart';

import '../../app/theme/app_text_styles.dart';

/// Section header with optional trailing text action.
class SectionHeader extends StatelessWidget {
  const SectionHeader({
    super.key,
    required this.title,
    this.trailingLabel,
    this.onTrailingTap,
    this.leading,
  });

  final String title;
  final String? trailingLabel;
  final VoidCallback? onTrailingTap;
  final Widget? leading;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (leading != null) ...[
          leading!,
          const SizedBox(width: 8),
        ],
        Expanded(
          child: Text(title, style: AppTextStyles.sectionTitle),
        ),
        if (trailingLabel != null && onTrailingTap != null)
          TextButton(
            onPressed: onTrailingTap,
            child: Text(
              trailingLabel!,
              style: AppTextStyles.linkLabel,
            ),
          ),
      ],
    );
  }
}

/// Legacy alias — prefer [SectionHeader].
@Deprecated('Use SectionHeader')
class SectionTitle extends StatelessWidget {
  const SectionTitle({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SectionHeader(title: title);
  }
}
