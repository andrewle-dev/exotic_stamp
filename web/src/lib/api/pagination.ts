export interface PaginationParams {
  page?: number
  size?: number
  sort?: string
}

/** Convert 1-based UI page to backend 0-based page index. */
export function toBackendPage(uiPage: number): number {
  return Math.max(0, uiPage - 1)
}

/** Convert backend 0-based page to 1-based UI page. */
export function toUiPage(backendPage: number): number {
  return backendPage + 1
}

export function buildPaginationQuery(params: PaginationParams): Record<string, string | number> {
  const query: Record<string, string | number> = {}

  if (params.page !== undefined) {
    query.page = params.page
  }
  if (params.size !== undefined) {
    query.size = params.size
  }
  if (params.sort) {
    query.sort = params.sort
  }

  return query
}
