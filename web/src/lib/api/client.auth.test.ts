import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  markAuthBootstrapComplete,
  refreshAccessTokenOnce,
  resetAuthClientCoordinationForTests,
  resolveApiBaseUrl,
  waitForAuthBootstrap,
} from './client'
import { tokenStore } from '../auth/tokenStore'
import { QueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { parseApiError } from './errors'
import { createQueryClient } from '../query/queryClient'

describe('auth client coordination', () => {
  beforeEach(() => {
    resetAuthClientCoordinationForTests()
    tokenStore.clear()
  })

  afterEach(() => {
    resetAuthClientCoordinationForTests()
    tokenStore.clear()
  })

  it('waitForAuthBootstrap blocks until markAuthBootstrapComplete', async () => {
    let released = false
    const waiter = waitForAuthBootstrap().then(() => {
      released = true
    })

    await Promise.resolve()
    expect(released).toBe(false)

    markAuthBootstrapComplete()
    await waiter
    expect(released).toBe(true)
  })

  it('refreshAccessTokenOnce single-flights concurrent callers', async () => {
    const post = vi.fn().mockResolvedValue({
      data: { accessToken: 'access-shared', tokenType: 'Bearer' },
    })
    const client = { post } as never

    const [a, b] = await Promise.all([
      refreshAccessTokenOnce(client),
      refreshAccessTokenOnce(client),
    ])

    expect(post).toHaveBeenCalledTimes(1)
    expect(a?.accessToken).toBe('access-shared')
    expect(b?.accessToken).toBe('access-shared')
    expect(tokenStore.get()).toBe('access-shared')
  })

  it('failed refresh clears token and returns null once', async () => {
    tokenStore.set('stale')
    const post = vi.fn().mockRejectedValue(new Error('refresh failed'))
    const client = { post } as never

    const result = await refreshAccessTokenOnce(client)
    expect(result).toBeNull()
    expect(tokenStore.get()).toBeNull()
  })

  it('queryClient.clear removes previous user data after logout', () => {
    const queryClient = new QueryClient()
    queryClient.setQueryData(['users', 'me'], { id: 'user-a', email: 'a@example.com' })
    expect(queryClient.getQueryData(['users', 'me'])).toBeTruthy()

    queryClient.clear()
    expect(queryClient.getQueryData(['users', 'me'])).toBeUndefined()
  })

  it('resolveApiBaseUrl trims trailing slashes and falls back locally', () => {
    expect(resolveApiBaseUrl('https://api.example.com///')).toBe('https://api.example.com')
    expect(resolveApiBaseUrl(undefined)).toBe('http://localhost:8080')
  })

  it('parseApiError maps 429 retry headers', () => {
    const error = new axios.AxiosError(
      'Too Many Requests',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        status: 429,
        statusText: 'Too Many Requests',
        headers: { 'retry-after': '120' },
        config: {} as never,
        data: undefined,
      },
    )

    const parsed = parseApiError(error)
    expect(parsed.status).toBe(429)
    expect(parsed.code).toBe('RATE_LIMITED')
    expect(parsed.retryAfterSeconds).toBe(120)
  })

  it('parseApiError maps 503 service unavailability', () => {
    const error = new axios.AxiosError(
      'Service Unavailable',
      'ERR_BAD_RESPONSE',
      undefined,
      undefined,
      {
        status: 503,
        statusText: 'Service Unavailable',
        headers: {},
        config: {} as never,
        data: undefined,
      },
    )

    const parsed = parseApiError(error)
    expect(parsed.status).toBe(503)
    expect(parsed.code).toBe('SERVICE_UNAVAILABLE')
  })

  it('query retries transient 429 once and 503 twice', () => {
    const queryClient = createQueryClient()
    const retry = queryClient.getDefaultOptions().queries?.retry
    expect(typeof retry).toBe('function')
    if (typeof retry !== 'function') {
      throw new Error('retry should be configured')
    }

    expect(retry(0, parseApiError(new axios.AxiosError(
      'Too Many Requests',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        status: 429,
        statusText: 'Too Many Requests',
        headers: {},
        config: {} as never,
        data: undefined,
      },
    )))).toBe(true)
    expect(retry(1, parseApiError(new axios.AxiosError(
      'Too Many Requests',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        status: 429,
        statusText: 'Too Many Requests',
        headers: {},
        config: {} as never,
        data: undefined,
      },
    )))).toBe(false)
    expect(retry(0, parseApiError(new axios.AxiosError(
      'Service Unavailable',
      'ERR_BAD_RESPONSE',
      undefined,
      undefined,
      {
        status: 503,
        statusText: 'Service Unavailable',
        headers: {},
        config: {} as never,
        data: undefined,
      },
    )))).toBe(true)
    expect(retry(2, parseApiError(new axios.AxiosError(
      'Service Unavailable',
      'ERR_BAD_RESPONSE',
      undefined,
      undefined,
      {
        status: 503,
        statusText: 'Service Unavailable',
        headers: {},
        config: {} as never,
        data: undefined,
      },
    )))).toBe(false)
  })
})
