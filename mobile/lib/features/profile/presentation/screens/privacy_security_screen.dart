import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_radius.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/logout_all_profile_usecase.dart';
import '../../domain/usecases/logout_profile_usecase.dart';
import '../cubit/privacy_security_cubit.dart';
import '../cubit/privacy_security_state.dart';

class PrivacySecurityScreen extends StatefulWidget {
  const PrivacySecurityScreen({super.key, this.cubit});

  final PrivacySecurityCubit? cubit;

  @override
  State<PrivacySecurityScreen> createState() => _PrivacySecurityScreenState();
}

class _PrivacySecurityScreenState extends State<PrivacySecurityScreen> {
  PrivacySecurityCubit? _ownedCubit;

  @override
  void initState() {
    super.initState();
    if (widget.cubit == null) {
      _ownedCubit = PrivacySecurityCubit(
        getProfileUseCase: GetProfileUseCase(
          Injection.instance.profileRepository,
        ),
        logoutProfileUseCase: LogoutProfileUseCase(
          Injection.instance.profileRepository,
        ),
        logoutAllProfileUseCase: LogoutAllProfileUseCase(
          Injection.instance.profileRepository,
        ),
      )..load();
    }
  }

  @override
  void dispose() {
    _ownedCubit?.close();
    super.dispose();
  }

  PrivacySecurityCubit get _cubit => widget.cubit ?? _ownedCubit!;

  Future<void> _completeLogout(BuildContext context) async {
    Injection.instance.authCubit.markUnauthenticated();
    Injection.instance.notifySessionChanged();
    if (context.mounted) {
      context.go(RouteNames.login);
    }
  }

  Future<void> _logoutCurrentDevice(BuildContext context) async {
    final shouldLogout = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Log Out'),
          content: const Text(
            'Log out of this device? You can sign in again anytime.',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(false),
              child: const Text('Cancel'),
            ),
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(true),
              child: const Text(
                'Log Out',
                style: TextStyle(color: AppColors.accentRed),
              ),
            ),
          ],
        );
      },
    );

    if (shouldLogout != true || !context.mounted) {
      return;
    }

    await _cubit.logout();
    if (context.mounted) {
      await _completeLogout(context);
    }
  }

  Future<void> _logoutAllDevices(BuildContext context) async {
    final shouldLogout = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Log Out All Devices'),
          content: const Text(
            'This will end every active session for your account, '
            'including this device. Continue?',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(false),
              child: const Text('Cancel'),
            ),
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(true),
              child: const Text(
                'Log Out All',
                style: TextStyle(color: AppColors.accentRed),
              ),
            ),
          ],
        );
      },
    );

    if (shouldLogout != true || !context.mounted) {
      return;
    }

    await _cubit.logoutAll();
    if (context.mounted) {
      await _completeLogout(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider.value(
      value: _cubit,
      child: BlocConsumer<PrivacySecurityCubit, PrivacySecurityState>(
        listener: (context, state) {
          if (state.status == PrivacySecurityStatus.unauthorized) {
            Injection.instance.authCubit.markUnauthenticated();
            Injection.instance.notifySessionChanged();
            context.go(RouteNames.login);
          }
        },
        builder: (context, state) {
          return Scaffold(
            backgroundColor: AppColors.backgroundWhite,
            appBar: const AppSecondaryAppBar(
              title: 'Privacy & Security',
              fallbackRoute: RouteNames.profile,
            ),
            body: _buildBody(context, state),
          );
        },
      ),
    );
  }

  Widget _buildBody(BuildContext context, PrivacySecurityState state) {
    if (state.status == PrivacySecurityStatus.initial ||
        state.status == PrivacySecurityStatus.loading) {
      return const AppLoadingView(message: 'Loading security settings...');
    }

    if (state.status == PrivacySecurityStatus.error && state.profile == null) {
      return AppErrorView(
        message: state.failure?.message ?? 'Unable to load security settings.',
        failure: state.failure,
        onRetry: () => _cubit.load(),
      );
    }

    final email = state.profile?.email ?? '';
    final isLoggingOut = state.status == PrivacySecurityStatus.loggingOut;

    return SafeArea(
      top: false,
      child: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.xl,
          AppSpacing.xxl,
          AppSpacing.xl,
          AppSpacing.huge,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'ACCOUNT SECURITY',
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w800,
                letterSpacing: 0.6,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            _InfoTile(
              icon: Icons.email_outlined,
              title: 'Email',
              subtitle: email.isEmpty ? 'Not available' : email,
            ),
            const SizedBox(height: AppSpacing.sm),
            _InfoTile(
              icon: Icons.lock_outline_rounded,
              title: 'Change Password',
              subtitle: 'Update your account password',
              onTap: () => context.push(RouteNames.changePassword),
            ),
            const SizedBox(height: AppSpacing.xxl),
            Text(
              'SESSIONS',
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w800,
                letterSpacing: 0.6,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            AppButton(
              label: 'Log out this device',
              variant: AppButtonVariant.outlined,
              isLoading: isLoggingOut,
              onPressed:
                  isLoggingOut ? null : () => _logoutCurrentDevice(context),
              icon: const Icon(
                Icons.logout_rounded,
                color: AppColors.accentRed,
                size: 20,
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            AppButton(
              label: 'Log out all devices',
              variant: AppButtonVariant.outlined,
              isLoading: isLoggingOut,
              onPressed:
                  isLoggingOut ? null : () => _logoutAllDevices(context),
              icon: const Icon(
                Icons.devices_other_outlined,
                color: AppColors.accentRed,
                size: 20,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            Text(
              'Logging out all devices ends every active session for your account.',
              style: AppTextStyles.caption.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _InfoTile extends StatelessWidget {
  const _InfoTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    const titleColor = AppColors.textPrimary;
    const iconColor = AppColors.primaryBlue;

    final content = Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.lg),
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: AppRadius.xlAll,
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: AppRadius.lgAll,
            ),
            child: Icon(icon, color: iconColor, size: 22),
          ),
          const SizedBox(width: AppSpacing.lg),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: AppTextStyles.titleMedium.copyWith(color: titleColor),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  subtitle,
                  style: AppTextStyles.bodyMedium.copyWith(
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          if (onTap != null)
            const Icon(
              Icons.chevron_right_rounded,
              color: AppColors.textSecondary,
            ),
        ],
      ),
    );

    if (onTap == null) {
      return content;
    }

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: AppRadius.xlAll,
        child: content,
      ),
    );
  }
}
