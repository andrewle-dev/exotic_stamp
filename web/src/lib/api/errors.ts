import axios from 'axios'
import type { ErrorResponse } from '../../types/api'

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly path?: string

  constructor(message: string, status: number, code: string, path?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.path = path
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}

export function parseApiError(error: unknown): ApiError {
  if (isApiError(error)) {
    return error
  }

  if (axios.isAxiosError(error)) {
    const status = error.response?.status ?? 500
    const data = error.response?.data as ErrorResponse | undefined

    if (data?.message) {
      return new ApiError(
        data.message,
        data.status ?? status,
        data.code ?? 'UNKNOWN_ERROR',
        data.path,
      )
    }

    if (status === 401) {
      return new ApiError('Session expired. Please sign in again.', 401, 'UNAUTHORIZED')
    }
    if (status === 403) {
      return new ApiError('You do not have permission to perform this action.', 403, 'FORBIDDEN')
    }
    if (status === 404) {
      return new ApiError('The requested resource was not found.', 404, 'NOT_FOUND')
    }
    if (status === 409) {
      return new ApiError('This action conflicts with existing data.', 409, 'CONFLICT')
    }
    if (status === 422) {
      return new ApiError('The request could not be processed.', 422, 'UNPROCESSABLE')
    }

    return new ApiError(
      error.message || 'An unexpected error occurred.',
      status,
      'NETWORK_ERROR',
    )
  }

  if (error instanceof Error) {
    return new ApiError(error.message, 500, 'UNKNOWN_ERROR')
  }

  return new ApiError('An unexpected error occurred.', 500, 'UNKNOWN_ERROR')
}

export function getErrorMessage(error: unknown): string {
  return parseApiError(error).message
}

export function isForbiddenError(error: unknown): boolean {
  return parseApiError(error).status === 403
}

export function isUnauthorizedError(error: unknown): boolean {
  return parseApiError(error).status === 401
}

export function isNotFoundError(error: unknown): boolean {
  return parseApiError(error).status === 404
}

export function isConflictError(error: unknown): boolean {
  return parseApiError(error).status === 409
}

export function isValidationError(error: unknown): boolean {
  const status = parseApiError(error).status
  return status === 400 || status === 422
}
