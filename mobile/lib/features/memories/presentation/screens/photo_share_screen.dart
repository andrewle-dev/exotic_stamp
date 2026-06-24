import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../../../core/di/injection.dart';
import '../../../../shared/widgets/app_button.dart';
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

  Future<void> _onSharePressed() async {
    final cubit = context.read<PhotoShareCubit>();
    final state = cubit.state;
    if (!state.hasPhoto || state.photoPath == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng chọn ảnh trước khi chia sẻ.')),
      );
      return;
    }

    final bytes = await captureWidgetPng(_previewKey);
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

    if (!mounted) {
      return;
    }

    final next = cubit.state;
    if (next.status == PhotoShareStatus.shared) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã mở bảng chia sẻ.')),
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

  void _onSavePressed() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text(
          'Lưu vào thư viện ảnh chưa hỗ trợ trong MVP. Hãy dùng Chia sẻ.',
        ),
      ),
    );
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
          final isSharing = state.status == PhotoShareStatus.sharing;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(AppSpacing.lg),
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
                  const SizedBox(height: AppSpacing.sm),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton.icon(
                      onPressed: isSharing
                          ? null
                          : () => context.read<PhotoShareCubit>().clearPhoto(),
                      icon: const Icon(Icons.refresh_rounded, size: 18),
                      label: const Text('Đổi ảnh'),
                    ),
                  ),
                ],
                const SizedBox(height: AppSpacing.lg),
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
                const SizedBox(height: AppSpacing.xl),
                Row(
                  children: [
                    Expanded(
                      child: AppButton(
                        label: 'Chia sẻ',
                        variant: AppButtonVariant.accent,
                        isLoading: isSharing,
                        onPressed: state.hasPhoto && !isSharing
                            ? _onSharePressed
                            : null,
                        icon: const Icon(
                          Icons.ios_share_rounded,
                          color: AppColors.backgroundWhite,
                          size: 20,
                        ),
                      ),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: AppButton(
                        label: 'Lưu ảnh',
                        variant: AppButtonVariant.outlined,
                        onPressed: state.hasPhoto && !isSharing
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
                if (!state.hasStampContext) ...[
                  const SizedBox(height: AppSpacing.lg),
                  Text(
                    'Mở màn hình từ chi tiết Stamp để thêm nhãn ga và ngày thu.',
                    style: AppTextStyles.caption.copyWith(
                      color: AppColors.textSecondary,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ],
                const SizedBox(height: AppSpacing.xl),
              ],
            ),
          );
        },
      ),
    );
  }
}
