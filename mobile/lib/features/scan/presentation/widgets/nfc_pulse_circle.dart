import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';

/// Animated NFC pulse ring used on the scan waiting screen.
class NfcPulseCircle extends StatefulWidget {
  const NfcPulseCircle({
    super.key,
    this.isScanning = true,
    this.size = 220,
  });

  final bool isScanning;
  final double size;

  @override
  State<NfcPulseCircle> createState() => _NfcPulseCircleState();
}

class _NfcPulseCircleState extends State<NfcPulseCircle>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1800),
    );
    if (widget.isScanning) {
      _controller.repeat();
    }
  }

  @override
  void didUpdateWidget(covariant NfcPulseCircle oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.isScanning && !_controller.isAnimating) {
      _controller.repeat();
    } else if (!widget.isScanning && _controller.isAnimating) {
      _controller.stop();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.size;
    final atmosphereSize = size * 1.42;

    return SizedBox(
      width: atmosphereSize,
      height: atmosphereSize,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Soft cool-blue wash — brand tokens only, no neon.
          DecoratedBox(
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: RadialGradient(
                colors: [
                  AppColors.blueTint,
                  AppColors.blueSurface.withValues(alpha: 0.65),
                  AppColors.backgroundWhite.withValues(alpha: 0),
                ],
                stops: const [0.0, 0.5, 1.0],
              ),
            ),
            child: SizedBox(
              width: atmosphereSize,
              height: atmosphereSize,
            ),
          ),
          SizedBox(
            width: size,
            height: size,
            child: Stack(
              alignment: Alignment.center,
              children: [
                if (widget.isScanning)
                  AnimatedBuilder(
                    animation: _controller,
                    builder: (context, child) {
                      final scale = 0.75 + (_controller.value * 0.35);
                      final opacity = (1 - _controller.value) * 0.45;
                      return Transform.scale(
                        scale: scale,
                        child: Container(
                          width: size,
                          height: size,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: AppColors.primaryBlue
                                  .withValues(alpha: opacity),
                              width: 3,
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                Container(
                  width: size * 0.72,
                  height: size * 0.72,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: const LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        AppColors.backgroundWhite,
                        AppColors.blueTint,
                      ],
                    ),
                    border: Border.all(color: AppColors.primaryBlue, width: 2),
                  ),
                  child: Icon(
                    Icons.nfc_rounded,
                    size: size * 0.28,
                    color: AppColors.primaryBlue,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
