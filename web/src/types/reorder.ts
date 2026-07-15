export interface ReorderItemResponse {
  id: string
  sortOrder: number
}

export interface ReorderResponse {
  scopeId?: string | null
  updatedCount: number
  items: ReorderItemResponse[]
}

export interface ReorderLinesRequest {
  orderedIds: string[]
}

export interface ReorderStationsRequest {
  lineId: string
  orderedIds: string[]
}

export interface ReorderStampDesignsRequest {
  campaignId: string
  orderedIds: string[]
}

export interface ReorderMilestonesRequest {
  campaignId: string
  orderedIds: string[]
}
