export type MetroStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE'

export type Status = MetroStatus | 'ARCHIVED'

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}
