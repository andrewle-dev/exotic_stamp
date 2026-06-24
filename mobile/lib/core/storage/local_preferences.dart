import 'package:shared_preferences/shared_preferences.dart';

/// Lightweight local preferences for non-sensitive app state.
class LocalPreferences {
  LocalPreferences({SharedPreferences? preferences})
      : _preferences = preferences;

  static const _onboardingCompletedKey = 'onboarding_completed';

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

  Future<void> clear() async {
    await _prefs.remove(_onboardingCompletedKey);
  }
}
