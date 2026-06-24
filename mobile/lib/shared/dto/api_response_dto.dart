class ApiResponseDto<T> {
  const ApiResponseDto({
    required this.data,
    this.message,
  });

  final T data;
  final String? message;
}
