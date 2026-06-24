import 'dart:typed_data';

import 'package:image_picker/image_picker.dart';

/// Abstraction over [ImagePicker] for tests and cubit wiring.
abstract class PhotoPickerService {
  Future<PickedPhoto?> pickFromGallery();
  Future<PickedPhoto?> pickFromCamera();
}

class PickedPhoto {
  const PickedPhoto({
    required this.bytes,
    required this.path,
  });

  final Uint8List bytes;
  final String path;
}

class ImagePickerPhotoPickerService implements PhotoPickerService {
  ImagePickerPhotoPickerService({ImagePicker? picker})
      : _picker = picker ?? ImagePicker();

  final ImagePicker _picker;

  @override
  Future<PickedPhoto?> pickFromGallery() => _pick(ImageSource.gallery);

  @override
  Future<PickedPhoto?> pickFromCamera() => _pick(ImageSource.camera);

  Future<PickedPhoto?> _pick(ImageSource source) async {
    final file = await _picker.pickImage(
      source: source,
      imageQuality: 85,
      maxWidth: 2048,
    );
    if (file == null) {
      return null;
    }
    final bytes = await file.readAsBytes();
    return PickedPhoto(bytes: bytes, path: file.path);
  }
}
