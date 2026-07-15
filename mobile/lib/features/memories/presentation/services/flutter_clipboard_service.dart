import 'package:flutter/services.dart';

import 'clipboard_service.dart';

class FlutterClipboardService implements ClipboardService {
  @override
  Future<void> copyText(String text) {
    return Clipboard.setData(ClipboardData(text: text));
  }
}
