import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../stamp_book/domain/usecases/get_stamp_book_usecase.dart';
import '../../domain/entities/photo_share_context.dart';
import '../../domain/usecases/record_share_event_usecase.dart';
import '../cubit/photo_share_cubit.dart';
import '../cubit/photo_share_state.dart';
import '../services/native_share_service.dart';
import '../services/photo_picker_service.dart';
import '../utils/photo_share_capture.dart';
import '../widgets/photo_picker_placeholder.dart';
import '../widgets/photo_share_editor_controls.dart';
import '../widgets/photo_share_preview.dart';
import '../widgets/photo_share_stamp_platform.dart';

class PhotoShareScreen extends StatelessWidget {
  const PhotoShareScreen({
    super.key,
    this.initialContext,
    this.cubit,
  });

  final PhotoShareContext? initialContext;
  final PhotoShareCubit? cubit;

  @override
  Widget build(BuildContext context) {
    final routeContext = initialContext ??
        (cubit != null
            ? cubit!.state.context
            : GoRouterState.of(context).extra as PhotoShareContext?);

    if (cubit != null) {
      return BlocProvider<PhotoShareCubit>.value(
        value: cubit!,
        child: _PhotoShareView(initialContext: routeContext),
      );
    }

    return BlocProvider(
      create: (_) => PhotoShareCubit(
        recordShareEventUseCase: RecordShareEventUseCase(
          Injection.instance.memoriesRepository,
        ),
        getStampBookUseCase: GetStampBookUseCase(
          Injection.instance.stampBookRepository,
        ),
        photoPickerService: ImagePickerPhotoPickerService(),
        nativeShareService: SharePlusNativeShareService(),
        initialContext: routeContext,
      ),
      child: _PhotoShareView(initialContext: routeContext),
    );
  }
}

class _PhotoShareView extends StatefulWidget {
  const _PhotoShareView({this.initialContext});

  final PhotoShareContext? initialContext;

  @override
  State<_PhotoShareView> createState() => _PhotoShareViewState();
}

class _PhotoShareViewState extends State<_PhotoShareView> {
  final _previewKey = GlobalKey();
  late final TextEditingController _captionController;

  @override
  void initState() {
    super.initState();
    final cubit = context.read<PhotoShareCubit>();
    _captionController = TextEditingController(text: cubit.state.caption);
    _captionController.addListener(() {
      cubit.setCaption(_captionController.text);
    });
  }

  @override
  void dispose() {
    _captionController.dispose();
    super.dispose();
  }

  Future<Uint8List?> _capturePreview() async {
    return captureWidgetPng(_previewKey);
  }

  Future<void> _onSharePressed() async {
    final cubit = context.read<PhotoShareCubit>();
    final state = cubit.state;
    if (!state.hasPhoto || state.photoPath == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng chọn ảnh trước khi chia sẻ.')),
      );
      return;
    }

    final bytes = await _capturePreview();
    if (bytes == null) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Không thể tạo ảnh chia sẻ.')),
      );
      return;
    }

    await cubit.shareComposedImage(bytes);
    _showShareResult(cubit.state);
  }

  Future<void> _onSavePressed() async {
    final cubit = context.read<PhotoShareCubit>();
    if (!cubit.state.hasPhoto) {
      return;
    }

    final bytes = await _capturePreview();
    if (bytes == null) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Không thể tạo ảnh.')),
      );
      return;
    }

    await cubit.saveComposedImage(bytes);
    if (!mounted) {
      return;
    }
    final next = cubit.state;
    if (next.status == PhotoShareStatus.saved) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã lưu ảnh kỷ niệm.')),
      );
    } else if (next.status == PhotoShareStatus.shareFailed) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(next.failure?.message ?? 'Không thể lưu ảnh.'),
        ),
      );
    }
  }

  Future<void> _onPlatformSelected(PhotoSharePlatform platform) async {
    final cubit = context.read<PhotoShareCubit>();
    if (!cubit.state.hasPhoto) {
      return;
    }

    final bytes = await _capturePreview();
    if (bytes == null) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Không thể tạo ảnh chia sẻ.')),
      );
      return;
    }

    await cubit.shareWithPlatform(
      platform: platform,
      composedImageBytes: bytes,
    );
    _showShareResult(cubit.state);
  }

  void _showShareResult(PhotoShareState next) {
    if (!mounted) {
      return;
    }
    if (next.status == PhotoShareStatus.shared) {
      final message = next.trackingFailed
          ? 'Đã chia sẻ. Ghi nhận sự kiện không thành công.'
          : 'Đã mở bảng chia sẻ.';
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message)),
      );
    } else if (next.status == PhotoShareStatus.shareFailed) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            next.failure?.message ?? 'Không thể chia sẻ ảnh.',
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundWhite,
      appBar: AppBar(
        backgroundColor: AppColors.backgroundWhite,
        foregroundColor: AppColors.textPrimary,
        title: const Text('Chia sẻ kỷ niệm'),
      ),
      body: BlocBuilder<PhotoShareCubit, PhotoShareState>(
        builder: (context, state) {
          final isBusy = state.status == PhotoShareStatus.sharing ||
              state.status == PhotoShareStatus.saving;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(AppSpacing.xl),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                if (state.hasPhoto && state.photoPath != null)
                  RepaintBoundary(
                    key: _previewKey,
                    child: PhotoSharePreview(
                      photoPath: state.photoPath!,
                      context: state.context,
                      showStationName: state.showStationName,
                      showCollectionDate: state.showCollectionDate,
                    ),
                  )
                else
                  PhotoPickerPlaceholder(
                    onPickGallery: () =>
                        context.read<PhotoShareCubit>().pickFromGallery(),
                    onPickCamera: () =>
                        context.read<PhotoShareCubit>().pickFromCamera(),
                  ),
                if (state.hasPhoto) ...[
                  const SizedBox(height: AppSpacing.md),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton.icon(
                      onPressed: isBusy
                          ? null
                          : () => context.read<PhotoShareCubit>().clearPhoto(),
                      icon: const Icon(Icons.refresh_rounded, size: 18),
                      label: const Text('Đổi ảnh'),
                    ),
                  ),
                ],
                if (state.stampOptions.isNotEmpty) ...[
                  const SizedBox(height: AppSpacing.xl),
                  PhotoShareStampSelectorRow(
                    options: state.stampOptions,
                    selectedStationId: state.selectedStationId,
                    onSelected: (option) =>
                        context.read<PhotoShareCubit>().selectStamp(option),
                  ),
                ],
                const SizedBox(height: AppSpacing.xl),
                PhotoShareEditorControls(
                  captionController: _captionController,
                  showStationName: state.showStationName,
                  showCollectionDate: state.showCollectionDate,
                  hasStampContext: state.hasStampContext,
                  onShowStationNameChanged: (value) => context
                      .read<PhotoShareCubit>()
                      .toggleShowStationName(value),
                  onShowCollectionDateChanged: (value) => context
                      .read<PhotoShareCubit>()
                      .toggleShowCollectionDate(value),
                ),
                const SizedBox(height: AppSpacing.xxl),
                Row(
                  children: [
                    Expanded(
                      child: AppButton(
                        label: 'Chia sẻ',
                        variant: AppButtonVariant.accent,
                        isLoading: state.status == PhotoShareStatus.sharing,
                        onPressed: state.hasPhoto && !isBusy
                            ? _onSharePressed
                            : null,
                        icon: const Icon(
                          Icons.ios_share_rounded,
                          color: AppColors.backgroundWhite,
                          size: 20,
                        ),
                      ),
                    ),
                    const SizedBox(width: AppSpacing.lg),
                    Expanded(
                      child: AppButton(
                        label: 'Lưu ảnh',
                        variant: AppButtonVariant.outlined,
                        isLoading: state.status == PhotoShareStatus.saving,
                        onPressed: state.hasPhoto && !isBusy
                            ? _onSavePressed
                            : null,
                        icon: const Icon(
                          Icons.download_rounded,
                          color: AppColors.primaryBlue,
                          size: 20,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: AppSpacing.xxl),
                PhotoSharePlatformChips(
                  enabled: state.hasPhoto && !isBusy,
                  onPlatformSelected: _onPlatformSelected,
                ),
                if (!state.hasStampContext) ...[
                  const SizedBox(height: AppSpacing.xl),
                  Text(
                    'Mở màn hình từ chi tiết Stamp để thêm nhãn ga và ngày thu.',
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.textSecondary,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ],
                const SizedBox(height: AppSpacing.xxl),
              ],
            ),
          );
        },
      ),
    );
  }
}
