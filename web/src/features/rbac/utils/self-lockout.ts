export function isSelfUserId(enteredUserId: string, currentUserId: string | undefined): boolean {
  if (!currentUserId?.trim() || !enteredUserId.trim()) {
    return false
  }
  return enteredUserId.trim().toLowerCase() === currentUserId.trim().toLowerCase()
}

export const SELF_LOCKOUT_WARNING =
  'You are modifying your own roles. You may lose access after this action.'
