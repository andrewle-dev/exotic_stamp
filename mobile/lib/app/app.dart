import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../core/di/injection.dart';
import '../features/auth/presentation/cubit/auth_cubit.dart';
import 'theme/app_theme.dart';

class MetroStampApp extends StatelessWidget {
  const MetroStampApp({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider<AuthCubit>.value(
      value: Injection.instance.authCubit,
      child: MaterialApp.router(
        title: 'Metro Stamp',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.light(),
        routerConfig: Injection.instance.router,
      ),
    );
  }
}
