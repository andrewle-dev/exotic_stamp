import '../../domain/entities/mobile_app_config.dart';
import '../../domain/repositories/app_config_repository.dart';
import '../datasources/app_config_remote_datasource.dart';

class AppConfigRepositoryImpl implements AppConfigRepository {
  AppConfigRepositoryImpl({required AppConfigRemoteDataSource remoteDataSource})
      : _remoteDataSource = remoteDataSource;

  final AppConfigRemoteDataSource _remoteDataSource;

  @override
  Future<MobileAppConfig> fetchAppConfig() async {
    final model = await _remoteDataSource.fetchAppConfig();
    return model.toEntity();
  }
}
