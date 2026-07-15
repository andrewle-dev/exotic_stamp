import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/core/errors/error_mapper.dart';
import 'package:metro_stamp_app/core/errors/failure.dart';

void main() {
  const mapper = ErrorMapper();

  group('ErrorMapper', () {
    test('maps INVALID_CREDENTIALS', () {
      final failure = mapper.fromJson({
        'code': 'INVALID_CREDENTIALS',
        'message': 'Invalid email or password',
        'status': 401,
      });

      expect(failure.code, FailureCode.invalidCredentials);
      expect(failure.message, 'Invalid email or password');
      expect(failure.statusCode, 401);
    });

    test('maps STAMP_ALREADY_COLLECTED to stampDuplicate', () {
      final failure = mapper.fromJson({'code': 'STAMP_ALREADY_COLLECTED'});

      expect(failure.code, FailureCode.stampDuplicate);
    });

    test('maps SCAN_KEY_NOT_FOUND to nfcInvalid', () {
      final failure = mapper.fromJson({'code': 'SCAN_KEY_NOT_FOUND'});

      expect(failure.code, FailureCode.nfcInvalid);
    });

    test('maps SCAN_KEY_INACTIVE to qrExpired', () {
      final failure = mapper.fromJson({'code': 'SCAN_KEY_INACTIVE'});

      expect(failure.code, FailureCode.qrExpired);
    });

    test('maps GPS_OUT_OF_RANGE to gpsOutsideRange', () {
      final failure = mapper.fromJson({'code': 'GPS_OUT_OF_RANGE'});

      expect(failure.code, FailureCode.gpsOutsideRange);
    });

    test('maps REDEEM_NOT_SUPPORTED', () {
      final failure = mapper.fromJson({'code': 'REDEEM_NOT_SUPPORTED'});

      expect(failure.code, FailureCode.redeemNotSupported);
    });

    test('maps DEFAULT_CAMPAIGN_AMBIGUOUS to localized message', () {
      final failure = mapper.fromJson({
        'code': 'DEFAULT_CAMPAIGN_AMBIGUOUS',
        'message':
            'Multiple active default campaigns found (2); provide lineId to disambiguate or configure a single global default',
        'status': 422,
      });

      expect(failure.code, FailureCode.defaultCampaignAmbiguous);
      expect(failure.message, ErrorMapper.defaultCampaignAmbiguousMessage);
      expect(failure.statusCode, 422);
    });

    test('maps ambiguous campaign message without backend code', () {
      final failure = mapper.fromJson({
        'message':
            'Multiple active default campaigns found (2); provide lineId to disambiguate',
      });

      expect(failure.code, FailureCode.defaultCampaignAmbiguous);
      expect(failure.message, ErrorMapper.defaultCampaignAmbiguousMessage);
    });

    test('uses default message when backend message is missing', () {
      final failure = mapper.fromJson({'code': 'CAMPAIGN_NOT_ACTIVE'});

      expect(failure.code, FailureCode.campaignInactive);
      expect(failure.message, isNotEmpty);
    });

    test('maps unknown backend code to unknown failure', () {
      final failure = mapper.fromJson({'code': 'SOMETHING_NEW'});

      expect(failure.code, FailureCode.unknown);
    });
  });
}
