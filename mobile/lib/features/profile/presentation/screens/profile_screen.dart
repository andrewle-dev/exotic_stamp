import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../core/auth/role_gates.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_page_scaffold.dart';
import '../../../../shared/widgets/app_version_footer.dart';
import '../../domain/entities/profile.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/logout_profile_usecase.dart';
import '../cubit/profile_cubit.dart';
import '../cubit/profile_state.dart';
import '../widgets/profile_header.dart';
import '../widgets/profile_menu_section.dart';
import '../widgets/profile_sections.dart';
import '../widgets/profile_stats_row.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key, this.cubit});

  /// Optional override for widget tests.
  final ProfileCubit? cubit;

  @override
  Widget build(BuildContext context) {
    if (cubit != null) {
      return BlocProvider<ProfileCubit>.value(
        value: cubit!,
        child: const _ProfileView(),
      );
    }

    return BlocProvider(
      create: (_) => ProfileCubit(
        getProfileUseCase: GetProfileUseCase(
          Injection.instance.profileRepository,
        ),
      )..load(),
      child: const _ProfileView(),
    );
  }
}

class _ProfileView extends StatelessWidget {
  const _ProfileView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      body: SafeArea(
        child: BlocBuilder<ProfileCubit, ProfileState>(
          builder: (context, state) {
            switch (state.status) {
              case ProfileStatus.initial:
              case ProfileStatus.loading:
                return const AppLoadingView(
                  message: 'Loading profile...',
                );
              case ProfileStatus.unauthorized:
                return AppErrorView(
                  message: 'Your session has expired.',
                  failure: state.failure,
                  onRetry: () => context.go(RouteNames.login),
                  retryLabel: 'Sign in',
                );
              case ProfileStatus.error:
                return AppErrorView(
                  message: state.failure?.message ?? 'Unable to load profile.',
                  failure: state.failure,
                  onRetry: () => context.read<ProfileCubit>().load(),
                );
              case ProfileStatus.loaded:
                final profile = state.profile!;
                return RefreshIndicator(
                  color: AppColors.primaryBlue,
                  onRefresh: () => context.read<ProfileCubit>().load(),
                  child: SingleChildScrollView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.fromLTRB(
                      AppSpacing.xl,
                      AppSpacing.md,
                      AppSpacing.xl,
                      AppPageScaffold.shellBottomInset,
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        ProfileHeader(
                          profile: profile,
                          onSettingsTap: () => _openPersonalInformation(context),
                        ),
                        const SizedBox(height: AppSpacing.xxl),
                        ProfileStatsRow(
                          stats: profile.stats ?? const ProfileStats(),
                        ),
                        if (profile.invite != null) ...[
                          const SizedBox(height: AppSpacing.xxl),
                          ProfileInviteCard(invite: profile.invite!),
                        ],
                        if (profile.memories.isNotEmpty) ...[
                          const SizedBox(height: AppSpacing.xxl),
                          ProfileMemoriesCarousel(memories: profile.memories),
                        ],
                        if (profile.achievements.isNotEmpty) ...[
                          const SizedBox(height: AppSpacing.xxl),
                          ProfileAchievementsGrid(
                            achievements: profile.achievements,
                          ),
                        ],
                        const SizedBox(height: AppSpacing.xxl),
                        ProfileMenuSection(
                          onPersonalInformationTap: () =>
                              _openPersonalInformation(context),
                          onPrivacySecurityTap: () => context.push(
                            RouteNames.privacySecurity,
                          ),
                          onHelpCenterTap: () =>
                              context.push(RouteNames.helpCenter),
                          onApiDebugTap: kDebugMode &&
                                  Injection.instance.isInitialized
                              ? () => context.push(RouteNames.apiDebug)
                              : null,
                          showAdminTools: Injection.instance.isInitialized &&
                              RoleGates.isAdmin(
                                Injection.instance.authCubit.state.session
                                    ?.user,
                              ),
                          onAdminNfcWriterTap: () =>
                              context.push(RouteNames.adminNfcWriter),
                          onLogoutTap: () => _confirmLogout(context),
                        ),
                        const AppVersionFooter(),
                      ],
                    ),
                  ),
                );
            }
          },
        ),
      ),
    );
  }

  Future<void> _openPersonalInformation(BuildContext context) async {
    await context.push(RouteNames.personalInformation);
    if (context.mounted) {
      await context.read<ProfileCubit>().load();
    }
  }

  Future<void> _confirmLogout(BuildContext context) async {
    final shouldLogout = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Log Out'),
          content: const Text('Are you sure you want to log out?'),
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

    await LogoutProfileUseCase(Injection.instance.profileRepository).call();
    Injection.instance.authCubit.markUnauthenticated();
    Injection.instance.notifySessionChanged();

    if (context.mounted) {
      context.go(RouteNames.login);
    }
  }
}
