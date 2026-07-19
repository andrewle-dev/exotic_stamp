import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/router/route_names.dart';
import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/config/api_config.dart';
import '../../../../core/di/injection.dart';
import '../../../rewards/domain/entities/reward_unlocked_share_payload.dart';

/// Shows and optionally overrides the API base URL for physical-device local dev.
class ApiDebugScreen extends StatefulWidget {
  const ApiDebugScreen({super.key});

  @override
  State<ApiDebugScreen> createState() => _ApiDebugScreenState();
}

class _ApiDebugScreenState extends State<ApiDebugScreen> {
  late final TextEditingController _hostController;
  late final TextEditingController _portController;
  String _currentBaseUrl = ApiConfig.baseUrl;

  @override
  void initState() {
    super.initState();
    final prefs = Injection.instance.localPreferences;
    _hostController = TextEditingController(text: prefs.apiHostOverride ?? '');
    _portController =
        TextEditingController(text: prefs.apiPortOverride ?? '8080');
  }

  @override
  void dispose() {
    _hostController.dispose();
    _portController.dispose();
    super.dispose();
  }

  Future<void> _apply() async {
    final host = _hostController.text.trim();
    final port = _portController.text.trim();
    final prefs = Injection.instance.localPreferences;
    await prefs.setApiHostOverride(host.isEmpty ? null : host);
    await prefs.setApiPortOverride(port.isEmpty ? null : port);
    ApiConfig.applyRuntimeOverride(
      host: host.isEmpty ? null : host,
      port: port.isEmpty ? null : port,
    );
    Injection.instance.apiClient.updateBaseUrl(ApiConfig.baseUrl);
    setState(() {
      _currentBaseUrl = ApiConfig.baseUrl;
    });
    if (!mounted) {
      return;
    }
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('API base URL set to $_currentBaseUrl')),
    );
  }

  Future<void> _clear() async {
    final prefs = Injection.instance.localPreferences;
    await prefs.setApiHostOverride(null);
    await prefs.setApiPortOverride(null);
    ApiConfig.applyRuntimeOverride(host: null, port: null);
    Injection.instance.apiClient.updateBaseUrl(ApiConfig.baseUrl);
    _hostController.clear();
    _portController.text = '8080';
    setState(() {
      _currentBaseUrl = ApiConfig.baseUrl;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        title: const Text('API Debug'),
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.xl),
        children: [
          const Text('Current API base URL', style: AppTextStyles.titleMedium),
          const SizedBox(height: AppSpacing.md),
          SelectableText(
            _currentBaseUrl,
            style: AppTextStyles.bodyMedium.copyWith(fontFamily: 'monospace'),
          ),
          const SizedBox(height: AppSpacing.xl),
          Text(
            'Physical devices cannot use localhost. Set your machine LAN IP '
            '(e.g. 192.168.1.10). Android emulator can use 10.0.2.2.',
            style: AppTextStyles.bodyMedium.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: AppSpacing.xl),
          TextField(
            controller: _hostController,
            decoration: const InputDecoration(
              labelText: 'API host (LAN IP)',
              hintText: '192.168.x.x',
              border: OutlineInputBorder(),
            ),
            keyboardType: TextInputType.url,
          ),
          const SizedBox(height: AppSpacing.lg),
          TextField(
            controller: _portController,
            decoration: const InputDecoration(
              labelText: 'API port',
              border: OutlineInputBorder(),
            ),
            keyboardType: TextInputType.number,
          ),
          const SizedBox(height: AppSpacing.xl),
          FilledButton(
            onPressed: _apply,
            child: const Text('Apply'),
          ),
          const SizedBox(height: AppSpacing.md),
          OutlinedButton(
            onPressed: _clear,
            child: const Text('Clear override'),
          ),
          if (kDebugMode) ...[
            const SizedBox(height: AppSpacing.xxl),
            const Divider(),
            const SizedBox(height: AppSpacing.lg),
            Text(
              'UI previews',
              style: AppTextStyles.titleMedium.copyWith(
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: AppSpacing.sm),
            Text(
              'Collect response has no newlyIssuedReward yet. '
              'Use this to preview RewardUnlockedShareScreen only.',
              style: AppTextStyles.bodyMedium.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            OutlinedButton(
              onPressed: () {
                context.push(
                  RouteNames.scanRewardUnlocked,
                  extra: const RewardUnlockedSharePayload(
                    rewardId: 'debug-reward-preview',
                    rewardTitle: 'Debug Coffee Voucher',
                    partnerName: 'Metro Café',
                    offerTitle: 'Free Coffee',
                    milestoneName: '3 stamps',
                    unlockCondition: 'Debug preview — not from collect API',
                    voucherCode: 'DEBUG50',
                  ),
                );
              },
              child: const Text('Preview Reward Unlocked'),
            ),
          ],
        ],
      ),
    );
  }
}
