String formatRecentStampTime(DateTime collectedAt) {
  final now = DateTime.now();
  final diff = now.difference(collectedAt);

  if (diff.inMinutes < 1) {
    return 'Vừa xong';
  }
  if (diff.inMinutes < 60) {
    return '${diff.inMinutes} phút trước';
  }
  if (diff.inHours < 24) {
    return '${diff.inHours} giờ trước';
  }
  if (diff.inDays == 1) {
    return 'Hôm qua';
  }
  if (diff.inDays < 7) {
    return '${diff.inDays} ngày trước';
  }
  return '${collectedAt.day}/${collectedAt.month}/${collectedAt.year}';
}
