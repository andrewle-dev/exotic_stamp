import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../core/di/injection.dart';
import '../../../../core/utils/validators.dart';
import '../../../../core/widgets/app_notice_dialog.dart';
import '../cubit/auth_cubit.dart';
import '../cubit/auth_state.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final TextEditingController identifierController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  bool rememberMe = false;
  bool obscurePassword = true;

  @override
  void dispose() {
    identifierController.dispose();
    passwordController.dispose();
    super.dispose();
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

  void _submitLogin() {
    final identifier = identifierController.text.trim();
    final password = passwordController.text;

    if (!Validators.isNotEmpty(identifier)) {
      _showNotice(
        title: 'Thiếu tài khoản',
        message: 'Vui lòng nhập email hoặc tên đăng nhập để tiếp tục.',
      );
      return;
    }

    if (!Validators.isNotEmpty(password)) {
      _showNotice(
        title: 'Thiếu mật khẩu',
        message: 'Vui lòng nhập mật khẩu trước khi đăng nhập.',
      );
      return;
    }

    context.read<AuthCubit>().login(
          identifier: identifier,
          password: password,
        );
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<AuthCubit, AuthState>(
      listenWhen: (previous, current) => previous.status != current.status,
      listener: (context, state) async {
        if (state.status == AuthStatus.authenticated) {
          Injection.instance.notifySessionChanged();
          await _showNotice(
            title: 'Đăng nhập thành công',
            message: rememberMe
                ? 'Chào mừng bạn quay lại. Phiên đăng nhập sẽ được ghi nhớ trên thiết bị này.'
                : 'Chào mừng bạn quay lại Metro Stamp.',
          );
          if (!context.mounted) {
            return;
          }
          context.go(RouteNames.home);
          return;
        }

        if (state.status == AuthStatus.failure && state.failure != null) {
          await _showNotice(
            title: 'Đăng nhập thất bại',
            message: state.failure!.message,
          );
          if (!context.mounted) {
            return;
          }
          context.read<AuthCubit>().clearTransientState();
        }
      },
      builder: (context, state) {
        final isSubmitting = state.status == AuthStatus.loading;

        return Scaffold(
          appBar: AppBar(
            centerTitle: true,
            toolbarHeight: 56,
            leading: const SizedBox.shrink(),
            title: const Text.rich(
              TextSpan(
                children: [
                  TextSpan(
                    text: 'Exotic ',
                    style: TextStyle(
                      color: AppColors.primaryBlue,
                      fontSize: 18,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  TextSpan(
                    text: 'Stamp',
                    style: TextStyle(
                      color: AppColors.accentRed,
                      fontSize: 18,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
          ),
          body: SafeArea(
            top: false,
            child: Stack(
              children: [
                Positioned(
                  top: -24,
                  right: -40,
                  child: Container(
                    width: 120,
                    height: 120,
                    decoration: const BoxDecoration(
                      color: AppColors.redTint,
                      shape: BoxShape.circle,
                    ),
                  ),
                ),
                Positioned(
                  bottom: -96,
                  left: -72,
                  child: Container(
                    width: 170,
                    height: 170,
                    decoration: const BoxDecoration(
                      color: AppColors.blueTint,
                      shape: BoxShape.circle,
                    ),
                  ),
                ),
                SingleChildScrollView(
                  padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
                  child: Column(
                    children: [
                      SizedBox(
                        width: 100,
                        height: 100,
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(24),
                          child: Padding(
                            padding: const EdgeInsets.all(10),
                            child: Image.asset(
                              'assets/logo/ExoticStamp_logo.png',
                              fit: BoxFit.contain,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),
                      const Text(
                        'Chào mừng trở lại',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.w800,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Vui lòng đăng nhập để tiếp tục',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 15,
                          color: AppColors.textSecondary,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      const SizedBox(height: 24),
                      const _FieldLabel(text: 'Email hoặc Tên Đăng Nhập'),
                      const SizedBox(height: 8),
                      _InputField(
                        hintText: 'example@mail.com',
                        prefixIcon: Icons.mail_outline,
                        controller: identifierController,
                      ),
                      const SizedBox(height: 16),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const _FieldLabel(text: 'Mật Khẩu'),
                          TextButton(
                            onPressed: () =>
                                context.push(RouteNames.forgotPassword),
                            style: TextButton.styleFrom(
                              padding: EdgeInsets.zero,
                              minimumSize: Size.zero,
                              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                            ),
                            child: const Text(
                              'Quên mật khẩu?',
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w700,
                                color: AppColors.accentRed,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      _InputField(
                        hintText: '........',
                        prefixIcon: Icons.lock_outline,
                        controller: passwordController,
                        suffixIcon: IconButton(
                          onPressed: () {
                            setState(() {
                              obscurePassword = !obscurePassword;
                            });
                          },
                          icon: Icon(
                            obscurePassword
                                ? Icons.visibility_outlined
                                : Icons.visibility_off_outlined,
                            color: AppColors.textSecondary,
                          ),
                        ),
                        obscureText: obscurePassword,
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          SizedBox(
                            height: 24,
                            child: Checkbox(
                              value: rememberMe,
                              onChanged: (value) {
                                setState(() {
                                  rememberMe = value ?? false;
                                });
                              },
                            ),
                          ),
                          const SizedBox(width: 1),
                          const Text(
                            'Ghi nhớ đăng nhập',
                            style: TextStyle(
                              fontSize: 15,
                              color: AppColors.textPrimary,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 18),
                      SizedBox(
                        width: double.infinity,
                        height: 52,
                        child: ElevatedButton(
                          onPressed: isSubmitting ? null : _submitLogin,
                          child: isSubmitting
                              ? const SizedBox(
                                  width: 22,
                                  height: 22,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2.2,
                                    color: AppColors.backgroundWhite,
                                  ),
                                )
                              : const Text('Đăng nhập'),
                        ),
                      ),
                      const SizedBox(height: 20),
                      const Row(
                        children: [
                          Expanded(child: Divider(color: AppColors.border)),
                          Padding(
                            padding: EdgeInsets.symmetric(horizontal: 12),
                            child: Text(
                              'HOẶC TIẾP TỤC VỚI',
                              style: TextStyle(
                                color: AppColors.textSecondary,
                                fontSize: 12,
                                fontWeight: FontWeight.w700,
                                letterSpacing: 0.6,
                              ),
                            ),
                          ),
                          Expanded(child: Divider(color: AppColors.border)),
                        ],
                      ),
                      const SizedBox(height: 12),
                      const Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          _SocialButton(
                            icon: FontAwesomeIcons.google,
                            color: AppColors.accentRed,
                          ),
                          SizedBox(width: 10),
                          _SocialButton(
                            icon: FontAwesomeIcons.facebookF,
                            color: AppColors.accentRed,
                          ),
                          SizedBox(width: 10),
                          _SocialButton(
                            icon: FontAwesomeIcons.apple,
                            color: AppColors.accentRed,
                          ),
                        ],
                      ),
                      const SizedBox(height: 20),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Text(
                            'Chưa có tài khoản? ',
                            style: TextStyle(
                              fontSize: 15,
                              color: AppColors.textSecondary,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          GestureDetector(
                            onTap: () => context.push(RouteNames.register),
                            child: const Text(
                              'Đăng ký ngay',
                              style: TextStyle(
                                fontSize: 15,
                                color: AppColors.primaryBlue,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _FieldLabel extends StatelessWidget {
  const _FieldLabel({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Align(
      alignment: Alignment.centerLeft,
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.w700,
          color: AppColors.textPrimary,
        ),
      ),
    );
  }
}

class _InputField extends StatelessWidget {
  const _InputField({
    required this.hintText,
    required this.prefixIcon,
    required this.controller,
    this.suffixIcon,
    this.obscureText = false,
  });

  final String hintText;
  final IconData prefixIcon;
  final TextEditingController controller;
  final Widget? suffixIcon;
  final bool obscureText;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      obscureText: obscureText,
      decoration: InputDecoration(
        hintText: hintText,
        prefixIcon: Icon(prefixIcon, color: AppColors.primaryBlue),
        suffixIcon: suffixIcon,
      ),
    );
  }
}

class _SocialButton extends StatelessWidget {
  const _SocialButton({
    required this.icon,
    required this.color,
  });

  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 74,
      height: 52,
      decoration: BoxDecoration(
        color: AppColors.backgroundWhite,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.primaryBlue),
      ),
      child: Center(
        child: FaIcon(icon, size: 22, color: color),
      ),
    );
  }
}
