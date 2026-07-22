import { describe, expect, it, beforeEach } from 'vitest'
import {
  clearLegacyPersistedAccessToken,
  tokenStore,
} from './tokenStore'

describe('tokenStore', () => {
  beforeEach(() => {
    tokenStore.clear()
  })

  it('holds access token in memory only', () => {
    tokenStore.set('access-1')
    expect(tokenStore.get()).toBe('access-1')
  })

  it('clear removes memory token', () => {
    tokenStore.set('access-1')
    tokenStore.clear()
    expect(tokenStore.get()).toBeNull()
  })

  it('clearLegacyPersistedAccessToken is safe without localStorage', () => {
    expect(() => clearLegacyPersistedAccessToken()).not.toThrow()
  })
})
