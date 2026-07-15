import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_error_view.dart';
import '../../../../shared/widgets/app_loading_view.dart';
import '../../../../shared/widgets/app_network_image.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../domain/entities/profile.dart';
import '../../domain/usecases/get_profile_usecase.dart';
import '../../domain/usecases/logout_profile_usecase.dart';
import '../../domain/usecases/update_profile_usecase.dart';
import '../cubit/settings_cubit.dart';
import '../cubit/settings_state.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key, this.cubit});

  final SettingsCubit? cubit;

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late final TextEditingController _firstnameController;
  late final TextEditingController _lastnameController;
  late final TextEditingController _emailController;
  late final TextEditingController _phoneController;
  SettingsCubit? _ownedCubit;

  @override
  void initState() {
    super.initState();
    _firstnameController = TextEditingController();
    _lastnameController = TextEditingController();
    _emailController = TextEditingController();
    _phoneController = TextEditingController();

    if (widget.cubit == null) {
      _ownedCubit = SettingsCubit(
        getProfileUseCase: GetProfileUseCase(
          Injection.instance.profileRepository,
        ),
        updateProfileUseCase: UpdateProfileUseCase(
          Injection.instance.profileRepository,
        ),
        logoutProfileUseCase: LogoutProfileUseCase(
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
  }

  Future<void> _handleLogout(BuildContext context) async {
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

    await _cubit.logout();
    Injection.instance.authCubit.markUnauthenticated();
    Injection.instance.notifySessionChanged();

    if (context.mounted) {
      context.go(RouteNames.login);
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider.value(
      value: _cubit,
      child: BlocConsumer<SettingsCubit, SettingsState>(
        listener: (context, state) {
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
            appBar: AppBar(
              backgroundColor: AppColors.backgroundWhite,
              elevation: 0,
              leading: IconButton(
                onPressed: () => context.pop(),
                icon: const Icon(
                  Icons.arrow_back_ios_new_rounded,
                  size: 20,
                  color: AppColors.textPrimary,
                ),
              ),
              title: Text(
                'Cài đặt',
                style: AppTextStyles.titleMedium.copyWith(
                  fontWeight: FontWeight.w800,
                ),
              ),
              bottom: const PreferredSize(
                preferredSize: Size.fromHeight(1),
                child: Divider(height: 1, color: AppColors.border),
              ),
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
      return const AppLoadingView(message: 'Đang tải cài đặt...');
    }

    if (state.profile == null &&
        (state.status == SettingsStatus.saveFailure ||
            state.status == SettingsStatus.initial)) {
      return AppErrorView(
        message: state.failure?.message ?? 'Không thể tải cài đặt.',
        failure: state.failure,
        onRetry: () => _cubit.load(),
      );
    }

    final profile = state.profile;
    if (profile == null) {
      return const AppLoadingView(message: 'Đang tải cài đặt...');
    }

    final isSaving = state.status == SettingsStatus.saving;
    final isLoggingOut = state.status == SettingsStatus.loggingOut;

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
              'THÔNG TIN CÁ NHÂN',
              style: AppTextStyles.caption.copyWith(
                fontWeight: FontWeight.w800,
                letterSpacing: 0.6,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            AppTextField(
              controller: _firstnameController,
              label: 'Tên',
              hint: 'Nhập tên',
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
              label: 'Họ',
              hint: 'Nhập họ',
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
              controller: _emailController,
              label: 'Email',
              hint: 'Email',
              keyboardType: TextInputType.emailAddress,
              readOnly: true,
            ),
            const SizedBox(height: AppSpacing.xs),
            const Text(
              'Email không thể thay đổi qua ứng dụng.',
              style: AppTextStyles.caption,
            ),
            const SizedBox(height: AppSpacing.lg),
            AppTextField(
              controller: _phoneController,
              label: 'Số điện thoại',
              hint: 'Số điện thoại',
              keyboardType: TextInputType.phone,
              readOnly: true,
            ),
            const SizedBox(height: AppSpacing.xs),
            const Text(
              'Số điện thoại chỉ đọc — backend chưa hỗ trợ cập nhật.',
              style: AppTextStyles.caption,
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
              label: 'Lưu thay đổi',
              isLoading: isSaving,
              onPressed: isSaving || isLoggingOut
                  ? null
                  : () {
                      _cubit.updateProfile(
                        firstname: _firstnameController.text,
                        lastname: _lastnameController.text,
                      );
                    },
            ),
            const SizedBox(height: AppSpacing.huge),
            AppButton(
              label: 'Đăng xuất',
              variant: AppButtonVariant.outlined,
              isLoading: isLoggingOut,
              onPressed: isSaving || isLoggingOut
                  ? null
                  : () => _handleLogout(context),
              icon: const Icon(
                Icons.logout_rounded,
                color: AppColors.accentRed,
                size: 20,
              ),
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
