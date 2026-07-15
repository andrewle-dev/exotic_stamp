/// Canonical route path constants for go_router.
abstract final class RouteNames {
  static const welcome = '/welcome';
  static const login = '/login';

  /// Legacy alias used by existing feature pages during migration.
  static const auth = '/auth';

  static const register = '/register';
  static const forgotPassword = '/forgot-password';
  static const verifyAccountOtp = '/verify-account-otp';
  static const forceUpdate = '/force-update';
  static const maintenance = '/maintenance';

  static String verifyAccountOtpWithEmail(String email) =>
      '$verifyAccountOtp?email=${Uri.encodeComponent(email)}';
  static const home = '/home';
  static const stampBook = '/stamp-book';

  /// MVP stamp detail route parameter.
  ///
  /// Uses [stationId] because stamp-book cells do not expose a stable stampId
  /// for every cell. Replace with stampId after
  /// `GET /collection/my-stamps/{stampId}` exists.
  static String stampDetail(String stationId) => '/stamps/$stationId';
  static const scan = '/scan';
  static const scanTapToCollect = '/scan/tap-to-collect';
  static const scanLocationVerification = '/scan/location-verification';
  static const scanSuccess = '/scan/success';
  static const scanError = '/scan/error';
  static const stations = '/stations';
  static const rewards = '/rewards';

  static String voucherDetail(String voucherId) =>
      '/rewards/vouchers/$voucherId';
  static const profile = '/profile';
  static const settings = '/profile/settings';
  static const apiDebug = '/profile/api-debug';
  static const adminNfcWriter = '/admin/nfc-writer';
  static const memoriesCreate = '/memories/create';
  static const rewardsShare = '/rewards/share';
  static const scanRewardUnlocked = '/scan/reward-unlocked';


  static String stationDetail(String stationId) => '/stations/$stationId';

  static const Set<String> publicRoutes = {
    welcome,
    login,
    auth,
    register,
    forgotPassword,
    verifyAccountOtp,
    forceUpdate,
    maintenance,
  };

  static const Set<String> shellRoutes = {
    home,
    stampBook,
    scan,
    stations,
    rewards,
    profile,
  };

  /// Tab order for [StatefulShellRoute] branches.
  static const List<String> shellTabRoutes = [
    home,
    stampBook,
    scan,
    stations,
    rewards,
    profile,
  ];
}

/// Shell branch indices aligned with [RouteNames.shellTabRoutes].
abstract final class ShellTabIndex {
  static const home = 0;
  static const book = 1;
  static const scan = 2;
  static const stations = 3;
  static const rewards = 4;
  static const profile = 5;
}
