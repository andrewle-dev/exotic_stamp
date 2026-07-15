import type { PageResponse } from './common'

export type StampRarity = 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY'

export type StampDesignStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE'

export interface StampDesignResponse {
  id: string
  campaignId: string
  stationId: string
  name: string
  description?: string
  imageUrl: string
  previewImageUrl?: string
  rarity?: StampRarity
  status?: StampDesignStatus
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

export interface CreateStampDesignRequest {
  campaignId: string
  stationId: string
  name: string
  description?: string
  imageUrl: string
  previewImageUrl?: string
  rarity?: StampRarity
  status?: StampDesignStatus
  sortOrder?: number
}

export interface UpdateStampDesignRequest {
  campaignId?: string
  stationId?: string
  name?: string
  description?: string
  imageUrl?: string
  previewImageUrl?: string
  rarity?: StampRarity
  status?: StampDesignStatus
  sortOrder?: number
}

export type PageResponseStampDesignResponse = PageResponse<StampDesignResponse>

export interface StampDesignsListParams {
  page?: number
  size?: number
  campaignId?: string
}
