import type { ApiResponse } from '../../types/api'
import { ApiError } from './errors'

export function unwrapApiResponse<T>(response: ApiResponse<T>): T {
  if (!response.success) {
    throw new ApiError(
      response.message || 'Request failed.',
      400,
      'API_ERROR',
    )
  }

  if (response.data === null || response.data === undefined) {
    throw new ApiError('Response data is missing.', 500, 'EMPTY_DATA')
  }

  return response.data
}

export function unwrapApiResponseOptional<T>(response: ApiResponse<T>): T | undefined {
  if (!response.success) {
    throw new ApiError(
      response.message || 'Request failed.',
      400,
      'API_ERROR',
    )
  }

  return response.data ?? undefined
}
