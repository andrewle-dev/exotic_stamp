import 'package:equatable/equatable.dart';

/// Optional profile statistics sourced from backend APIs.
class ProfileStats extends Equatable {
  const ProfileStats({
    this.collectedStampsCount,
    this.linesCount,
    this.rankPosition,
    this.memoriesCount,
    this.level,
  });

  final int? collectedStampsCount;
  final int? linesCount;
  final int? rankPosition;
  final int? memoriesCount;
  final int? level;

  bool get hasAnyData =>
      collectedStampsCount != null ||
      linesCount != null ||
      rankPosition != null ||
      memoriesCount != null ||
      level != null;

  @override
  List<Object?> get props => [
        collectedStampsCount,
        linesCount,
        rankPosition,
        memoriesCount,
        level,
      ];
}

class ProfileInvite extends Equatable {
  const ProfileInvite({
    required this.referralCode,
    required this.description,
  });

  final String referralCode;
  final String description;

  @override
  List<Object?> get props => [referralCode, description];
}

class ProfileMemory extends Equatable {
  const ProfileMemory({
    required this.id,
    required this.title,
    this.subtitle,
    this.imageUrl,
    this.capturedAtLabel,
  });

  final String id;
  final String title;
  final String? subtitle;
  final String? imageUrl;
  final String? capturedAtLabel;

  @override
  List<Object?> get props => [id, title, subtitle, imageUrl, capturedAtLabel];
}

class ProfileAchievement extends Equatable {
  const ProfileAchievement({
    required this.id,
    required this.title,
    this.earnedAtLabel,
    this.locked = false,
    this.iconName,
  });

  final String id;
  final String title;
  final String? earnedAtLabel;
  final bool locked;
  final String? iconName;

  @override
  List<Object?> get props => [id, title, earnedAtLabel, locked, iconName];
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
    this.subtitle,
    this.stats,
    this.invite,
    this.memories = const [],
    this.achievements = const [],
    this.appVersionLabel,
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
  final String? subtitle;
  final ProfileStats? stats;
  final ProfileInvite? invite;
  final List<ProfileMemory> memories;
  final List<ProfileAchievement> achievements;
  final String? appVersionLabel;

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

  String? get joinedLabel {
    if (createdAt == null) {
      return null;
    }
    const months = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];
    final month = months[createdAt!.month - 1];
    return 'Joined $month ${createdAt!.year}';
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
        subtitle,
        stats,
        invite,
        memories,
        achievements,
        appVersionLabel,
      ];
}
