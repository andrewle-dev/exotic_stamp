import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

/// Canonical icon mapping for chrome and navigation.
///
/// Prefer these over ad-hoc icon picks in screen headers / bottom nav.
abstract final class AppIcons {
  static const IconData home = Icons.home_outlined;
  static const IconData homeActive = Icons.home_rounded;

  /// Rubber-stamp glyph (Font Awesome) for the Stamp book tab.
  static const IconData stamp = FontAwesomeIcons.stamp;
  static const IconData stampActive = FontAwesomeIcons.stamp;

  static const IconData scan = Icons.center_focus_weak_rounded;

  static const IconData stations = Icons.format_list_bulleted;
  static const IconData stationsActive = Icons.format_list_bulleted_rounded;

  static const IconData profile = Icons.person_outline_rounded;
  static const IconData profileActive = Icons.person_rounded;

  static const IconData search = Icons.search_rounded;
  static const IconData settings = Icons.settings_outlined;
  static const IconData filter = Icons.tune_rounded;
  static const IconData lock = Icons.lock_rounded;
  static const IconData collected = Icons.check_rounded;
  static const IconData reward = Icons.card_giftcard_outlined;
}
