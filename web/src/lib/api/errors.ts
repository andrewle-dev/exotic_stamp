import axios from 'axios'
import type { ErrorResponse } from '../../types/api'

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly path?: string
  readonly retryAfterSeconds?: number

  constructor(
    message: string,
    status: number,
    code: string,
    path?: string,
    retryAfterSeconds?: number,
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.path = path
    this.retryAfterSeconds = retryAfterSeconds
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}

function parseRetryAfterSeconds(value: unknown): number | undefined {
  if (typeof value !== 'string' || value.trim() === '') {
    return undefined
  }

  const asNumber = Number(value)
  if (Number.isFinite(asNumber) && asNumber >= 0) {
    return asNumber
  }

  const retryAt = Date.parse(value)
  if (Number.isNaN(retryAt)) {
    return undefined
  }

  const deltaSeconds = Math.ceil((retryAt - Date.now()) / 1000)
  return deltaSeconds > 0 ? deltaSeconds : 0
}

export function parseApiError(error: unknown): ApiError {
  if (isApiError(error)) {
    return error
  }

  if (axios.isAxiosError(error)) {
    const status = error.response?.status ?? 500
    const data = error.response?.data as ErrorResponse | undefined
    const retryAfterSeconds = parseRetryAfterSeconds(error.response?.headers?.['retry-after'])

    if (data?.message) {
      return new ApiError(
        data.message,
        data.status ?? status,
        data.code ?? 'UNKNOWN_ERROR',
        data.path,
        retryAfterSeconds,
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
    if (status === 429) {
      return new ApiError(
        'Too many requests. Please wait a moment and try again.',
        429,
        'RATE_LIMITED',
        undefined,
        retryAfterSeconds,
      )
    }
    if (status === 503) {
      return new ApiError(
        'Service is temporarily unavailable. Please try again shortly.',
        503,
        'SERVICE_UNAVAILABLE',
        undefined,
        retryAfterSeconds,
      )
    }

    return new ApiError(
      error.message || 'An unexpected error occurred.',
      status,
      'NETWORK_ERROR',
      undefined,
      retryAfterSeconds,
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
