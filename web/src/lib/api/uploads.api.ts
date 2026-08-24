import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type { AssetUploadPurpose, PublicAssetUploadResponse } from '../../types/uploads'

export async function uploadPublicAsset(
  file: File,
  purpose: AssetUploadPurpose = 'GENERIC',
  entityId?: string,
): Promise<PublicAssetUploadResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const { data } = await apiClient.post<ApiResponse<PublicAssetUploadResponse>>(
    '/api/v1/admin/uploads/public',
    formData,
    {
      params: { purpose, ...(entityId ? { entityId } : {}) },
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  )

  return unwrapApiResponse(data)
}
