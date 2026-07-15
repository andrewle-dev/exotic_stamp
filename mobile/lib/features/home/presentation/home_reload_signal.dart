import 'package:flutter/foundation.dart';

/// Notifies Home to soft-refresh when the user returns to the Home tab
/// after visiting another shell branch (e.g. after collecting a stamp).
class HomeReloadSignal extends ChangeNotifier {
  int _generation = 0;

  int get generation => _generation;

  void requestReload() {
    _generation++;
    notifyListeners();
  }
}
