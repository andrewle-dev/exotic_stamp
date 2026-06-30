import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../core/utils/validators.dart';
import '../../../../core/widgets/app_notice_dialog.dart';
import '../cubit/auth_cubit.dart';
import '../cubit/auth_state.dart';
import '../widgets/otp_input_field.dart';

class VerifyAccountOtpScreen extends StatefulWidget {
  const VerifyAccountOtpScreen({
    super.key,
    this.initialEmail = '',
  });

  final String initialEmail;

  @override
  State<VerifyAccountOtpScreen> createState() => _VerifyAccountOtpScreenState();
}

class _VerifyAccountOtpScreenState extends State<VerifyAccountOtpScreen> {
  static const _defaultResendCooldownSeconds = 120;

  final TextEditingController emailController = TextEditingController();
  final TextEditingController otpController = TextEditingController();
  final FocusNode otpFocusNode = FocusNode();

  Timer? _cooldownTimer;
  int _cooldownSeconds = 0;

  @override
  void initState() {
    super.initState();
    emailController.text = widget.initialEmail;
    _startCooldown(_defaultResendCooldownSeconds);
  }

  @override
  void dispose() {
    _cooldownTimer?.cancel();
    emailController.dispose();
    otpController.dispose();
    otpFocusNode.dispose();
    super.dispose();
  }

  void _startCooldown(int seconds) {
    _cooldownTimer?.cancel();
    setState(() => _cooldownSeconds = seconds);
    if (seconds <= 0) {
      return;
    }
    _cooldownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!mounted) {
        timer.cancel();
        return;
      }
      if (_cooldownSeconds <= 1) {
        timer.cancel();
        setState(() => _cooldownSeconds = 0);
        return;
      }
      setState(() => _cooldownSeconds -= 1);
    });
  }

  int? _parseCooldownSeconds(String message) {
    final match = RegExp(r'(\d+)\s*seconds?').firstMatch(message);
    if (match == null) {
      return null;
    }
    return int.tryParse(match.group(1)!);
  }

  Future<void> _showNotice({
    required String title,
    required String message,
  }) {
    return showDialog<void>(
      context: context,
      builder: (context) => AppNoticeDialog(title: title, message: message),
    );
  }

  void _submit() {
    final email = emailController.text.trim();
    final otp = otpController.text.trim();

    if (!Validators.isValidEmail(email)) {
      _showNotice(
        title: 'Email chưa hợp lệ',
        message: 'Vui lòng nhập đúng email đã đăng ký.',
      );
      return;
    }

    if (otp.length != 6) {
      _showNotice(
        title: 'Mã chưa đủ',
        message: 'Vui lòng nhập đủ 6 chữ số trong email xác minh.',
      );
      return;
    }

    context.read<AuthCubit>().verifyAccount(email: email, otp: otp);
  }

  void _resend() {
    if (_cooldownSeconds > 0) {
      return;
    }

    final email = emailController.text.trim();
    if (!Validators.isValidEmail(email)) {
      _showNotice(
        title: 'Email chưa hợp lệ',
        message: 'Vui lòng nhập email trước khi gửi lại mã.',
      );
      return;
    }

    context.read<AuthCubit>().resendVerificationOtp(email: email);
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<AuthCubit, AuthState>(
      listenWhen: (previous, current) => previous.status != current.status,
      listener: (context, state) async {
        if (state.status == AuthStatus.actionSuccess) {
          if (state.actionKey == 'resend_verification_otp') {
            _startCooldown(_defaultResendCooldownSeconds);
            await _showNotice(
              title: 'Đã gửi mã mới',
              message: state.message ??
                  'Nếu email hợp lệ, mã xác minh mới đã được gửi.',
            );
            if (!context.mounted) {
              return;
            }
            context.read<AuthCubit>().clearTransientState();
            return;
          }

          await _showNotice(
            title: 'Xác minh thành công',
            message: state.message ??
                'Tài khoản đã được kích hoạt. Bạn có thể đăng nhập ngay.',
          );
          if (!context.mounted) {
            return;
          }
          context.read<AuthCubit>().clearTransientState();
          context.go(RouteNames.login);
          return;
        }

        if (state.status == AuthStatus.failure && state.failure != null) {
          final failure = state.failure!;
          final cooldown = _parseCooldownSeconds(failure.message);
          if (cooldown != null) {
            _startCooldown(cooldown);
          }

          await _showNotice(
            title: 'Không thể tiếp tục',
            message: failure.message,
          );
          if (!context.mounted) {
            return;
          }
          context.read<AuthCubit>().clearTransientState();
        }
      },
      builder: (context, state) {
        final isSubmitting = state.status == AuthStatus.loading;
        final canResend = _cooldownSeconds <= 0 && !isSubmitting;

        return Scaffold(
          appBar: AppBar(
            toolbarHeight: 64,
            titleSpacing: 0,
            title: const Text(
              'Xác minh tài khoản',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary,
              ),
            ),
          ),
          body: SafeArea(
            top: false,
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(20, 20, 20, 28),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text(
                    'Nhập mã 6 chữ số',
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.w700,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Mã xác minh đã được gửi tới email của bạn để kích hoạt tài khoản.',
                    style: TextStyle(
                      fontSize: 15,
                      height: 1.5,
                      color: AppColors.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 24),
                  TextField(
                    controller: emailController,
                    enabled: !isSubmitting,
                    keyboardType: TextInputType.emailAddress,
                    decoration: InputDecoration(
                      labelText: 'Email',
                      filled: true,
                      fillColor: AppColors.inputBackground,
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: const BorderSide(color: AppColors.border),
                      ),
                    ),
                  ),
                  const SizedBox(height: 28),
                  Center(
                    child: OtpInputField(
                      controller: otpController,
                      focusNode: otpFocusNode,
                      enabled: !isSubmitting,
                      onCompleted: (_) => _submit(),
                    ),
                  ),
                  const SizedBox(height: 28),
                  SizedBox(
                    height: 52,
                    child: FilledButton(
                      onPressed: isSubmitting ? null : _submit,
                      style: FilledButton.styleFrom(
                        backgroundColor: AppColors.primaryBlue,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      child: isSubmitting
                          ? const SizedBox(
                              width: 22,
                              height: 22,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : const Text(
                              'Xác minh tài khoản',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  TextButton(
                    onPressed: canResend ? _resend : null,
                    child: Text(
                      _cooldownSeconds > 0
                          ? 'Gửi lại mã sau ${_cooldownSeconds}s'
                          : 'Gửi lại mã',
                      style: TextStyle(
                        color: canResend
                            ? AppColors.primaryBlue
                            : AppColors.textSecondary,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
