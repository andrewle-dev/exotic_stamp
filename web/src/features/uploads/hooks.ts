import type { AssetUploadPurpose } from '../../types/uploads'
import { useMutation } from '@tanstack/react-query'
import { uploadPublicAsset } from '../../lib/api/uploads.api'

export interface UploadPublicAssetVars {
  file: File
  purpose?: AssetUploadPurpose
  entityId?: string
}

export function useUploadPublicAsset() {
  return useMutation({
    mutationFn: ({ file, purpose = 'GENERIC', entityId }: UploadPublicAssetVars) =>
      uploadPublicAsset(file, purpose, entityId),
  })
}
