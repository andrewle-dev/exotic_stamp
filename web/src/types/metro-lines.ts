import type { MetroStatus, PageResponse } from './common'

export type { MetroStatus, PageResponse }

export interface LineResponse {
  id: string
  code: string
  name: string
  displayName?: string
  description?: string
  colorHex?: string
  sortOrder?: number
  totalStations?: number
  status: MetroStatus
  createdAt?: string
  updatedAt?: string
}

export interface LineDetailResponse extends LineResponse {
  stations?: import('./stations').StationResponse[]
}

export interface CreateLineRequest {
  code: string
  name: string
  displayName?: string
  description?: string
  colorHex?: string
  sortOrder?: number
  status?: MetroStatus
}

export interface UpdateLineRequest {
  code?: string
  name?: string
  displayName?: string
  description?: string
  colorHex?: string
  sortOrder?: number
  status?: MetroStatus
}

export type PageResponseLineResponse = PageResponse<LineResponse>

export interface MetroLinesListParams {
  status?: MetroStatus
  search?: string
  page?: number
  size?: number
  sort?: string
}
