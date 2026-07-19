import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_secondary_app_bar.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_network_image.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../domain/entities/profile.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/update_profile_usecase.dart';
import '../cubit/settings_cubit.dart';
import '../cubit/settings_state.dart';

class PersonalInformationScreen extends StatefulWidget {
  const PersonalInformationScreen({super.key, this.cubit});

  final SettingsCubit? cubit;

  @override
  State<PersonalInformationScreen> createState() =>
      _PersonalInformationScreenState();
}

class _PersonalInformationScreenState extends State<PersonalInformationScreen> {
  late final TextEditingController _firstnameController;
  late final TextEditingController _lastnameController;
  late final TextEditingController _emailController;
  late final TextEditingController _phoneController;
  late final TextEditingController _bioController;
  SettingsCubit? _ownedCubit;

  @override
  void initState() {
    super.initState();
    _firstnameController = TextEditingController();
    _lastnameController = TextEditingController();
    _emailController = TextEditingController();
    _phoneController = TextEditingController();
    _bioController = TextEditingController();

    if (widget.cubit == null) {
      _ownedCubit = SettingsCubit(
        getProfileUseCase: GetProfileUseCase(
          Injection.instance.profileRepository,
        ),
        updateProfileUseCase: UpdateProfileUseCase(
          Injection.instance.profileRepository,
        ),
      )..load();
    }
  }

  @override
  void dispose() {
    _firstnameController.dispose();
    _lastnameController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    _bioController.dispose();
    _ownedCubit?.close();
    super.dispose();
  }

  SettingsCubit get _cubit => widget.cubit ?? _ownedCubit!;

  void _syncControllers(SettingsState state) {
    final profile = state.profile;
    if (profile == null) {
      return;
    }
    _firstnameController.text = profile.firstname ?? '';
    _lastnameController.text = profile.lastname ?? '';
    _emailController.text = profile.email;
    _phoneController.text = profile.phoneNumber ?? '';
    _bioController.text = profile.bio ?? '';
  }

  void _handleUnauthorized(BuildContext context) {
    Injection.instance.authCubit.markUnauthenticated();
    Injection.instance.notifySessionChanged();
    context.go(RouteNames.login);
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider.value(
      value: _cubit,
      child: BlocConsumer<SettingsCubit, SettingsState>(
        listener: (context, state) {
          if (state.status == SettingsStatus.unauthorized) {
            _handleUnauthorized(context);
            return;
          }
          if (state.status == SettingsStatus.loaded ||
              state.status == SettingsStatus.saveSuccess) {
            _syncControllers(state);
          }
          if (state.status == SettingsStatus.saveSuccess &&
              state.successMessage != null) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(state.successMessage!)),
            );
          }
        },
        builder: (context, state) {
          return Scaffold(
            backgroundColor: AppColors.backgroundWhite,
            appBar: const AppSecondaryAppBar(
              title: 'Personal Information',
              fallbackRoute: RouteNames.profile,
            ),
            body: _buildBody(context, state),
          );
        },
      ),
    );
  }

  Widget _buildBody(BuildContext context, SettingsState state) {
    if (state.status == SettingsStatus.initial ||
        state.status == SettingsStatus.loading) {
      return const AppLoadingView(message: 'Loading personal information...');
    }

    if (state.status == SettingsStatus.error && state.profile == null) {
      return AppErrorView(
        message: state.failure?.message ?? 'Unable to load personal information.',
        failure: state.failure,
        onRetry: () => _cubit.load(),
      );
    }

    final profile = state.profile;
    if (profile == null) {
      return const AppLoadingView(message: 'Loading personal information...');
    }

    final isSaving = state.status == SettingsStatus.saving;

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
            _ProfileSummaryCard(profile: profile),
            const SizedBox(height: AppSpacing.xxl),
            Text(
              'Your details',
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w800,
                letterSpacing: 0.6,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              controller: _firstnameController,
              label: 'First name',
              hint: 'Enter first name',
              textInputAction: TextInputAction.next,
            ),
            if (state.fieldErrors['firstname'] != null) ...[
              const SizedBox(height: AppSpacing.xs),
              Text(
                state.fieldErrors['firstname']!,
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.accentRed,
                ),
              ),
            ],
            const SizedBox(height: AppSpacing.lg),
            AppTextField(
              controller: _lastnameController,
              label: 'Last name',
              hint: 'Enter last name',
              textInputAction: TextInputAction.next,
            ),
            if (state.fieldErrors['lastname'] != null) ...[
              const SizedBox(height: AppSpacing.xs),
              Text(
                state.fieldErrors['lastname']!,
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.accentRed,
                ),
              ),
            ],
            const SizedBox(height: AppSpacing.lg),
            AppTextField(
              controller: _bioController,
              label: 'Bio',
              hint: 'Short bio (optional)',
              textInputAction: TextInputAction.done,
              maxLines: 3,
            ),
            if (state.fieldErrors['bio'] != null) ...[
              const SizedBox(height: AppSpacing.xs),
              Text(
                state.fieldErrors['bio']!,
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.accentRed,
                ),
              ),
            ],
            const SizedBox(height: AppSpacing.xxl),
            Text(
              'Account details',
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w800,
                letterSpacing: 0.6,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              controller: _emailController,
              label: 'Email',
              hint: 'your@email.com',
              keyboardType: TextInputType.emailAddress,
              readOnly: true,
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(
              'Email is managed with your account and can’t be edited here.',
              style: AppTextStyles.caption.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            AppTextField(
              controller: _phoneController,
              label: 'Phone number',
              hint: 'Phone number',
              keyboardType: TextInputType.phone,
              readOnly: true,
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(
              'Phone number can’t be changed in the app. Contact support if you need an update.',
              style: AppTextStyles.caption.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
            if (state.failure != null &&
                state.status == SettingsStatus.saveFailure &&
                state.fieldErrors.isEmpty) ...[
              const SizedBox(height: AppSpacing.lg),
              Text(
                state.failure!.message,
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.accentRed,
                ),
              ),
            ],
            const SizedBox(height: AppSpacing.xxl),
            AppButton(
              label: 'Save changes',
              isLoading: isSaving,
              onPressed: isSaving
                  ? null
                  : () {
                      _cubit.updateProfile(
                        firstname: _firstnameController.text,
                        lastname: _lastnameController.text,
                        bio: _bioController.text,
                      );
                    },
            ),
          ],
        ),
      ),
    );
  }
}

class _ProfileSummaryCard extends StatelessWidget {
  const _ProfileSummaryCard({required this.profile});

  final Profile profile;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.xl),
      decoration: BoxDecoration(
        color: AppColors.blueTint,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          Container(
            width: 72,
            height: 72,
            clipBehavior: Clip.antiAlias,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              color: AppColors.primaryBlue,
            ),
            child: profile.avatarUrl != null && profile.avatarUrl!.isNotEmpty
                ? AppNetworkImage(
                    imageUrl: profile.avatarUrl,
                    width: 72,
                    height: 72,
                  )
                : const Icon(
                    Icons.person_outline_rounded,
                    size: 36,
                    color: AppColors.backgroundWhite,
                  ),
          ),
          const SizedBox(width: AppSpacing.lg),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  profile.displayName,
                  style: AppTextStyles.titleLarge,
                ),
                if (profile.email.isNotEmpty) ...[
                  const SizedBox(height: AppSpacing.xs),
                  Text(profile.email, style: AppTextStyles.bodyMedium),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Legacy alias kept for older imports / tests during migration.
typedef SettingsScreen = PersonalInformationScreen;
