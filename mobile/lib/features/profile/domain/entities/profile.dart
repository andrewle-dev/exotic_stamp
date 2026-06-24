import 'package:equatable/equatable.dart';

/// Optional profile statistics sourced from existing backend APIs only.
///
/// API gap: no dedicated `GET /api/v1/mobile/profile` summary endpoint.
/// [collectedStampsCount] may come from `GET /collection/progress` on the
/// first active metro line only (MVP limitation — not a multi-line total).
/// [memoriesCount] may come from `GET /community/share-events/me`.
class ProfileStats extends Equatable {
  const ProfileStats({
    this.collectedStampsCount,
    this.memoriesCount,
  });

  final int? collectedStampsCount;
  final int? memoriesCount;

  bool get hasAnyData => collectedStampsCount != null || memoriesCount != null;

  @override
  List<Object?> get props => [collectedStampsCount, memoriesCount];
}

class Profile extends Equatable {
  const Profile({
    required this.id,
    required this.email,
    required this.username,
    this.firstname,
    this.lastname,
    this.phoneNumber,
    this.avatarUrl,
    this.bio,
    this.createdAt,
    this.stats,
  });

  final String id;
  final String email;
  final String username;
  final String? firstname;
  final String? lastname;
  final String? phoneNumber;
  final String? avatarUrl;
  final String? bio;
  final DateTime? createdAt;
  final ProfileStats? stats;

  String get displayName {
    final first = firstname?.trim();
    final last = lastname?.trim();
    if (first != null && first.isNotEmpty && last != null && last.isNotEmpty) {
      return '$first $last';
    }
    if (first != null && first.isNotEmpty) {
      return first;
    }
    return username.isNotEmpty ? username : email;
  }

  @override
  List<Object?> get props => [
        id,
        email,
        username,
        firstname,
        lastname,
        phoneNumber,
        avatarUrl,
        bio,
        createdAt,
        stats,
      ];
}
