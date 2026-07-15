/// Bundled asset paths used by chrome UI (never load logo from CDN).
abstract final class AppAssets {
  static const logo = 'assets/logo/ExoticStamp_logo.png';
}

@Deprecated('Use AppAssets.logo')
abstract final class AppLogoAssets {
  static const path = AppAssets.logo;
}
