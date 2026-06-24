import 'package:equatable/equatable.dart';

import '../../../../core/errors/failure.dart';
import '../../domain/entities/photo_share_context.dart';

enum PhotoShareStatus {
  initial,
  pickingPhoto,
  editing,
  sharing,
  shared,
  shareFailed,
}

class PhotoShareState extends Equatable {
  const PhotoShareState({
    this.status = PhotoShareStatus.initial,
    this.context,
    this.photoPath,
    this.caption = '',
    this.showStationName = true,
    this.showCollectionDate = true,
    this.failure,
    this.trackingRecorded = false,
    this.trackingFailed = false,
  });

  final PhotoShareStatus status;
  final PhotoShareContext? context;
  final String? photoPath;
  final String caption;
  final bool showStationName;
  final bool showCollectionDate;
  final Failure? failure;
  final bool trackingRecorded;
  final bool trackingFailed;

  bool get hasPhoto => photoPath != null && photoPath!.isNotEmpty;
  bool get hasStampContext => context != null;

  PhotoShareState copyWith({
    PhotoShareStatus? status,
    PhotoShareContext? context,
    String? photoPath,
    String? caption,
    bool? showStationName,
    bool? showCollectionDate,
    Failure? failure,
    bool clearFailure = false,
    bool? trackingRecorded,
    bool? trackingFailed,
    bool clearPhoto = false,
  }) {
    return PhotoShareState(
      status: status ?? this.status,
      context: context ?? this.context,
      photoPath: clearPhoto ? null : (photoPath ?? this.photoPath),
      caption: caption ?? this.caption,
      showStationName: showStationName ?? this.showStationName,
      showCollectionDate: showCollectionDate ?? this.showCollectionDate,
      failure: clearFailure ? null : (failure ?? this.failure),
      trackingRecorded: trackingRecorded ?? this.trackingRecorded,
      trackingFailed: trackingFailed ?? this.trackingFailed,
    );
  }

  @override
  List<Object?> get props => [
        status,
        context,
        photoPath,
        caption,
        showStationName,
        showCollectionDate,
        failure,
        trackingRecorded,
        trackingFailed,
      ];
}
