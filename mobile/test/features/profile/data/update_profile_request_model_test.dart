import 'package:flutter_test/flutter_test.dart';
import 'package:metro_stamp_app/features/profile/data/models/update_profile_request_model.dart';

void main() {
  test('toJson includes only documented UpdateUserRequest fields', () {
    const request = UpdateProfileRequestModel(
      firstname: 'An',
      lastname: 'Nguyen',
      bio: 'Commuter',
      avatarUrl: 'https://cdn.example/avatar.png',
    );

    expect(
      request.toJson(),
      {
        'firstname': 'An',
        'lastname': 'Nguyen',
        'bio': 'Commuter',
        'avatarUrl': 'https://cdn.example/avatar.png',
      },
    );
    expect(request.toJson().containsKey('email'), isFalse);
    expect(request.toJson().containsKey('phoneNumber'), isFalse);
  });

  test('toJson omits null fields', () {
    const request = UpdateProfileRequestModel(firstname: 'An');

    expect(request.toJson(), {'firstname': 'An'});
  });
}
