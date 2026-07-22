import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  markAuthBootstrapComplete,
  refreshAccessTokenOnce,
  resetAuthClientCoordinationForTests,
  waitForAuthBootstrap,
} from './client'
import { tokenStore } from '../auth/tokenStore'
import { QueryClient } from '@tanstack/react-query'

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
})
