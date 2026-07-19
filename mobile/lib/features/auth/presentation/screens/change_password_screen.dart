import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/errors/failure.dart';
import '../../../../core/utils/validators.dart';
import '../../../../shared/widgets/app_back_button.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../domain/usecases/change_password_usecase.dart';
import '../cubit/change_password_cubit.dart';
import '../cubit/change_password_state.dart';

class ChangePasswordScreen extends StatefulWidget {
  const ChangePasswordScreen({super.key, this.cubit});

  final ChangePasswordCubit? cubit;

  @override
  State<ChangePasswordScreen> createState() => _ChangePasswordScreenState();
}

class _ChangePasswordScreenState extends State<ChangePasswordScreen> {
  final _formKey = GlobalKey<FormState>();
  final _currentController = TextEditingController();
  final _newController = TextEditingController();
  final _confirmController = TextEditingController();

  bool _obscureCurrent = true;
  bool _obscureNew = true;
  bool _obscureConfirm = true;
  ChangePasswordCubit? _ownedCubit;
  String? _inlineError;

  @override
  void initState() {
    super.initState();
    if (widget.cubit == null) {
      _ownedCubit = ChangePasswordCubit(
        changePasswordUseCase: ChangePasswordUseCase(
          Injection.instance.authRepository,
        ),
      );
    }
  }

  @override
  void dispose() {
    _currentController.dispose();
    _newController.dispose();
    _confirmController.dispose();
    _ownedCubit?.close();
    super.dispose();
  }

  ChangePasswordCubit get _cubit => widget.cubit ?? _ownedCubit!;

  Future<void> _onSuccess(BuildContext context) async {
    await Injection.instance.apiClient.clearSession();
    Injection.instance.authCubit.markUnauthenticated();
    Injection.instance.notifySessionChanged();
    if (!context.mounted) {
      return;
    }
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text(
          'Password changed successfully. Please sign in again.',
        ),
      ),
    );
    context.go(RouteNames.login);
  }

  void _submit() {
    setState(() => _inlineError = null);
    final current = _currentController.text;
    final next = _newController.text;
    final confirm = _confirmController.text;

    if (current.trim().isEmpty) {
      setState(() => _inlineError = 'Enter your current password.');
      return;
    }
    if (!Validators.isValidPassword(next)) {
      setState(
        () => _inlineError =
            'New password must be at least ${Validators.minPasswordLength} characters.',
      );
      return;
    }
    if (next != confirm) {
      setState(() => _inlineError = 'New password and confirmation do not match.');
      return;
    }
    if (next == current) {
      setState(
        () => _inlineError =
            'New password must be different from the current password.',
      );
      return;
    }

    _cubit.submit(
      currentPassword: current,
      newPassword: next,
      confirmNewPassword: confirm,
    );
  }

  String _mapFailure(Failure failure) {
    switch (failure.backendCode) {
      case 'CURRENT_PASSWORD_INCORRECT':
        return 'Current password is incorrect.';
      case 'PASSWORD_CONFIRMATION_MISMATCH':
        return 'New password and confirmation do not match.';
      case 'NEW_PASSWORD_SAME_AS_CURRENT':
        return 'New password must be different from the current password.';
      case 'PASSWORD_POLICY_VIOLATION':
        return failure.message;
      default:
        if (failure.code == FailureCode.invalidCredentials) {
          return 'Current password is incorrect.';
        }
        return failure.message;
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider.value(
      value: _cubit,
      child: BlocConsumer<ChangePasswordCubit, ChangePasswordState>(
        listener: (context, state) {
          if (state.status == ChangePasswordStatus.success) {
            _onSuccess(context);
          } else if (state.status == ChangePasswordStatus.failure &&
              state.failure != null) {
            setState(() => _inlineError = _mapFailure(state.failure!));
          }
        },
        builder: (context, state) {
          final submitting = state.isSubmitting;
          return Scaffold(
            backgroundColor: AppColors.backgroundWhite,
            appBar: AppBar(
              backgroundColor: AppColors.backgroundWhite,
              foregroundColor: AppColors.textPrimary,
              elevation: 0,
              automaticallyImplyLeading: false,
              leading: const AppBackButton(
                fallbackRoute: RouteNames.privacySecurity,
              ),
              title: Text(
                'Change Password',
                style: AppTextStyles.titleMedium.copyWith(
                  fontWeight: FontWeight.w800,
                ),
              ),
              bottom: const PreferredSize(
                preferredSize: Size.fromHeight(1),
                child: Divider(height: 1, color: AppColors.border),
              ),
            ),
            body: SafeArea(
              top: false,
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(
                  AppSpacing.xl,
                  AppSpacing.xxl,
                  AppSpacing.xl,
                  AppSpacing.huge,
                ),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Update your password. After a successful change you will '
                        'need to sign in again on all devices.',
                        style: AppTextStyles.bodyMedium.copyWith(
                          color: AppColors.textSecondary,
                        ),
                      ),
                      const SizedBox(height: AppSpacing.xxl),
                      AppTextField(
                        controller: _currentController,
                        label: 'Current password',
                        hint: 'Enter current password',
                        obscureText: _obscureCurrent,
                        enabled: !submitting,
                        textInputAction: TextInputAction.next,
                        prefixIcon: const Icon(Icons.lock_outline),
                        suffixIcon: IconButton(
                          onPressed: submitting
                              ? null
                              : () => setState(
                                    () => _obscureCurrent = !_obscureCurrent,
                                  ),
                          icon: Icon(
                            _obscureCurrent
                                ? Icons.visibility_outlined
                                : Icons.visibility_off_outlined,
                          ),
                        ),
                      ),
                      const SizedBox(height: AppSpacing.lg),
                      AppTextField(
                        controller: _newController,
                        label: 'New password',
                        hint: 'At least ${Validators.minPasswordLength} characters',
                        obscureText: _obscureNew,
                        enabled: !submitting,
                        textInputAction: TextInputAction.next,
                        prefixIcon: const Icon(Icons.lock_outline),
                        suffixIcon: IconButton(
                          onPressed: submitting
                              ? null
                              : () => setState(() => _obscureNew = !_obscureNew),
                          icon: Icon(
                            _obscureNew
                                ? Icons.visibility_outlined
                                : Icons.visibility_off_outlined,
                          ),
                        ),
                      ),
                      const SizedBox(height: AppSpacing.lg),
                      AppTextField(
                        controller: _confirmController,
                        label: 'Confirm new password',
                        hint: 'Re-enter new password',
                        obscureText: _obscureConfirm,
                        enabled: !submitting,
                        textInputAction: TextInputAction.done,
                        onSubmitted: (_) {
                          if (!submitting) {
                            _submit();
                          }
                        },
                        prefixIcon: const Icon(Icons.verified_user_outlined),
                        suffixIcon: IconButton(
                          onPressed: submitting
                              ? null
                              : () => setState(
                                    () => _obscureConfirm = !_obscureConfirm,
                                  ),
                          icon: Icon(
                            _obscureConfirm
                                ? Icons.visibility_outlined
                                : Icons.visibility_off_outlined,
                          ),
                        ),
                      ),
                      if (_inlineError != null) ...[
                        const SizedBox(height: AppSpacing.lg),
                        Text(
                          _inlineError!,
                          style: AppTextStyles.bodyMedium.copyWith(
                            color: AppColors.accentRed,
                          ),
                        ),
                      ],
                      const SizedBox(height: AppSpacing.xxl),
                      AppButton(
                        label: 'Change password',
                        isLoading: submitting,
                        onPressed: submitting ? null : _submit,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
