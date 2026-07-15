import 'package:shared_preferences/shared_preferences.dart';

/// Lightweight local preferences for non-sensitive app state.
class LocalPreferences {
  LocalPreferences({SharedPreferences? preferences})
      : _preferences = preferences;

  static const _onboardingCompletedKey = 'onboarding_completed';
  static const _apiHostOverrideKey = 'api_host_override';
  static const _apiPortOverrideKey = 'api_port_override';
  static const _optionalUpdateDismissedVersionKey =
      'optional_update_dismissed_version';

  SharedPreferences? _preferences;

  Future<void> init() async {
    _preferences ??= await SharedPreferences.getInstance();
  }

  SharedPreferences get _prefs {
    final prefs = _preferences;
    if (prefs == null) {
      throw StateError('LocalPreferences.init() must be called before use.');
    }
    return prefs;
  }

  bool get onboardingCompleted =>
      _preferences?.getBool(_onboardingCompletedKey) ?? false;

  Future<void> setOnboardingCompleted({required bool value}) async {
    await _prefs.setBool(_onboardingCompletedKey, value);
  }

  String? get apiHostOverride => _preferences?.getString(_apiHostOverrideKey);

  String? get apiPortOverride => _preferences?.getString(_apiPortOverrideKey);

  Future<void> setApiHostOverride(String? host) async {
    if (host == null || host.trim().isEmpty) {
      await _prefs.remove(_apiHostOverrideKey);
      return;
    }
    await _prefs.setString(_apiHostOverrideKey, host.trim());
  }

  Future<void> setApiPortOverride(String? port) async {
    if (port == null || port.trim().isEmpty) {
      await _prefs.remove(_apiPortOverrideKey);
      return;
    }
    await _prefs.setString(_apiPortOverrideKey, port.trim());
  }

  /// Latest version for which the optional update prompt was dismissed.
  String? get optionalUpdateDismissedVersion =>
      _preferences?.getString(_optionalUpdateDismissedVersionKey);

  Future<void> setOptionalUpdateDismissedVersion(String? version) async {
    if (version == null || version.trim().isEmpty) {
      await _prefs.remove(_optionalUpdateDismissedVersionKey);
      return;
    }
    await _prefs.setString(
      _optionalUpdateDismissedVersionKey,
      version.trim(),
    );
  }

  Future<void> clear() async {
    await _prefs.remove(_onboardingCompletedKey);
    await _prefs.remove(_apiHostOverrideKey);
    await _prefs.remove(_apiPortOverrideKey);
    await _prefs.remove(_optionalUpdateDismissedVersionKey);
  }
}
