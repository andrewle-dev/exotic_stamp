import 'package:flutter/material.dart';

import '../core/di/injection.dart';
import 'app.dart';

Future<void> bootstrap() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Injection.instance.init();
  runApp(const MetroStampApp());
}
