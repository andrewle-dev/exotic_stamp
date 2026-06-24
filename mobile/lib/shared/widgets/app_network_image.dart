import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';
import '../../core/utils/media_url_resolver.dart';

class AppNetworkImage extends StatelessWidget {
  AppNetworkImage({
    super.key,
    required this.imageUrl,
    this.width,
    this.height,
    this.fit = BoxFit.cover,
    this.borderRadius,
    this.placeholder,
    this.errorWidget,
    MediaUrlResolver? urlResolver,
  }) : _urlResolver = urlResolver ?? MediaUrlResolver();

  final String? imageUrl;
  final double? width;
  final double? height;
  final BoxFit fit;
  final BorderRadius? borderRadius;
  final Widget? placeholder;
  final Widget? errorWidget;
  final MediaUrlResolver _urlResolver;

  @override
  Widget build(BuildContext context) {
    final resolvedUrl = _urlResolver.resolve(imageUrl);
    final image = resolvedUrl == null
        ? _buildFallback()
        : CachedNetworkImage(
            imageUrl: resolvedUrl,
            width: width,
            height: height,
            fit: fit,
            placeholder: (context, url) => placeholder ?? _defaultPlaceholder(),
            errorWidget: (context, url, error) =>
                errorWidget ?? _buildFallback(),
          );

    if (borderRadius != null) {
      return ClipRRect(
        borderRadius: borderRadius!,
        child: image,
      );
    }

    return image;
  }

  Widget _defaultPlaceholder() {
    return Container(
      width: width,
      height: height,
      color: AppColors.surface,
      alignment: Alignment.center,
      child: const SizedBox(
        width: 24,
        height: 24,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          color: AppColors.primaryBlue,
        ),
      ),
    );
  }

  Widget _buildFallback() {
    return Container(
      width: width,
      height: height,
      color: AppColors.surface,
      alignment: Alignment.center,
      child: Icon(
        Icons.image_not_supported_outlined,
        color: AppColors.textSecondary,
        size: (height ?? 48) * 0.35,
      ),
    );
  }
}
