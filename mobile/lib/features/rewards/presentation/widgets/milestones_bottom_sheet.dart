import 'package:flutter/material.dart';

import '../../../../app/theme/app_colors.dart';
import '../../../../app/theme/app_spacing.dart';
import '../../../../app/theme/app_text_styles.dart';
import '../../domain/entities/milestone.dart';
import '../../domain/entities/user_reward.dart';
import 'milestone_timeline_item.dart';

Future<void> showMilestonesBottomSheet({
  required BuildContext context,
  required List<Milestone> milestones,
  required UserReward? Function(String milestoneId) rewardForMilestone,
  required void Function(UserReward reward) onOpenReward,
}) {
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    backgroundColor: AppColors.backgroundWhite,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (sheetContext) {
      return _MilestonesBottomSheet(
        milestones: milestones,
        rewardForMilestone: rewardForMilestone,
        onOpenReward: (reward) {
          Navigator.of(sheetContext).pop();
          onOpenReward(reward);
        },
      );
    },
  );
}

class _MilestonesBottomSheet extends StatelessWidget {
  const _MilestonesBottomSheet({
    required this.milestones,
    required this.rewardForMilestone,
    required this.onOpenReward,
  });

  final List<Milestone> milestones;
  final UserReward? Function(String milestoneId) rewardForMilestone;
  final void Function(UserReward reward) onOpenReward;

  @override
  Widget build(BuildContext context) {
    final bottomInset = MediaQuery.paddingOf(context).bottom;
    final maxHeight = MediaQuery.sizeOf(context).height * 0.85;

    return SafeArea(
      child: SizedBox(
        height: maxHeight,
        child: Padding(
          padding: EdgeInsets.fromLTRB(
            AppSpacing.xl,
            AppSpacing.lg,
            AppSpacing.xl,
            AppSpacing.xl + bottomInset,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(999),
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      'Hành trình phần thưởng',
                      style: AppTextStyles.titleMedium.copyWith(
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ),
                  IconButton(
                    tooltip: 'Đóng',
                    onPressed: () => Navigator.of(context).pop(),
                    icon: const Icon(Icons.close_rounded),
                  ),
                ],
              ),
              Text(
                '${milestones.length} mốc thưởng',
                style: AppTextStyles.bodyMedium.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              Expanded(
                child: ListView.builder(
                  itemCount: milestones.length,
                  itemBuilder: (context, index) {
                    final milestone = milestones[index];
                    final linked = rewardForMilestone(milestone.id);
                    return MilestoneTimelineItem(
                      milestone: milestone,
                      isFirst: index == 0,
                      isLast: index == milestones.length - 1,
                      onClaimTap: linked == null
                          ? null
                          : () => onOpenReward(linked),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
