import 'package:flutter/foundation.dart';
import 'package:go_router/go_router.dart';

import '../../app/router/app_router.dart';
import '../config/mock_config.dart';
import '../mock/mock_data_store.dart';
import '../mock/repositories/mock_home_repository.dart';
import '../mock/repositories/mock_profile_repository.dart';
import '../mock/repositories/mock_rewards_repository.dart';
import '../mock/repositories/mock_scan_repository.dart';
import '../mock/repositories/mock_stamp_book_repository.dart';
import '../mock/repositories/mock_stations_repository.dart';
import '../../features/app_config/data/datasources/app_config_remote_datasource.dart';
import '../../features/app_config/data/repositories/app_config_repository_impl.dart';
import '../../features/app_config/domain/entities/app_update_decision.dart';
import '../../features/app_config/domain/repositories/app_config_repository.dart';
import '../../features/app_config/domain/services/app_config_startup_checker.dart';
import '../../features/app_config/domain/services/app_version_reader.dart';
import '../../features/memories/data/datasources/memories_remote_datasource.dart';
import '../../features/memories/data/repositories/memories_repository_impl.dart';
import '../../features/memories/domain/repositories/memories_repository.dart';
import '../../features/home/data/datasources/home_remote_datasource.dart';
import '../../features/home/data/repositories/home_repository_impl.dart';
import '../../features/home/domain/repositories/home_repository.dart';
import '../../features/home/presentation/home_reload_signal.dart';
import '../../features/stations/data/datasources/stations_remote_datasource.dart';
import '../../features/stations/data/repositories/stations_repository_impl.dart';
import '../../features/stations/domain/repositories/stations_repository.dart';
import '../../features/stamp_book/data/datasources/stamp_book_remote_datasource.dart';
import '../../features/stamp_book/data/repositories/stamp_book_repository_impl.dart';
import '../../features/stamp_book/domain/repositories/stamp_book_repository.dart';
import '../../features/rewards/data/datasources/rewards_remote_datasource.dart';
import '../../features/rewards/data/repositories/rewards_repository_impl.dart';
import '../../features/rewards/domain/repositories/rewards_repository.dart';
import '../../features/scan/data/datasources/scan_remote_datasource.dart';
import '../../features/scan/data/repositories/scan_repository_impl.dart';
import '../../features/scan/domain/repositories/scan_repository.dart';
import '../../features/scan/domain/usecases/check_collect_status_usecase.dart';
import '../../features/scan/domain/usecases/collect_stamp_usecase.dart';
import '../../features/scan/domain/usecases/resolve_scan_usecase.dart';
import '../../features/scan/presentation/cubit/scan_flow_cubit.dart';
import '../../features/auth/data/datasources/auth_remote_datasource.dart';
import '../../features/auth/data/repositories/auth_repository_impl.dart';
import '../../features/auth/domain/repositories/auth_repository.dart';
import '../../features/auth/domain/usecases/forgot_password_usecase.dart';
import '../../features/auth/domain/usecases/login_usecase.dart';
import '../../features/auth/domain/usecases/logout_usecase.dart';
import '../../features/auth/domain/usecases/refresh_session_usecase.dart';
import '../../features/auth/domain/usecases/register_usecase.dart';
import '../../features/auth/domain/usecases/resend_verification_otp_usecase.dart';
import '../../features/auth/domain/usecases/verify_account_usecase.dart';
import '../../features/auth/presentation/cubit/auth_cubit.dart';
import '../../features/profile/data/datasources/profile_remote_datasource.dart';
import '../../features/profile/data/repositories/profile_repository_impl.dart';
import '../../features/profile/domain/repositories/profile_repository.dart';
import '../config/api_config.dart';
import '../location/app_location_service.dart';
import '../network/api_client.dart';
import '../utils/idempotency_key_generator.dart';
import '../storage/local_preferences.dart';
import '../storage/secure_token_storage.dart';

/// Lightweight service locator for app wiring.
class Injection {
  Injection._();

  static final Injection instance = Injection._();

  late SecureTokenStorage tokenStorage;
  late LocalPreferences localPreferences;
  late ApiClient apiClient;
  late AuthRepository authRepository;
  late HomeRepository homeRepository;
  late StationsRepository stationsRepository;
  late StampBookRepository stampBookRepository;
  late RewardsRepository rewardsRepository;
  late ScanRepository scanRepository;
  late ProfileRepository profileRepository;
  late MemoriesRepository memoriesRepository;
  late ScanFlowCubit scanFlowCubit;
  late AuthCubit authCubit;
  late AppConfigRepository appConfigRepository;
  late AppConfigStartupChecker appConfigStartupChecker;
  late GoRouter router;

  /// Result of the last app-config policy check (fail-open defaults to supported).
  AppUpdateDecision appUpdateDecision = const AppUpdateDecision.supported();

  final SessionListenable sessionListenable = SessionListenable();
  final HomeReloadSignal homeReloadSignal = HomeReloadSignal();

  bool _initialized = false;

  bool get isInitialized => _initialized;

  Future<void> init({
    SecureTokenStorage? tokenStorageOverride,
    LocalPreferences? localPreferencesOverride,
    ApiClient? apiClientOverride,
    AuthRepository? authRepositoryOverride,
    AuthCubit? authCubitOverride,
    AppConfigRepository? appConfigRepositoryOverride,
    AppVersionReader? appVersionReaderOverride,
    GoRouter? routerOverride,
    bool restoreSession = true,
    bool checkAppConfig = true,
  }) async {
    if (_initialized) {
      return;
    }

    tokenStorage = tokenStorageOverride ?? SecureTokenStorage();
    localPreferences = localPreferencesOverride ?? LocalPreferences();
    await localPreferences.init();
    await ApiConfig.loadFromPreferences(localPreferences);

    apiClient = apiClientOverride ??
        await ApiClient.create(
          tokenStorage: tokenStorage,
          onSessionInvalidated: _handleSessionInvalidated,
        );

    authRepository = authRepositoryOverride ??
        AuthRepositoryImpl(
          remoteDataSource: AuthRemoteDataSource(apiClient: apiClient),
          tokenStorage: tokenStorage,
          apiClient: apiClient,
        );

    if (MockConfig.isMockMode) {
      MockDataStore.instance.reset();
      if (kDebugMode) {
        debugPrint(
          '[mock] USE_MOCK_DATA enabled — feature repositories use mock data',
        );
      }
      homeRepository = MockHomeRepository();
      stationsRepository = MockStationsRepository();
      stampBookRepository = MockStampBookRepository();
      rewardsRepository = MockRewardsRepository();
      scanRepository = MockScanRepository();
      profileRepository = MockProfileRepository();
    } else {
      homeRepository = HomeRepositoryImpl(
        remoteDataSource: HomeRemoteDataSource(apiClient: apiClient),
      );

      stationsRepository = StationsRepositoryImpl(
        remoteDataSource: StationsRemoteDataSource(apiClient: apiClient),
      );

      stampBookRepository = StampBookRepositoryImpl(
        remoteDataSource: StampBookRemoteDataSource(apiClient: apiClient),
      );

      rewardsRepository = RewardsRepositoryImpl(
        remoteDataSource: RewardsRemoteDataSource(apiClient: apiClient),
      );

      scanRepository = ScanRepositoryImpl(
        remoteDataSource: ScanRemoteDataSource(apiClient: apiClient),
      );

      profileRepository = ProfileRepositoryImpl(
        remoteDataSource: ProfileRemoteDataSource(apiClient: apiClient),
        apiClient: apiClient,
      );
    }

    memoriesRepository = MemoriesRepositoryImpl(
      remoteDataSource: MemoriesRemoteDataSource(apiClient: apiClient),
    );

    appConfigRepository = appConfigRepositoryOverride ??
        AppConfigRepositoryImpl(
          remoteDataSource: AppConfigRemoteDataSource(apiClient: apiClient),
        );
    appConfigStartupChecker = AppConfigStartupChecker(
      repository: appConfigRepository,
      versionReader:
          appVersionReaderOverride ?? const PackageInfoAppVersionReader(),
    );
    appUpdateDecision = const AppUpdateDecision.supported();

    scanFlowCubit = ScanFlowCubit(
      resolveScanUseCase: ResolveScanUseCase(scanRepository),
      collectStampUseCase: CollectStampUseCase(scanRepository),
      checkCollectStatusUseCase: CheckCollectStatusUseCase(scanRepository),
      locationService: AppLocationService(),
      idempotencyKeyGenerator: const IdempotencyKeyGenerator(),
      homeReloadSignal: homeReloadSignal,
    );

    authCubit = authCubitOverride ??
        AuthCubit(
          loginUseCase: LoginUseCase(authRepository),
          registerUseCase: RegisterUseCase(authRepository),
          forgotPasswordUseCase: ForgotPasswordUseCase(authRepository),
          verifyAccountUseCase: VerifyAccountUseCase(authRepository),
          resendVerificationOtpUseCase:
              ResendVerificationOtpUseCase(authRepository),
          refreshSessionUseCase: RefreshSessionUseCase(authRepository),
          logoutUseCase: LogoutUseCase(authRepository),
        );

    if (checkAppConfig) {
      appUpdateDecision = await appConfigStartupChecker.check();
    }

    router = routerOverride ?? createAppRouter();

    _initialized = true;

    if (restoreSession) {
      await authCubit.restoreSession();
      notifySessionChanged();
    }
  }

  /// Re-run policy check (e.g. maintenance retry). Updates gate and notifies router.
  Future<AppUpdateDecision> refreshAppUpdatePolicy() async {
    appUpdateDecision = await appConfigStartupChecker.check();
    notifySessionChanged();
    return appUpdateDecision;
  }

  Future<void> _handleSessionInvalidated() async {
    if (!authCubit.isClosed) {
      authCubit.markUnauthenticated();
    }
    notifySessionChanged();
  }

  void notifySessionChanged() {
    sessionListenable.notify();
  }

  Future<void> clearSession() async {
    await apiClient.clearSession();
    if (!authCubit.isClosed) {
      authCubit.markUnauthenticated();
    }
    notifySessionChanged();
  }

  Future<void> logout() async {
    await authCubit.logout();
    notifySessionChanged();
  }

  @visibleForTesting
  void reset() {
    if (_initialized && !authCubit.isClosed) {
      authCubit.close();
    }
    if (_initialized && !scanFlowCubit.isClosed) {
      scanFlowCubit.close();
    }
    appUpdateDecision = const AppUpdateDecision.supported();
    _initialized = false;
  }
}

/// Notifies [GoRouter] when auth or onboarding state changes.
class SessionListenable extends ChangeNotifier {
  void notify() => notifyListeners();
}
