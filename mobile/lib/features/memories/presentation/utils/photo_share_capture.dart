import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

/// Captures a widget subtree backed by [previewKey] as PNG bytes.
Future<Uint8List?> captureWidgetPng(GlobalKey previewKey) async {
  final boundary =
      previewKey.currentContext?.findRenderObject() as RenderRepaintBoundary?;
  if (boundary == null) {
    return null;
  }
  final image = await boundary.toImage(pixelRatio: 3);
  final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
  return byteData?.buffer.asUint8List();
}

String formatShareDate(DateTime? value) {
  if (value == null) {
    return '';
  }
  final local = value.toLocal();
  final day = local.day.toString().padLeft(2, '0');
  final month = local.month.toString().padLeft(2, '0');
  final year = local.year.toString();
  final hour = local.hour.toString().padLeft(2, '0');
  final minute = local.minute.toString().padLeft(2, '0');
  return '$day/$month/$year • $hour:$minute';
}

ImageProvider<Object>? photoImageProvider(String? path) {
  if (path == null || path.isEmpty) {
    return null;
  }
  return FileImage(File(path));
}
