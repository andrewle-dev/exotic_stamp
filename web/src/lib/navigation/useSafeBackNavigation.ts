import { useCallback } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'

export interface DetailNavigationState {
  from?: string
}

function isSameParentRoute(from: string, fallbackPath: string): boolean {
  return from === fallbackPath || from.startsWith(`${fallbackPath}?`)
}

/**
 * Returns a back handler that prefers browser history when the user arrived
 * from the parent list in this session, otherwise navigates to `fallbackPath`.
 */
export function useSafeBackNavigation(fallbackPath: string) {
  const navigate = useNavigate()
  const location = useLocation()

  return useCallback(() => {
    const state = location.state as DetailNavigationState | null
    const from = state?.from

    if (typeof from === 'string' && isSameParentRoute(from, fallbackPath)) {
      navigate(-1)
      return
    }

    navigate(fallbackPath)
  }, [fallbackPath, location.state, navigate])
}

/** Location state to pass when opening a detail page from its parent list. */
export function detailFromListState(listPath: string): DetailNavigationState {
  return { from: listPath }
}
