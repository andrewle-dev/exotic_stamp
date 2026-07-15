import type { PageResponse } from './common'

export interface PartnerResponse {
  id: string
  name: string
  logoUrl?: string
  bannerImageUrl?: string
  contactEmail?: string
  contractStartDate?: string
  contractEndDate?: string
  active?: boolean
}

export interface CreatePartnerRequest {
  name: string
  logoUrl?: string
  bannerImageUrl?: string
  contactEmail?: string
  contractStartDate?: string
  contractEndDate?: string
}

export interface UpdatePartnerRequest {
  name?: string
  logoUrl?: string
  bannerImageUrl?: string
  contactEmail?: string
  contractStartDate?: string
  contractEndDate?: string
}

export type PageResponsePartnerResponse = PageResponse<PartnerResponse>

export interface PartnersListParams {
  activeOnly?: boolean
  page?: number
  size?: number
}
