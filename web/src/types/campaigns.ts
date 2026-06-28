import type { PageResponse } from './common'

export type CampaignType = 'STANDARD' | 'SEASONAL' | 'EVENT'

export type CampaignStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED'

export type CampaignScheduleState = 'UPCOMING' | 'RUNNING' | 'EXPIRED'

export interface CampaignResponse {
  id: string
  code: string
  name: string
  displayName?: string
  description?: string
  campaignType?: CampaignType
  status: CampaignStatus
  startAt: string
  endAt: string
  bannerImageUrl?: string
  thumbnailImageUrl?: string
  priority?: number
  createdAt?: string
  updatedAt?: string
}

export interface CreateCampaignRequest {
  code: string
  name: string
  displayName?: string
  description?: string
  campaignType?: CampaignType
  startAt: string
  endAt: string
  bannerImageUrl?: string
  thumbnailImageUrl?: string
  priority?: number
}

export interface UpdateCampaignRequest {
  code?: string
  name?: string
  displayName?: string
  description?: string
  campaignType?: CampaignType
  status?: CampaignStatus
  startAt?: string
  endAt?: string
  bannerImageUrl?: string
  thumbnailImageUrl?: string
  priority?: number
}

export interface CampaignStationResponse {
  stationId: string
  name: string
  displayName?: string
  lineId?: string
  sortOrder?: number
}

export interface AssignCampaignStationRequest {
  stationId: string
}

export type PageResponseCampaignResponse = PageResponse<CampaignResponse>

export interface CampaignsListParams {
  page?: number
  size?: number
}
