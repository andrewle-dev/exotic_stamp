import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../domain/entities/profile.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/logout_profile_usecase.dart';
import '../cubit/profile_cubit.dart';
import '../cubit/profile_state.dart';
import '../widgets/profile_header.dart';
import '../widgets/profile_menu_section.dart';
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
                  message: 'Đang tải hồ sơ...',
                );
              case ProfileStatus.unauthorized:
                return AppErrorView(
                  message: 'Phiên đăng nhập đã hết hạn.',
                  failure: state.failure,
                  onRetry: () => context.go(RouteNames.login),
                  retryLabel: 'Đăng nhập',
                );
              case ProfileStatus.error:
                return AppErrorView(
                  message: state.failure?.message ?? 'Không thể tải hồ sơ.',
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
                      AppSpacing.lg,
                      AppSpacing.lg,
                      AppSpacing.lg,
                      AppSpacing.xxxl,
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        ProfileHeader(
                          profile: profile,
                          onSettingsTap: () =>
                              context.push(RouteNames.settings),
                        ),
                        const SizedBox(height: AppSpacing.xl),
                        if (profile.stats != null)
                          ProfileStatsRow(stats: profile.stats!)
                        else
                          const ProfileStatsRow(
                            stats: ProfileStats(),
                          ),
                        const SizedBox(height: AppSpacing.xl),
                        ProfileMenuSection(
                          onSettingsTap: () =>
                              context.push(RouteNames.settings),
                          onLogoutTap: () => _confirmLogout(context),
                        ),
                        const SizedBox(height: AppSpacing.xl),
                        Center(
                          child: Text(
                            'EXOTIC STAMP V0.1.0',
                            style: Theme.of(context)
                                .textTheme
                                .labelSmall
                                ?.copyWith(
                                  color: AppColors.textSecondary,
                                  letterSpacing: 1.2,
                                  fontWeight: FontWeight.w700,
                                ),
                          ),
                        ),
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

  Future<void> _confirmLogout(BuildContext context) async {
    final shouldLogout = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Đăng xuất'),
          content: const Text(
            'Bạn có chắc muốn đăng xuất khỏi tài khoản hiện tại không?',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(false),
              child: const Text('Hủy'),
            ),
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(true),
              child: const Text(
                'Đăng xuất',
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
