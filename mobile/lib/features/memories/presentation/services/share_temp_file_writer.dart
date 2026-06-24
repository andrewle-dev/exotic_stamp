import 'dart:io';
import 'dart:typed_data';

abstract class ShareTempFileWriter {
  Future<File> writePng(Uint8List bytes);
}

class PathProviderShareTempFileWriter implements ShareTempFileWriter {
  @override
  Future<File> writePng(Uint8List bytes) async {
    final dir = Directory.systemTemp;
    final file = File(
      '${dir.path}/exotic_stamp_share_${DateTime.now().millisecondsSinceEpoch}.png',
    );
    await file.writeAsBytes(bytes, flush: true);
    return file;
  }
}
