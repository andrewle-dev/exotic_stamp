import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../core/utils/validators.dart';
import '../../../../core/widgets/app_notice_dialog.dart';
import '../../domain/usecases/register_usecase.dart';
import '../cubit/auth_cubit.dart';
import '../cubit/auth_state.dart';
import '../utils/auth_form_utils.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final TextEditingController fullNameController = TextEditingController();
  final TextEditingController emailController = TextEditingController();
  final TextEditingController phoneController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();
  final TextEditingController confirmPasswordController =
      TextEditingController();

  bool agreeToTerms = false;
  bool obscurePassword = true;
  bool obscureConfirmPassword = true;

  @override
  void dispose() {
    fullNameController.dispose();
    emailController.dispose();
    phoneController.dispose();
    passwordController.dispose();
    confirmPasswordController.dispose();
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

  void _submitRegister() {
    final fullName = fullNameController.text.trim();
    final email = emailController.text.trim();
    final phone = phoneController.text.trim();
    final password = passwordController.text;
    final confirmPassword = confirmPasswordController.text;

    if (!Validators.isNotEmpty(fullName)) {
      _showNotice(
        title: 'Thiếu họ tên',
        message: 'Vui lòng nhập họ và tên của bạn.',
      );
      return;
    }

    if (!Validators.isValidEmail(email)) {
      _showNotice(
        title: 'Email chưa hợp lệ',
        message:
            'Vui lòng nhập đúng định dạng email, ví dụ `example@gmail.com`.',
      );
      return;
    }

    if (!Validators.isValidPhone(phone)) {
      _showNotice(
        title: 'Thiếu số điện thoại',
        message: 'Vui lòng nhập số điện thoại hợp lệ (8-15 chữ số).',
      );
      return;
    }

    if (!Validators.isValidPassword(password)) {
      _showNotice(
        title: 'Mật khẩu chưa đạt yêu cầu',
        message:
            'Mật khẩu cần có ít nhất ${Validators.minPasswordLength} ký tự.',
      );
      return;
    }

    if (password != confirmPassword) {
      _showNotice(
        title: 'Mật khẩu không khớp',
        message: 'Vui lòng kiểm tra lại phần xác nhận mật khẩu.',
      );
      return;
    }

    if (!agreeToTerms) {
      _showNotice(
        title: 'Chưa đồng ý điều khoản',
        message:
            'Bạn cần đồng ý Điều khoản dịch vụ và Chính sách bảo mật để tiếp tục.',
      );
      return;
    }

    final nameParts = AuthFormUtils.splitFullName(fullName);
    context.read<AuthCubit>().register(
          RegisterParams(
            firstname: nameParts.firstname,
            lastname: nameParts.lastname,
            username: AuthFormUtils.usernameFromEmail(email),
            email: email,
            phoneNumber: AuthFormUtils.normalizePhone('+84', phone),
            password: password,
          ),
        );
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<AuthCubit, AuthState>(
      listenWhen: (previous, current) => previous.status != current.status,
      listener: (context, state) async {
        if (state.status == AuthStatus.actionSuccess) {
          final email = emailController.text.trim();
          if (!context.mounted) {
            return;
          }
          context.read<AuthCubit>().clearTransientState();
          context.go(RouteNames.verifyAccountOtpWithEmail(email));
          return;
        }

        if (state.status == AuthStatus.failure && state.failure != null) {
          await _showNotice(
            title: 'Đăng ký chưa thành công',
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
            toolbarHeight: 64,
            titleSpacing: 0,
            title: const Text(
              'Tạo tài khoản',
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
              padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Center(
                    child: Column(
                      children: [
                        Text(
                          'Bắt đầu ngay',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 28,
                            fontWeight: FontWeight.w800,
                            color: AppColors.textPrimary,
                          ),
                        ),
                        SizedBox(height: 10),
                        Text.rich(
                          TextSpan(
                            style: TextStyle(
                              fontSize: 16,
                              height: 1.45,
                              color: AppColors.textSecondary,
                              fontWeight: FontWeight.w500,
                            ),
                            children: [
                              TextSpan(text: 'Tham gia cùng với '),
                              TextSpan(
                                text: 'Exotic ',
                                style: TextStyle(
                                  color: AppColors.primaryBlue,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              TextSpan(
                                text: 'Stamp',
                                style: TextStyle(
                                  color: AppColors.accentRed,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              TextSpan(
                                text:
                                    ' và hàng ngàn người dùng khác để trải nghiệm đầy đủ dịch vụ.',
                              ),
                            ],
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 28),
                  const _RegisterLabel(text: 'Họ và Tên'),
                  const SizedBox(height: 8),
                  _RegisterInputField(
                    hintText: 'Nguyễn Văn A',
                    prefixIcon: Icons.person_outline,
                    controller: fullNameController,
                  ),
                  const SizedBox(height: 18),
                  const _RegisterLabel(text: 'Email'),
                  const SizedBox(height: 8),
                  _RegisterInputField(
                    hintText: 'example@gmail.com',
                    prefixIcon: Icons.mail_outline,
                    controller: emailController,
                  ),
                  const SizedBox(height: 18),
                  const _RegisterLabel(text: 'Số điện thoại'),
                  const SizedBox(height: 8),
                  _RegisterInputField(
                    hintText: '901234567',
                    prefixIcon: Icons.phone_outlined,
                    controller: phoneController,
                    keyboardType: TextInputType.phone,
                  ),
                  const SizedBox(height: 18),
                  const _RegisterLabel(text: 'Mật khẩu'),
                  const SizedBox(height: 8),
                  _RegisterInputField(
                    hintText: '........',
                    prefixIcon: Icons.lock_outline,
                    controller: passwordController,
                    obscureText: obscurePassword,
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
                  ),
                  const SizedBox(height: 18),
                  const _RegisterLabel(text: 'Xác nhận mật khẩu'),
                  const SizedBox(height: 8),
                  _RegisterInputField(
                    hintText: '........',
                    prefixIcon: Icons.verified_user_outlined,
                    controller: confirmPasswordController,
                    obscureText: obscureConfirmPassword,
                    suffixIcon: IconButton(
                      onPressed: () {
                        setState(() {
                          obscureConfirmPassword = !obscureConfirmPassword;
                        });
                      },
                      icon: Icon(
                        obscureConfirmPassword
                            ? Icons.visibility_outlined
                            : Icons.visibility_off_outlined,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Padding(
                        padding: const EdgeInsets.only(top: 2),
                        child: SizedBox(
                          height: 24,
                          width: 24,
                          child: Checkbox(
                            value: agreeToTerms,
                            onChanged: (value) {
                              setState(() {
                                agreeToTerms = value ?? false;
                              });
                            },
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: RichText(
                          text: const TextSpan(
                            style: TextStyle(
                              fontSize: 15,
                              height: 1.45,
                              color: AppColors.textSecondary,
                              fontWeight: FontWeight.w500,
                            ),
                            children: [
                              TextSpan(text: 'Tôi đồng ý với '),
                              TextSpan(
                                text: 'Điều khoản dịch vụ',
                                style: TextStyle(
                                  color: AppColors.accentRed,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              TextSpan(text: ' và '),
                              TextSpan(
                                text: 'Chính sách bảo mật.',
                                style: TextStyle(
                                  color: AppColors.accentRed,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 22),
                  SizedBox(
                    width: double.infinity,
                    height: 54,
                    child: ElevatedButton(
                      onPressed: isSubmitting ? null : _submitRegister,
                      child: isSubmitting
                          ? const SizedBox(
                              width: 22,
                              height: 22,
                              child: CircularProgressIndicator(
                                strokeWidth: 2.2,
                                color: AppColors.backgroundWhite,
                              ),
                            )
                          : const Text('Đăng Ký Ngay'),
                    ),
                  ),
                  const SizedBox(height: 22),
                  const Row(
                    children: [
                      Expanded(child: Divider(color: AppColors.border)),
                      Padding(
                        padding: EdgeInsets.symmetric(horizontal: 12),
                        child: Text(
                          'HOẶC ĐĂNG KÝ BẰNG',
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
                  const SizedBox(height: 14),
                  const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      _RegisterSocialButton(
                        icon: FontAwesomeIcons.google,
                        color: AppColors.accentRed,
                      ),
                      SizedBox(width: 10),
                      _RegisterSocialButton(
                        icon: FontAwesomeIcons.facebookF,
                        color: AppColors.accentRed,
                      ),
                      SizedBox(width: 10),
                      _RegisterSocialButton(
                        icon: FontAwesomeIcons.apple,
                        color: AppColors.accentRed,
                      ),
                    ],
                  ),
                  const SizedBox(height: 22),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Text(
                        'Bạn đã có tài khoản? ',
                        style: TextStyle(
                          fontSize: 15,
                          color: AppColors.textSecondary,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      GestureDetector(
                        onTap: () => context.go(RouteNames.login),
                        child: const Text(
                          'Đăng nhập ngay',
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
          ),
        );
      },
    );
  }
}

class _RegisterLabel extends StatelessWidget {
  const _RegisterLabel({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: const TextStyle(
        fontSize: 15,
        color: AppColors.textPrimary,
        fontWeight: FontWeight.w700,
      ),
    );
  }
}

class _RegisterInputField extends StatelessWidget {
  const _RegisterInputField({
    required this.hintText,
    required this.prefixIcon,
    required this.controller,
    this.suffixIcon,
    this.obscureText = false,
    this.keyboardType,
  });

  final String hintText;
  final IconData prefixIcon;
  final TextEditingController controller;
  final Widget? suffixIcon;
  final bool obscureText;
  final TextInputType? keyboardType;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      obscureText: obscureText,
      keyboardType: keyboardType,
      decoration: InputDecoration(
        hintText: hintText,
        prefixIcon: Icon(prefixIcon, color: AppColors.primaryBlue),
        suffixIcon: suffixIcon,
      ),
    );
  }
}

class _RegisterSocialButton extends StatelessWidget {
  const _RegisterSocialButton({
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
