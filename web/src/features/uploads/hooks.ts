import type { AssetUploadPurpose } from '../../types/uploads'
import { useMutation } from '@tanstack/react-query'
import { uploadPublicAsset } from '../../lib/api/uploads.api'

export interface UploadPublicAssetVars {
  file: File
  purpose?: AssetUploadPurpose
}

export function useUploadPublicAsset() {
  return useMutation({
    mutationFn: ({ file, purpose = 'GENERIC' }: UploadPublicAssetVars) =>
      uploadPublicAsset(file, purpose),
  })
}
